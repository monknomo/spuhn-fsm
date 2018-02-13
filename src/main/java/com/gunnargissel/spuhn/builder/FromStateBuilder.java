package com.gunnargissel.spuhn.builder;


public interface FromStateBuilder<State, Event, Context> {

    /**
     * Define the state a transition begins in.  Required to build a valid transition
     * @param state the State this transition will begin in
     * @return a ToStateBuilder that will define the state this transition ends in
     */
	ToStateBuilder<State, Event, Context> from(State state);
	
	OnEventBuilder<State, Event, Context> refresh(State state);

}
