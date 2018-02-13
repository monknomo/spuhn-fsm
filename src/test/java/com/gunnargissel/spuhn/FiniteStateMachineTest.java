package com.gunnargissel.spuhn;

import junit.framework.TestCase;

import java.util.Optional;

public class FiniteStateMachineTest extends TestCase {

    private FiniteStateMachine<TestState, TestEvent, String> fsm;
    String ctx = "context";
    private FiniteStateMachineBuilder<TestState, TestEvent, String> builder;
    private FiniteStateMachineBuilder<TestHookState, TestEvent, TestContext> hookBuilder;
    private TestContext hookCtx;
    private FiniteStateMachine<TestHookState, TestEvent, TestContext> hookFsm = null;

    public FiniteStateMachineTest(String sTestName) throws Exception {
        super(sTestName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        fsm = null;
        builder = new FiniteStateMachineBuilder<>(ctx, Optional.empty());
        hookCtx = new TestContext("test");
        hookBuilder = new FiniteStateMachineBuilder<>(hookCtx, Optional.empty());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test to ensure that an exception is thrown when there is not initial state set in the builder
     */
    public void testInitialStateRequired() {
        builder.transition().from(TestState.INITIAL).to(TestState.END).on(TestEvent.END);
        try {
            builder.build();
        } catch (NullPointerException npe) {
            return;
        }
        fail();
    }

    /**
     * Test to ensure the builder will build a minimal fsm and initialize it to the specified
     * initial state
     */
    public void testBuilderInitializes() {
        builder.transition().from(TestState.INITIAL).to(TestState.END).on(TestEvent.END);
        builder.setInitialState(TestState.INITIAL);
        fsm = builder.build();
        assertTrue(fsm.isCurrentState(TestState.INITIAL));
    }

    /**
     * test to ensure fsm will transition between expected states in simplest case
     */
    public void testLinearEventHandling() {
        builder.transition().from(TestState.INITIAL).to(TestState.END).on(TestEvent.END);
        builder.setInitialState(TestState.INITIAL);
        fsm = builder.build();
        assertTrue(fsm.isCurrentState(TestState.INITIAL));
        fsm.fire(TestEvent.END);
        assertTrue(fsm.isCurrentState(TestState.END));
    }

    /**
     * Test to ensure exception is thrown when unknown event is fired at fsm
     */
    public void testBadEvent() {
        builder.transition().from(TestState.INITIAL).to(TestState.END).on(TestEvent.END);
        builder.setInitialState(TestState.INITIAL);
        fsm = builder.build();
        assertTrue(fsm.isCurrentState(TestState.INITIAL));
        try {
            fsm.fire(TestEvent.LEFT);
        } catch (FiniteStateMachineException e) {
            assertTrue(fsm.isCurrentState(TestState.INITIAL));
            assertEquals("invalid initial event: LEFT not found in internal events", e.getMessage());
            return;
        }
        fail();
    }

    /**
     * test to ensure simplest state can be drawn with graphviz dot notation
     */
    public void testSimpleGraphvizDotNotation() {
        builder.transition().from(TestState.INITIAL).to(TestState.END).on(TestEvent.END);
        builder.setInitialState(TestState.INITIAL);
        fsm = builder.build();
        assertEquals("digraph g {\nINITIAL -> END [label=\"END\"];\n}", fsm.getGraphvizDotRepresentation());
    }

    /**
     * testing drawing more complex graphviz dot notation
     */
    public void testDiamondGraphvizDotNotation() {
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.LEFT_DIAMOND).on(TestEvent.LEFT);
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.RIGHT_DIAMOND).on(TestEvent.RIGHT);
        builder.transition().from(TestState.LEFT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
        builder.transition().from(TestState.RIGHT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
        builder.setInitialState(TestState.TOP_DIAMOND);
        fsm = builder.build();
        String graph = fsm.getGraphvizDotRepresentation();
        assertTrue(graph.contains("digraph g {"));
        assertTrue(graph.contains("LEFT_DIAMOND -> BOTTOM_DIAMOND [label=\"DOWN\"];"));
        assertTrue(graph.contains("TOP_DIAMOND -> RIGHT_DIAMOND [label=\"RIGHT\"];"));
        assertTrue(graph.contains("TOP_DIAMOND -> LEFT_DIAMOND [label=\"LEFT\"];"));
        assertTrue(graph.contains("RIGHT_DIAMOND -> BOTTOM_DIAMOND [label=\"DOWN\"];"));
        assertTrue(graph.contains("}"));
        // System.out.println(graph);
    }

    /**
     * testing drawing graphviz dot notation when there are multiple transitions between the same
     * states
     */
    public void testMultiDiamondGraphvizDotNotation() {
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.LEFT_DIAMOND).on(TestEvent.LEFT);
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.RIGHT_DIAMOND).on(TestEvent.RIGHT);
        builder.transition().from(TestState.LEFT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
        builder.transition().from(TestState.RIGHT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.LEFT_DIAMOND).on(TestEvent.LEFTLEFT);
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.RIGHT_DIAMOND).on(TestEvent.RIGHTRIGHT);
        builder.transition().from(TestState.LEFT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWNDOWN);
        builder.transition().from(TestState.RIGHT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWNDOWN);

        builder.setInitialState(TestState.TOP_DIAMOND);
        fsm = builder.build();
        String graph = fsm.getGraphvizDotRepresentation();
        // System.out.println(graph);
        assertTrue(graph.contains("digraph g {"));
        assertTrue(graph.contains("LEFT_DIAMOND -> BOTTOM_DIAMOND [label=\"DOWN\"];"));
        assertTrue(graph.contains("TOP_DIAMOND -> RIGHT_DIAMOND [label=\"RIGHT\"];"));
        assertTrue(graph.contains("TOP_DIAMOND -> LEFT_DIAMOND [label=\"LEFT\"];"));
        assertTrue(graph.contains("RIGHT_DIAMOND -> BOTTOM_DIAMOND [label=\"DOWN\"];"));

        assertTrue(graph.contains("LEFT_DIAMOND -> BOTTOM_DIAMOND [label=\"DOWNDOWN\"];"));
        assertTrue(graph.contains("TOP_DIAMOND -> RIGHT_DIAMOND [label=\"RIGHTRIGHT\"];"));
        assertTrue(graph.contains("TOP_DIAMOND -> LEFT_DIAMOND [label=\"LEFTLEFT\"];"));
        assertTrue(graph.contains("RIGHT_DIAMOND -> BOTTOM_DIAMOND [label=\"DOWNDOWN\"];"));
        assertTrue(graph.contains("}"));
    }

