package org.openstreetmap.atlas.utilities.checkstyle;

/**
 * @author matthieun
 */
public class MyClass
{
    interface MyInterface
    {
        String string;

        void doSomething();
    }

    public enum MyPublicEnum
    {
        ENUM1,
        ENUM2
    }

    private enum MyPrivateEnum
    {
        ENUM1,
        ENUM2
    }

    class SomeClass
    {
        protected void doSomethingElse()
        {
            // Something else
        }
    }

    public static Integer staticField1;
    protected static Long staticField2;
    static boolean staticField3;
    private static final String staticField4;

    static
    {
        // My static block
    }

    public final int someValue1;
    protected String someValue2;
    final Boolean someValue3;
    private final String someValue4;

    public static final boolean getSomeBoolean()
    {
        return false;
    }

    static void saySomething()
    {
        System.out.println("Something!");
    }

    private static void doNothing()
    {
    }

    {
        // My initializer block
    }

    public MyClass()
    {
        this.someValue1 = 0;
        this.someValue3 = true;
        this.someValue4 = "Yes";
    }

    private MyClass()
    {
        this.someValue1 = 0;
        this.someValue3 = true;
        this.someValue4 = "Yes";
    }

    public void methodA()
    {
    }

    public void methodB()
    {
    }

    protected void methodC()
    {
    }

    protected void methodD()
    {
    }

    void methodE()
    {
    }

    void methodF()
    {
    }

    private void methodG()
    {
    }

    private void methodH()
    {
    }
}
