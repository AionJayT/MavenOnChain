package testing.downstream;


/**
 * This entry-point calls the upstream library to verify that the dependency was resolved correctly.
 */
public class HelloApplication
{
    public static void main(String[] args)
    {
        System.out.println(testing.upstream.HelloLibrary.hello() + " Application!");
    }
}
