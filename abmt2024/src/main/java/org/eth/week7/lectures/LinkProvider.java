package org.eth.week7.lectures;

import com.google.inject.Provider;

public class LinkProvider implements Provider<Link> {

    private int number;

    public LinkProvider(int number) {
        this.number = number;

    }

    @Override
    public Link get() {
        
        if (number < 10) {
            return new Link("Birchstrasse");
        }
        else if (number < 20) {
            return new Link("Whentalerstrasse");
        }
        else
            return new Link("Zurichbergstrasse");



    }

}
