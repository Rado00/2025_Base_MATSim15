package org.eth.week7.lectures.injection.example2;

import com.google.inject.Inject;

public class FleetOperator {
	private final Dispatcher dispatcher;
	
	@Inject
	public FleetOperator(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	
	public void operateFleet() {
		// ....
		this.dispatcher.dispatch();
		
		// ....
	}
}