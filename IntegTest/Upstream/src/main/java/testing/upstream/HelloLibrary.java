package testing.upstream;


/**
 * This is intended to be called from the downstream application to verify the dependency was resolved correctly.
 */
public class HelloLibrary
{
    public static String hello()
    {
        return "Hello Library!";
    }
}
