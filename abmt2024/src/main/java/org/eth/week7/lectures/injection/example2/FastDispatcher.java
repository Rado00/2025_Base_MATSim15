package org.eth.week7.lectures.injection.example2;

import com.google.inject.Inject;

public class FastDispatcher implements Dispatcher {
	
	@Inject
	public FastDispatcher() {
		
	}
	
	
	@Override
	public void dispatch() {
		// do something that is really fast here
		System.out.println("we are doing fast dispatching!");
	}
}
