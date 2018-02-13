package com.gunnargissel.spuhn;

public interface SwingFiniteStateMachine<StateType, Event> {

    void initialize();
    void fire(Event event);
    boolean isCurrentState(StateType state);
    
}
