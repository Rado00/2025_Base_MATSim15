package org.eth.week7.lectures;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class LinkModule extends AbstractModule{

    private int number;

    public LinkModule(int number) {
        this.number = number;
    }


    protected void configure() {

       // 1. 
       // bind(Link.class).asEagerSingleton();

       // 2.
       // bind(Link.class).toInstance(new Link("Wehntalerstrasse"));


       // 3.

       bind(String.class).annotatedWith(Names.named("StreetName")).toInstance(new String("Birchstrasse"));


       // 4.


       bind(Link.class).toProvider(new LinkProvider(number));

    }

}
