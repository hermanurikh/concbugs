public class Complex {

    private int counter;

    public void test1(Date other) throws InterruptedException {
        Complex a; //declaration
        a = new Complex(); //declaration
        Complex b = new Complex(); //declaration
        b = a; //cross assignment
        //declaration
        //cross assignment
        //cross assignment
        a = b = a = new ComplexImpl();
        //inner assignment
        int c = a.counter;
        //inner assignment
        c = b.counter;
        //branch statement with 2 method statements
        if (true) {
            test2("", other);
        } else {
            test2("second", new Date());
        }

        //branch statement with 1 method statements
        if (false) {
            test2("", other);
        }

        //branch statement with 1 method statement
        if (true) {
            test2("", other);
        } else {

        }

        //synchronized statement with method statement

        synchronized (a) {
            System.out.println("lil");
            System.out.println("lol");
            test2("lil", other);
        }

        //method statement
        other  = test2("ll", other);

        //method statement
        //declaration statement
        Date newDate = test2("f", other);

        //wait statement
        a.wait();

        //3 declaration statements?
        int ab, ac, ad = 4;

        //nothing
        d = Integer.parseInt("4");

        //nothing
        for (int i = 0; i < 2; i++) {
            System.out.println("lil2");
        }

        //nothing
        do {
            System.out.println("hehe");
        } while (d == 4);

        //method statement with synchronized statement inside
        while(true) {
            newDate = getDate();
        }
    }

    private static Date test2(String a, Date b) {
        c = d;
        return null;
    }

    private synchronized Date getDate() {
        a = b;
        return null;
    }

    private static class ComplexImpl extends Complex {

        @Override
        public void test1(Date other) throws InterruptedException {
            System.out.println("lololo");
        }
    }
}

