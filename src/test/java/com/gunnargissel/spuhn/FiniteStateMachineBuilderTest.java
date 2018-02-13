package com.gunnargissel.spuhn;

import java.util.Optional;
import java.util.function.Consumer;

import junit.framework.TestCase;



public class FiniteStateMachineBuilderTest extends TestCase {
    private FiniteStateMachineBuilder<TestState, TestEvent, String> builder;
    private FiniteStateMachine<TestState, TestEvent, String> fsm;

    public FiniteStateMachineBuilderTest(String sTestName) throws Exception {
        super(sTestName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        String ctx = "context";
        builder = new FiniteStateMachineBuilder<>(ctx,Optional.empty());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test() {
        Consumer<String> goIntermediate = arg -> {
        };
        builder.transition().from(TestState.INITIAL).to(TestState.INTERMEDIATE).on(TestEvent.BEGIN).during(goIntermediate).build();
    }

    public void testBuildDiamondStructure() {
        try {
            builder.transition().from(TestState.TOP_DIAMOND).to(TestState.LEFT_DIAMOND).on(TestEvent.LEFT);
            builder.transition().from(TestState.TOP_DIAMOND).to(TestState.RIGHT_DIAMOND).on(TestEvent.RIGHT);
            builder.transition().from(TestState.LEFT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
            builder.transition().from(TestState.RIGHT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
            builder.setInitialState(TestState.TOP_DIAMOND);
            fsm = builder.build();
        } catch (FiniteStateMachineException e) {
            fail();
        }
    }

    public void testDisconnectedStates() {
        try {
            builder.transition().from(TestState.TOP_DIAMOND).to(TestState.RIGHT_DIAMOND).on(TestEvent.RIGHT);
            builder.transition().from(TestState.LEFT_DIAMOND).to(TestState.BOTTOM_DIAMOND).on(TestEvent.DOWN);
            builder.setInitialState(TestState.TOP_DIAMOND);
            fsm = builder.build();
        } catch (FiniteStateMachineException e) {
            assertEquals(0, e.getMessage().indexOf("The following states are not reachable from the initial state: "));
            return;
        }
        fail();
    }

    public void testRefreshState() {
        try {
            builder.transition().refresh(TestState.INITIAL).on(TestEvent.REFRESH);
            builder.transition().from(TestState.INITIAL).to(TestState.END).on(TestEvent.END);
            builder.setInitialState(TestState.INITIAL);
            fsm = builder.build();
        } catch (FiniteStateMachineException e) {
            fail();
        }
    }
}
