package org.eth.week7.lectures;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class RunInjectionExample {

    public static void main(String[] args) {
        int number = Integer.parseInt(args[0]);


        Injector injector = Guice.createInjector(new LinkModule(number));


        Link link = injector.getInstance(Link.class);

        Network network = injector.getInstance(Network.class);

        System.out.println(network.getLink().getName());

    }

}
