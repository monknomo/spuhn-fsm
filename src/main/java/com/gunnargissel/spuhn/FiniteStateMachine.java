package com.gunnargissel.spuhn;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Finite State Machines are used to define a map of transitions between states. This implementation
 * defines such a map and provides events upon transitioning between states and entering a new
 * state. The events have access to a context object, which provides a handle into external
 * entities.
 * 
 * The design of this class assumes you will create your own State, Event and Context types. The
 * assumption is that you will create enums implementing the State and Context types. A simple enum
 * (or even arbitrary Strings) is sufficient for an Event type. This library does not provide an
 * interface for Events because there is no need for an event to do anything beyond trigger a
 * transition between states.
 * 
 * This class is not intended to be created directly. Instead, use the FiniteStateMachineBuilder to
 * create a FiniteStateMachine out of a series of transitions between states.
 *
 * @author gunnar.gissel
 *
 * @param <StateType>
 *            A type enumerating all the possible states for this FiniteStateMachine
 * @param <Event>
 *            A type enumerating all the possible events for this FiniteStateMachine to trigger
 *            transitions between states
 * @param <Context>
 *            A type used to give transitions between states a handle into some external context
 *
 */
public class FiniteStateMachine<StateType extends State<Context>, Event, Context> {
    private static Logger logger = Logger.getLogger(FiniteStateMachine.class.getName());
    /*
     * The key is a fromState, and the value is a hashmap where the keys are events for that
     * fromState and the values are the corresponding transition. You look up a state and you get
     * all the events for that state with the corresponding transition
     */
    private HashMap<StateType, HashMap<Event, Transition<StateType, Event, Context>>> transitionTable = new HashMap<>();
    private Set<StateType> states = new HashSet<>();
    private Set<Event> events = new HashSet<>();
    private StateType currentState;
    private StateType initialState;
    private Context ctx;


    /**
     * Constructor for FiniteStateMachine
     * 
     * @param ctx
     *            a Context that will be passed into each transition and final state event
     * @param logger
     *            an Optional<Logger> that will override the internal logger with an externally provided one
     */
    protected FiniteStateMachine(Context ctx, Optional<Logger> logger) {
        this.ctx = ctx;
        logger.ifPresent(logger1 -> FiniteStateMachine.logger = logger1);
    }

    protected boolean addStates(Collection<StateType> states) {
        Objects.requireNonNull(states);
        return this.states.addAll(states);
    }

    protected void addState(StateType state) {
        Objects.requireNonNull(state);
        states.add(state);
    }

    protected boolean addEvents(Collection<Event> events) {
        Objects.requireNonNull(events);
        return this.events.addAll(events);
    }

    protected void addEvent(Event event) {
        Objects.requireNonNull(event);
        events.add(event);
    }

    @SuppressWarnings("unchecked")
    private boolean isDisconnectedStates(Set<StateType> allStates) {
        walk(initialState, allStates);
        return allStates.size() != 0;
    }

    private void walk(StateType s, Set<StateType> remainingStates) {
        if (!remainingStates.contains(s))
            return;
        remainingStates.remove(s);
        HashMap<Event, Transition<StateType, Event, Context>> transitions = transitionTable.get(s);
        if (null != transitions) {
            for (Event e : transitions.keySet()) {
                logger.finest("FROM: " + transitions.get(e).getFromState() + " TO: " + transitions.get(e).getToState() + " ON: " + transitions.get(e).getOnEvent());
                walk(transitions.get(e).getToState(), remainingStates);
            }
        }
    }

    /**
     * Graphviz dot notation is a notation that can be used to create a graphical representation of
     * a directed or undirected graph. In the case of a state machine, we have a directed graph.
     * There are many programs that will display a picture when given dot notation - here's an
     * online one: http://viz-js.com/
     * 
     * @return a string with a graphviz dot representation of the finite state machine
     */
    public String getGraphvizDotRepresentation() {
        List<String> blah = new ArrayList<>();
        // walk(initialState, allStates, blah);
        for (StateType s : transitionTable.keySet()) {
            HashMap<Event, Transition<StateType, Event, Context>> eventTransitions = transitionTable.get(s);
            if (null != eventTransitions) {
                for (Event e : eventTransitions.keySet()) {
                    Transition<StateType, Event, Context> t = eventTransitions.get(e);
                    String dotTransition = t.getFromState() + " -> " + t.getToState() + " [label=\"" + t.getOnEvent() + "\"];";
                    blah.add(dotTransition);
                }
            }
        }
        return "digraph g {\n" + blah.stream().collect(Collectors.joining("\n")) + "\n}";
    }

