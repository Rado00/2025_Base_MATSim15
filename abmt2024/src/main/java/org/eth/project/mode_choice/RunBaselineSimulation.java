package org.eth.project.mode_choice;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModule;
import org.eqasim.switzerland.SwitzerlandConfigurator;
import org.eqasim.switzerland.mode_choice.SwissModeChoiceModule;
import org.eth.project.config.AstraConfigurator;
import org.eth.project.travel_time.SmoothingTravelTimeModule;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehiclesFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class RunBaselineSimulation {

	/**
	 * 
	 * you need one argument to run this class
	 * --config-path "path-to-your-config-file/config.xml"
	 * 
	 */
	static public void main(String[] args) throws ConfigurationException, MalformedURLException, IOException {

		// set preventwaitingtoentertraffic to y if you want to to prevent that waiting
		// traffic has to wait for space in the link buffer
		// this is especially important to avoid high waiting times when we cutout
		// scenarios from a larger scenario.

		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-parameter", "cost-parameter", "preventwaitingtoentertraffic") //
				.build();
		AstraConfigurator astraConfigurator = new AstraConfigurator();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), astraConfigurator.getConfigGroups());
		AstraConfigurator.configure(config);
		cmd.applyConfiguration(config);
		if (cmd.hasOption("preventwaitingtoentertraffic")) {
			if (cmd.getOption("preventwaitingtoentertraffic").get().equals("y")) {
				((QSimConfigGroup) config.getModules().get(QSimConfigGroup.GROUP_NAME))
						.setPcuThresholdForFlowCapacityEasing(1.0);
			}
		}
		Scenario scenario = ScenarioUtils.createScenario(config);


		SwitzerlandConfigurator configurator = new SwitzerlandConfigurator();
		configurator.configureScenario(scenario);
		ScenarioUtils.loadScenario(scenario);
		configurator.adjustScenario(scenario);
		astraConfigurator.adjustScenario(scenario);

		EqasimConfigGroup eqasimConfig = EqasimConfigGroup.get(config);

		for (Link link : scenario.getNetwork().getLinks().values()) {
			if (link.getAllowedModes().contains("car")) {				
				
				Set<String> allowedModes = new HashSet<>(link.getAllowedModes());
				allowedModes.add(TransportMode.bike);
				link.setAllowedModes(allowedModes);
				
			}
			
		}

		for (Link link : scenario.getNetwork().getLinks().values()) {
			double maximumSpeed = link.getFreespeed();
			boolean isMajor = true;

			for (Link other : link.getToNode().getInLinks().values()) {
				if (other.getCapacity() >= link.getCapacity()) {
					isMajor = false;
				}
			}

			if (!isMajor && link.getToNode().getInLinks().size() > 1) {
				double travelTime = link.getLength() / maximumSpeed;
				travelTime += eqasimConfig.getCrossingPenalty();
				link.setFreespeed(link.getLength() / travelTime);
			}
		}

		for (Person person : scenario.getPopulation().getPersons().values()) {

			
			for (PlanElement pe : person.getSelectedPlan().getPlanElements()) {
				if (pe instanceof Leg) {
					if (((Leg) pe).getMode().equals( TransportMode.bike)) {
						((Leg) pe).setRoute(null);
					}
				}
			}
		}

		scenario.getConfig().qsim().setVehiclesSource( QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData );

		// create all vehicleTypes requested by planscalcroute networkModes

		final VehiclesFactory vf = VehicleUtils.getFactory();
		scenario.getVehicles().addVehicleType(vf.createVehicleType(Id.create(TransportMode.car, VehicleType.class))
				.setMaximumVelocity(120.0/3.6));
		scenario.getVehicles().addVehicleType(vf.createVehicleType(Id.create("car_passenger", VehicleType.class))
				.setMaximumVelocity(120.0/3.6));
		scenario.getVehicles().addVehicleType(vf.createVehicleType(Id.create(TransportMode.truck, VehicleType.class))
				.setMaximumVelocity(120.0/3.6));
		scenario.getVehicles().addVehicleType( vf.createVehicleType(Id.create(TransportMode.bike, VehicleType.class))
				.setMaximumVelocity(15.0/3.6).setPcuEquivalents(0.25)); 


		// EqasimLinkSpeedCalculator deactivated!

		Controler controller = new Controler(scenario);
		configurator.configureController(controller);
		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModule());
		controller.addOverridingModule(new SwissModeChoiceModule(cmd));
		controller.addOverridingModule(new AstraModule(cmd));

		AstraConfigurator.configureController(controller, cmd);

		controller.addOverridingModule(new SmoothingTravelTimeModule());
		controller.addOverridingModule(new AbstractModule(){
			// preparation: compute max speed given link speed limit and vehicle maximum speed:
			private double getMaxSpeedFromVehicleAndLink( Link link, double time, Vehicle vehicle ) {

				double maxSpeedFromLink = link.getFreespeed( time );

				double maxSpeedFromVehicle = vehicle.getType().getMaximumVelocity();
				
				// as usual, we return the min of all the speeds:
				return Math.min ( maxSpeedFromLink, maxSpeedFromVehicle );
			}

			@Override public void install(){

				// set meaningful travel time binding for routing:
				this.addTravelTimeBinding( TransportMode.bike ).toInstance( new TravelTime(){
					
					@Inject 
					@Named(TransportMode.bike) TravelTimeCalculator bikeCalculator;

					@Override public double getLinkTravelTime( Link link, double time, Person person, Vehicle vehicle ){

						// we get the max speed from vehicle and link, as defined in the preparation above:
						final double maxSpeedFromVehicleAndLink = getMaxSpeedFromVehicleAndLink( link, time, vehicle );

						// we also get the speed from observation:
						double speedFromObservation = bikeCalculator.getLinkTravelTimes().getLinkTravelTime( link, time, person, vehicle );

						// we compute the min of the two:
						double actualSpeed = Math.min( speedFromObservation, maxSpeedFromVehicleAndLink );

						return link.getLength()/actualSpeed;
					}
				} );
				
			}
		} ) ;

		controller.run();
	}
}