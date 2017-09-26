package xiaofei.library.zlangtest;

import org.junit.Test;

import xiaofei.library.zlang.Compiler;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        Compiler compiler = new Compiler(null);
    }
}