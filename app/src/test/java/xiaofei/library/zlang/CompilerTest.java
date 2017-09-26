package xiaofei.library.zlang;

import org.junit.Test;

import xiaofei.library.zlang.Compiler;

import static org.junit.Assert.*;

public class CompilerTest {
    @Test
    public void compile() throws Exception {
        Library library = new Library.Builder()
                .addFunctions("function f1(a,c) {b=(a+c)*1 + a/c/c;return a + b;}" +
                        "function f2(a) {a = 1; return a==a;}" +
                        "function f3(a) {if (a>1) return a; else return a + 1;}" +
                        "function f4(b) {a=1;while (b == 1) a= a+ 1;c= a;}" +
                        "function f5() {}")
                .build();
        library.compile();
        library.print("f1", 2);
        library.print("f2", 1);
        library.print("f3", 1);
        library.print("f4", 1);
        library.print("f5", 0);
    }
}