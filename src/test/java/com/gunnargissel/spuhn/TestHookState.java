package com.gunnargissel.spuhn;

public enum TestHookState implements State<TestContext>  {
    INITIAL {
        public void enter(TestContext ctx) {
        }
    },
    ENTRY_HOOK {
        public void enter(TestContext ctx) {
            ctx.setState("foobar");
        }
    },
    NO_ENTRY_HOOK {
        public void enter(TestContext ctx) {
        }
    }
}
