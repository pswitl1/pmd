
public class CWE398_Poor_Code_Quality__empty_case_01 extends AbstractTestCase
{
    public void bad() throws Throwable
    {
        switch (x)
        {
            /* FLAW: An empty case statement has no effect */
            case 2:
                IO.writeLine("Inside the case statement");
                break;
            case 1:
            case 0:
                break;
            default:
                IO.writeLine("Inside the default statement");
                break;

        }

    }
}

