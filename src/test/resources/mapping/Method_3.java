package mapping;

public class Method_3 {

    java.lang.String a;

    public void testOverride() {
        doSomething(a);
    }

    public void doSomething(java.lang.String expected) {
        java.util.Date date = new java.util.Date();
    }

    private static class InnerClass extends Method_3 {

        @Override
        public void doSomething(java.lang.String expectedOverriden) {
            java.lang.String someString = new java.lang.String();
        }
    }
}

