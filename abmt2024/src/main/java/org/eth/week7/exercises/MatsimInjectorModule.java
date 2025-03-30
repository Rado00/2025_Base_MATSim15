package org.eth.week7.exercises;

import org.eth.week7.lectures.Link;
import org.eth.week7.lectures.LinkProvider;
import org.eth.week7.lectures.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.name.Names;

public class MatsimInjectorModule extends AbstractModule{

    @Override
    public void install() {
        String name = new String("X");
        bind(String.class).annotatedWith(Names.named("StreetName")).toInstance(name);
        // we want to provide a Link object using a provider
        bind(Link.class).toProvider(new LinkProvider(10));
        // we want to bind a NetworkGuice object and ensure that only one instance is
        // created
        bind(Network.class).asEagerSingleton();
    }

}
