package org.eth;

import org.matsim.core.controler.Controler;

public class RunSimulation {
    public static void main(String[] args) {
        // Either take config from argument or hardcode:
        String config = args.length > 0 ? args[0] : "../../scenarios/equil/config.xml";
        new Controler(config).run();
    }
}
