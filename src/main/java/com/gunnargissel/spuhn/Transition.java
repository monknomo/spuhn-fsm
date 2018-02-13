package com.gunnargissel.spuhn;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class Transition<State,Event,Context> {

	private final State fromState;
	private final State toState;
	private final Event onEvent;
	private final Optional<Consumer<Context>> transitionFunc;
	
	public Transition(State fromState, State toState, Event onEvent, Consumer<Context> transitionFunc){
		Objects.requireNonNull(fromState);
		Objects.requireNonNull(toState);
		Objects.requireNonNull(onEvent);
		this.fromState = fromState;
		this.toState = toState;
		this.onEvent = onEvent;
		this.transitionFunc = Optional.ofNullable(transitionFunc);
	}
	
	public void transit(Context ctx){
		transitionFunc.ifPresent(contextConsumer -> contextConsumer.accept(ctx));
	}

	public State getFromState() {
		return fromState;
	}

	public State getToState() {
		return toState;
	}

	public Event getOnEvent() {
		return onEvent;
	}

	public Optional<Consumer<Context>> getTransitionFunc() {
		return transitionFunc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fromState == null) ? 0 : fromState.hashCode());
		result = prime * result + ((onEvent == null) ? 0 : onEvent.hashCode());
		result = prime * result + ((toState == null) ? 0 : toState.hashCode());
		result = prime * result + ((transitionFunc == null) ? 0 : transitionFunc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Transition other = (Transition) obj;
		if (fromState == null) {
			if (other.fromState != null)
				return false;
		} else if (!fromState.equals(other.fromState))
			return false;
		if (onEvent == null) {
			if (other.onEvent != null)
				return false;
		} else if (!onEvent.equals(other.onEvent))
			return false;
		if (toState == null) {
			if (other.toState != null)
				return false;
		} else if (!toState.equals(other.toState))
			return false;
		if (transitionFunc == null) {
			if (other.transitionFunc != null)
				return false;
		} else if (!transitionFunc.equals(other.transitionFunc))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DRFSMTransition [fromState=" + fromState + ", toState=" + toState + ", onEvent=" + onEvent + ", transitionFunc=" + transitionFunc + "]";
	}
	
	
	
}