    /**
     * test creating dot notation from fsm with a loop in it
     */
    public void testLoopGraphvizDotNotation() {
        builder.transition().from(TestState.INITIAL).to(TestState.END).on(TestEvent.END);
        builder.transition().from(TestState.END).to(TestState.INITIAL).on(TestEvent.BEGIN);
        builder.setInitialState(TestState.INITIAL);
        fsm = builder.build();
        String graph = fsm.getGraphvizDotRepresentation();
        // System.out.println(graph);
        assertTrue(graph.contains("digraph g {"));
        assertTrue(graph.contains("INITIAL -> END [label=\"END\"];"));
        assertTrue(graph.contains("END -> INITIAL [label=\"BEGIN\"];"));
        assertTrue(graph.contains("}"));
    }

    /**
     * Test to ensure exception is thrown when a valid event is fired at the fsm, but there is no
     * transition for that event from the current state
     */
    public void testNoTransitionForEvent() {
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.LEFT_DIAMOND).on(TestEvent.LEFT);
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.RIGHT_DIAMOND).on(TestEvent.RIGHT);
        builder.transition().from(TestState.LEFT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
        builder.transition().from(TestState.RIGHT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
        builder.setInitialState(TestState.TOP_DIAMOND);
        fsm = builder.build();
        assertTrue(fsm.isCurrentState(TestState.TOP_DIAMOND));
        try {
            fsm.fire(TestEvent.DOWN);
        } catch (FiniteStateMachineException e) {
            assertTrue(fsm.isCurrentState(TestState.TOP_DIAMOND));
            assertEquals("No transition exists from current state: TOP_DIAMOND for the event: DOWN", e.getMessage());
            return;
        }
        fail();
    }

    /**
     * test to ensure that the fsm can handle events when the events cause the fsm states to loop
     * 
     * @throws Exception when something breaks
     */
    public void testLoopEventHandling() throws Exception {
        builder.transition().from(TestState.INITIAL).to(TestState.END).on(TestEvent.END);
        builder.transition().from(TestState.END).to(TestState.INITIAL).on(TestEvent.BEGIN);
        builder.setInitialState(TestState.INITIAL);
        fsm = builder.build();

        assertTrue(fsm.isCurrentState(TestState.INITIAL));
        for (int i = 0; i < 5; i++) {
            if (i % 2 == 1) {
                fsm.fire(TestEvent.BEGIN);
                assertTrue(fsm.isCurrentState(TestState.INITIAL));
            } else {
                fsm.fire(TestEvent.END);
                assertTrue(fsm.isCurrentState(TestState.END));
            }
        }
    }

    /**
     * Test to ensure that two connected loops can be created by the fsm builder
     */
    public void testFigureEightLoop() {
        builder.transition().from(TestState.INITIAL).to(TestState.INTERMEDIATE).on(TestEvent.BEGIN);
        builder.transition().from(TestState.INTERMEDIATE).to(TestState.END).on(TestEvent.END);
        builder.transition().from(TestState.INTERMEDIATE).to(TestState.INITIAL).on(TestEvent.CANCEL);
        builder.transition().from(TestState.END).to(TestState.INTERMEDIATE).on(TestEvent.CANCEL);
        builder.setInitialState(TestState.INITIAL);
        fsm = builder.build();
    }

    /**
     * Test to ensure graphviz dot notation can be produced for an fsm with two connected loops
     */
    public void testFigureEightLoopGraphvizDot() {
        builder.transition().from(TestState.INITIAL).to(TestState.INTERMEDIATE).on(TestEvent.BEGIN);
        builder.transition().from(TestState.INTERMEDIATE).to(TestState.END).on(TestEvent.END);
        builder.transition().from(TestState.INTERMEDIATE).to(TestState.INITIAL).on(TestEvent.CANCEL);
        builder.transition().from(TestState.END).to(TestState.INTERMEDIATE).on(TestEvent.CANCEL);
        builder.setInitialState(TestState.INITIAL);
        fsm = builder.build();
        String graph = fsm.getGraphvizDotRepresentation();
        // System.out.println(graph);
        assertTrue(graph.contains("digraph g {"));
        assertTrue(graph.contains("INITIAL -> INTERMEDIATE [label=\"BEGIN\"];"));
        assertTrue(graph.contains("END -> INTERMEDIATE [label=\"CANCEL\"];"));
        assertTrue(graph.contains("INTERMEDIATE -> INITIAL [label=\"CANCEL\"];"));
        assertTrue(graph.contains("INTERMEDIATE -> END [label=\"END\"];"));
        assertTrue(graph.contains("}"));

    }

    /**
     * Test transitions for an fsm with a diamond shape
     */
    public void testDiamondTransition() {
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.LEFT_DIAMOND).on(TestEvent.LEFT);
        builder.transition().from(TestState.TOP_DIAMOND).to(TestState.RIGHT_DIAMOND).on(TestEvent.RIGHT);
        builder.transition().from(TestState.LEFT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
        builder.transition().from(TestState.RIGHT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
        builder.setInitialState(TestState.TOP_DIAMOND);
        fsm = builder.build();
        assertTrue(fsm.isCurrentState(TestState.TOP_DIAMOND));
        fsm.fire(TestEvent.LEFT);
        assertTrue(fsm.isCurrentState(TestState.LEFT_DIAMOND));
        try {
            fsm.fire(TestEvent.RIGHT);
        } catch (FiniteStateMachineException e) {
            // expected, continue
        } catch (Exception e) {
            fail();
        }
        fsm.fire(TestEvent.DOWN);
        assertTrue(fsm.isCurrentState(TestState.BOTTOM_DIAMOND));

        fsm.initialize();

        assertTrue(fsm.isCurrentState(TestState.TOP_DIAMOND));
        fsm.fire(TestEvent.RIGHT);
        assertTrue(fsm.isCurrentState(TestState.RIGHT_DIAMOND));
        fsm.fire(TestEvent.DOWN);
        assertTrue(fsm.isCurrentState(TestState.BOTTOM_DIAMOND));
    }

    /**
     * test to ensure that functions are fired during a transition
     * 
     * @throws Exception when something breaks
     */
    public void testTransitionFunctionHook() throws Exception {
        hookBuilder.transition().from(TestHookState.INITIAL).to(TestHookState.NO_ENTRY_HOOK).on(TestEvent.END).during(ctx -> ctx.setState("transitions rule"));
        hookBuilder.setInitialState(TestHookState.INITIAL);
        hookFsm = hookBuilder.build();
        assertEquals("test", hookCtx.getState());
        assertTrue(hookFsm.isCurrentState(TestHookState.INITIAL));
        hookFsm.fire(TestEvent.END);
        assertEquals("transitions rule", hookCtx.getState());
        assertTrue(hookFsm.isCurrentState(TestHookState.NO_ENTRY_HOOK));
    }

    /**
     * Test to ensure that entering a transition triggers the state entry method
     */
    public void testTransitionStateEntryHook() throws Exception {
        hookBuilder.transition().from(TestHookState.INITIAL).to(TestHookState.ENTRY_HOOK).on(TestEvent.END).during(ctx -> ctx.setState("transitions rule"));
        hookBuilder.setInitialState(TestHookState.INITIAL);
        hookFsm = hookBuilder.build();
        assertEquals("test", hookCtx.getState());
        assertTrue(hookFsm.isCurrentState(TestHookState.INITIAL));
        hookFsm.fire(TestEvent.END);
        assertEquals("foobar", hookCtx.getState());
        assertTrue(hookFsm.isCurrentState(TestHookState.ENTRY_HOOK));
    }

    public void testTransitionToSameState() {
        builder.transition().refresh(TestState.INITIAL).on(TestEvent.REFRESH);
        builder.transition().from(TestState.INITIAL).to(TestState.END).on(TestEvent.END);
        builder.setInitialState(TestState.INITIAL);
        fsm = builder.build();
        assertTrue(fsm.isCurrentState(TestState.INITIAL));
        fsm.fire(TestEvent.REFRESH);
        assertTrue(fsm.isCurrentState(TestState.INITIAL));
        fsm.fire(TestEvent.END);
        assertTrue(fsm.isCurrentState(TestState.END));
    }
    
    public void testTransitionToSameStateFiresHook(){
        hookBuilder.transition().from(TestHookState.ENTRY_HOOK).to(TestHookState.NO_ENTRY_HOOK).on(TestEvent.END);
        hookBuilder.transition().refresh(TestHookState.ENTRY_HOOK).on(TestEvent.REFRESH);
        hookBuilder.setInitialState(TestHookState.ENTRY_HOOK);
        hookFsm = hookBuilder.build();
        assertTrue(hookFsm.isCurrentState(TestHookState.ENTRY_HOOK));
        assertEquals("test",hookCtx.getState());
        hookFsm.fire(TestEvent.REFRESH);
        assertEquals("foobar", hookCtx.getState());
        hookCtx.setState("reset");
        assertEquals("reset", hookCtx.getState());
        hookFsm.fire(TestEvent.END);
        assertEquals("reset", hookCtx.getState());
    }
}
