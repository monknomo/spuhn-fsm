package com.gunnargissel.spuhn;

public class TestContext {
    String state = "";

    TestContext(String state) {
        this.state = state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }
}
