package com.gunnargissel.spuhn;



import com.gunnargissel.spuhn.builder.FromStateBuilder;
import com.gunnargissel.spuhn.builder.GenericTransitionBuilder;

import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 
 * @author gunnar.gissel
 *
 *         This class is intended to be the correct way to build a finite state machine. The
 *         intended usage is to instantiate a FiniteStateMachineBuilder composed of the State, Event
 *         and Context types you will use. Then, create all the transitions that the intended
 *         FiniteStateMachine will be composed of, by using this.transition() and building off the
 *         returned values.
 * 
 *         When all the desired transitions have been added, use this.build() to create an
 *         initialized FiniteStateMachine
 * 
 *         The design of this class assumes you will create your own State, Event and Context types.
 *         The assumption is that you will create enums implementing the State and Context types. A
 *         simple enum (or even arbitrary Strings) is sufficent for an Event type. This library does
 *         not provide an interface for Events because there is no need for an event to do anything
 *         beyond trigger a transition between states.
 *
 * @param <StateType>
 *            The type of State that defines all possible states of the desired FiniteStateMachine
 * @param <Event>
 *            The type of Event that defines all possible events of the desired FiniteStateMachine
 * @param <Context>
 *            The type of Context that gives transitions and state entry a handle into external
 *            entities
 */
public class FiniteStateMachineBuilder<StateType extends State<Context>, Event, Context> {

    private ArrayList<GenericTransitionBuilder<StateType, Event, Context>> transitions = new ArrayList<>();
    private StateType initialState;
    private Context ctx;
    private Optional<Logger> logger = Optional.empty();

    public FiniteStateMachineBuilder(Context context, Optional<Logger> logger) {
        this.ctx = context;
        this.logger = logger;
    }

    /**
     * Creates a new transition builder used to define a transition in this FiniteStateMachineBuilder
     * 
     * @return a new FromStateBuilder, to allow a starting state to be added to this transition
     */
    public FromStateBuilder<StateType, Event, Context> transition() {
        GenericTransitionBuilder<StateType, Event, Context> tempTransition = new GenericTransitionBuilder<>();
        transitions.add(tempTransition);
        return tempTransition;
    }

    /**
     * Build a finite state machine out of the transitions encoded in this
     * FiniteStateMachineBuilder. Throws NullPointerException if an incomplete transition builder is
     * present
     * 
     * @return an initialized FiniteStateMachine
     */
    public FiniteStateMachine<StateType, Event, Context> build() {
        FiniteStateMachine<StateType, Event, Context> result = new FiniteStateMachine<>(ctx, logger);
        for (GenericTransitionBuilder<StateType, Event, Context> transition : transitions) {
            try {
                result.addState(transition.from());
                result.addState(transition.to());
                result.addEvent(transition.on());
                result.addTransition(transition.build());
            } catch (NullPointerException e) {
                String msg = e.getMessage();
                msg += "\nAttempting to build an fsm encountered an error.  From and To states are required, On event is required for transition: " + transition;
                throw new NullPointerException(msg);
            }
        }
        result.setInitialState(initialState);
        result.initialize();
        return result;
    }
    
    

    /**
     * Indicate which state is the initial state. Will throw an exception if initial is not a state
     * with a transition from it, because then the FiniteStateMachine effectively consists of a
     * single state, which is not useful
     * 
     * @param initial
     *            The state that is supposed to be the initial state of the resulting
     *            FiniteStateMachine
     */
    public void setInitialState(StateType initial) {
        if (transitions.stream().filter(t -> null != t.getFromState() && t.getFromState().equals(initial)).count() <= 0)
            throw new FiniteStateMachineException("The state " + initial.toString() + " is not found in the transitions in this builder");
        this.initialState = initial;
    }

}
