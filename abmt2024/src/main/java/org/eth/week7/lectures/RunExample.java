package org.eth.week7.lectures;

public class RunExample {

    public static void main(String[] args) {
		Link link = new Link("Wehntalerstrasse");
		// this is a traditional way of injecting dependencies in java
		Network network = new Network(link);
		System.out.println(network.getLink().getName());
		
	}
}
