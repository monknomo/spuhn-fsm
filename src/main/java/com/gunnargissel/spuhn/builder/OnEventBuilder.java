package com.gunnargissel.spuhn.builder;


public interface OnEventBuilder<State, Event, Context> {

    /**
     * Defines the event that triggers this transition.  Required to build a valid transition
     * @param event The event that triggers this transition
     * @return An optional PerformFunctionBuilder that may be used to define a function that is executed during this transition
     */
	DuringFunctionBuilder<State, Event, Context> on(Event event);

}
