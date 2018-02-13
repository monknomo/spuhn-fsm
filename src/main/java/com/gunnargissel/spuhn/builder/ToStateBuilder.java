package com.gunnargissel.spuhn.builder;


public interface ToStateBuilder<State, Event, Context> {

    /**
     * Defines the state this transition ends in.  Required to build a valid transition
     * @param state The state this transition ends in
     * @return an OnEventBuilder that defines what event triggers this transition
     */
	OnEventBuilder<State, Event, Context> to(State state);

}
