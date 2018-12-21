public class Synchronized_1 {
    java.lang.String a;

    public void synchronized_test() {
        synchronized (a) {
            b = c;
        }
    }
}

