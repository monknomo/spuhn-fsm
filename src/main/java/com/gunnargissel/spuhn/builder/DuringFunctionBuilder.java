package com.gunnargissel.spuhn.builder;

import java.util.function.Consumer;

public interface DuringFunctionBuilder<State, Event, Context> {

    /**
     * Defines the Consumer that will execute during this transition. Optional in valid transitions
     * 
     * @param func
     *            a Consumer that has access to this FiniteStateMachine's Context
     * @return This TransitionBuilder, to provide a final reference to the builder object, in case
     *         you are not using the FiniteStateMachineBuilder.transition() method to create
     *         transitions
     */
    TransitionBuilder<State, Event, Context> during(Consumer<Context> func);

}
