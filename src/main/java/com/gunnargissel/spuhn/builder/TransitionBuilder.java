package com.gunnargissel.spuhn.builder;


import com.gunnargissel.spuhn.Transition;

public interface TransitionBuilder<State, Event, Context> {

	Transition<State, Event, Context> build();
	
}