    /**
     * Returns true if this currentState is state
     * 
     * @param state
     *            A state
     * @return true if this currentState is state
     */
    public boolean isCurrentState(StateType state) {
        return currentState.equals(state);
    }

    /**
     * Adds a transition to this finite state machine
     * @param transition The Transition to be added to the transition table
     * @return This FiniteStateMachine
     */
    protected FiniteStateMachine<StateType, Event, Context> addTransition(Transition<StateType, Event, Context> transition) {
        Objects.requireNonNull(transition);
        if (transitionTable.containsKey(transition.getFromState())) {
            // the startState already exists, so set the event transition for
            // startState to this transition
            transitionTable.get(transition.getFromState()).put(transition.getOnEvent(), transition);
        } else {
            // the startState does not exist, set up an event transition for it
            HashMap<Event, Transition<StateType, Event, Context>> eventMap = new HashMap<>();
            eventMap.put(transition.getOnEvent(), transition);
            transitionTable.put(transition.getFromState(), eventMap);
        }
        return this;
    }

    /**
     * Fire an event that cause this FiniteStateMachine to transition from its current state a new
     * state defined in this FiniteStateMachine's transition table. Transitioning from one state to
     * another will cause the perform method on the transition to fire and the enter method on the
     * final state to fire
     * 
     * @param event
     *            an event corresponding to a transition from the current state to a new state
     * @throws FiniteStateMachineException
     *             when there is not a transition from the current state, or because the final state
     *             is not found, or the event being fired is not registered with this
     *             FiniteStateMachine
     */
    public void fire(Event event) throws FiniteStateMachineException {
        logger.info("current state: " + currentState);
        logger.info("event: " + event);
        if (events.contains(event)) {
            HashMap<Event, Transition<StateType, Event, Context>> eventTransitions = transitionTable.get(currentState);
            Transition<StateType, Event, Context> transition = eventTransitions.get(event);
            if (null != transition) {
                logger.info(transition.toString());
                if (currentState != transition.getFromState() || !states.contains(transition.getFromState())) {
                    throw new FiniteStateMachineException("Transition: " + transition + " not valid from state: " + currentState);
                }
                if (!states.contains(transition.getToState())) {
                    throw new FiniteStateMachineException("Transition: " + transition + " not valid because state: " + transition.getToState() + " is not found");
                }
                transition.transit(ctx);
                currentState = transition.getToState();
                currentState.enter(ctx);
                logger.info("Transition complete: " + transition.toString());
            } else {
                throw new FiniteStateMachineException("No transition exists from current state: " + currentState + " for the event: " + event);
            }
        } else {
            throw new FiniteStateMachineException("invalid initial event: " + event + " not found in internal events");
        }
    }

    /**
     * sets the initial state of the finite state machine so it can be initialized and reinitialized
     * @param state The desired initial state of this FiniteStateMachine
     * @throws FiniteStateMachineException when the desired initial state is not found
     */
    protected void setInitialState(StateType state) throws FiniteStateMachineException {
        Objects.requireNonNull(state);
        if (states.contains(state)) {
            initialState = state;
        } else {
            throw new FiniteStateMachineException("invalid initial state: " + state + " not found in internal states");
        }
    }

    /**
     * Resets this FiniteStateMachine's current state to the initial state
     */
    public void initialize() {
        this.currentState = initialState;
        Set<StateType> allStates = states.stream().collect(Collectors.toSet());
        if (isDisconnectedStates(allStates)) {
            StringBuilder stateList = new StringBuilder();
            for (StateType s : allStates) {
                stateList.append(s);
                stateList.append(", ");
            }
            stateList = new StringBuilder(stateList.substring(0, stateList.length() - 2));
            throw new FiniteStateMachineException("The following states are not reachable from the initial state: " + stateList);
        }
    }
}
