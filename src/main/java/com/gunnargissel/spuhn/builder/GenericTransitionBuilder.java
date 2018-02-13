package com.gunnargissel.spuhn.builder;

import com.gunnargissel.spuhn.State;
import com.gunnargissel.spuhn.Transition;

import java.util.Objects;
import java.util.function.Consumer;


public class GenericTransitionBuilder<StateType extends State<Context>, Event, Context> implements FromStateBuilder<StateType, Event, Context>, ToStateBuilder<StateType, Event, Context>,
		OnEventBuilder<StateType, Event, Context>, DuringFunctionBuilder<StateType, Event, Context>, TransitionBuilder<StateType, Event, Context> {

	private StateType fromState;
	private StateType toState;
	private Event onEvent;
	private Consumer<Context> transitionFunc;

	public GenericTransitionBuilder() {
	}
	
	public OnEventBuilder<StateType, Event, Context> refresh(StateType state){
	    Objects.requireNonNull(state);
	    this.fromState = state;
	    this.toState = state;
	    return this;
	}

	@Override
	public ToStateBuilder<StateType, Event, Context> from(StateType state) {
		Objects.requireNonNull(state);
		this.fromState = state;
		return this;
	}

	public StateType from() {
		return this.fromState;
	}

	@Override
	public OnEventBuilder<StateType, Event, Context> to(StateType state) {
		Objects.requireNonNull(state);
		this.toState = state;
		return this;
	}

	public StateType to() {
		return this.toState;
	}

	@Override
	public DuringFunctionBuilder<StateType, Event, Context> on(Event event) {
		Objects.requireNonNull(event);
		this.onEvent = event;
		return this;
	}

	public Event on() {
		return this.onEvent;
	}

	@Override
	public TransitionBuilder<StateType, Event, Context> during(Consumer<Context> func) {
		this.transitionFunc = func;
		return this;
	}

	public Consumer<Context> getPerform() {
		return this.transitionFunc;
	}

	public Transition<StateType, Event, Context> build() {
		return new GenericDRFSMTransition<StateType, Event, Context>(fromState, toState, onEvent, transitionFunc);
	}

	@Override
	public String toString() {
		return "GenericDRFSMTransitionBuilder [fromState=" + fromState + ", toState=" + toState + ", onEvent=" + onEvent + ", transitionFunc=" + transitionFunc + "]";
	}

    public StateType getFromState() {
        return fromState;
    }
    
    public StateType getToState() {
        return toState;
    }

}
