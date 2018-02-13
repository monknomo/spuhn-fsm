package com.gunnargissel.spuhn.builder;

import com.gunnargissel.spuhn.Transition;

import java.util.function.Consumer;


public class GenericDRFSMTransition<State,Event,Context> extends Transition<State,Event,Context> {

	public GenericDRFSMTransition(State fromState, State toState, Event onEvent, Consumer<Context> transitionFunc) {
		super(fromState, toState, onEvent, transitionFunc);
	}

}
