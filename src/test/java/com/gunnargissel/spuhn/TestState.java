package com.gunnargissel.spuhn;


public enum TestState implements State<String> {
    INITIAL {
        public void enter(String ctx) {
        }
    },
    INTERMEDIATE {
        public void enter(String ctx) {
        }
    },
    TOP_DIAMOND {
        public void enter(String ctx) {
        }
    },
    LEFT_DIAMOND {
        public void enter(String ctx) {
        }
    },
    RIGHT_DIAMOND {
        public void enter(String ctx) {
        }
    },
    BOTTOM_DIAMOND {
        public void enter(String ctx) {
        }
    },
    END {
        public void enter(String ctx) {
        }
    },
    TASK_1_START{
        public void enter(String ctx) {
        }
    },
    TASK_1_END{
        public void enter(String ctx) {
        }
    },
    TASK_2_START{
        public void enter(String ctx) {
        }
    },
    TASK_3_END{
        public void enter(String ctx) {
        }
    },
    WAITING_1{
        public void enter(String ctx) {
        }
    },
    WAITING_2{
        public void enter(String ctx) {
        }
    }
}
