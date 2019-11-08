package testing.upstream;

import org.junit.Assert;
import org.junit.Test;


public class HelloLibraryTest
{
    @Test
    public void testHelloLibrary()
    {
        Assert.assertEquals("Hello Library!", HelloLibrary.hello());
    }
}
