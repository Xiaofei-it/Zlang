package xiaofei.library.zlang;

import org.junit.Test;

public class ExecutorTest {
    @Test
    public void test1() throws Exception {
        Library library = new Library.Builder()
                .addFunctions("function f1(a) {if (a == 0) return 0; else return a + f1(a-1);}")
                .addFunctions("function f1(a, b) {if (a == 0) return 8*9; }")
                .addFunctions("function g(a) {return f2(a);}")
                .addFunctions("function f2(a) {s = 0; for i = 0 to a step 1 s =s + i; return s;}")
                .build();
        library.compile();
        library.print("f2", 1);
        System.out.println(library.execute("f1", new Object[]{100, 3}));
        System.out.println(library.execute("f1", new Object[]{0, 3}));
        System.out.println(library.execute("g", new Object[]{100}));
    }

    @Test
    public void test2() throws Exception {
        Library library1 = new Library.Builder()
                .addFunctions("function f(a) {g();} function g() {f(1);}")
                .build();
        Library library2 = new Library.Builder()
                .addFunctions("function f1(a) {g();}")
                .addDependency(library1)
                .build();
        library2.compile();
        library2.print("f", 1);
        library2.print("g", 0);
        library2.print("f1", 1);
    }

    @Test
    public void test3() throws Exception {
        Library library = new Library.Builder()
                .addFunctions("function f(a) {" +
                        "a = 1;" +
                        "while (a > 0) {" +
                        "  a=a*8;" +
                        "  if ((a<=10)&&(a>8)) {" +
                        "    s = 0;" +
                        "    while (s< 100) {" +
                        "      s=s+a;" +
                        "      if (s>10) break; else continue;" +
                        "    }" +
                        "  } else {a = a+1;continue;}" +
                        "  s = 100 *9;" +
                        "  if (s == 100) break;" +
                        "   f(a-1);" +
                        "}" +
                        "}")
                .build();
        library.compile();
        library.print("f", 1);
    }

    @Test
    public void test4() throws Exception {
        Library library = new Library.Builder()
                .addFunctions("function f(a) {" +
                        "s = 0;" +
                        "for i = 0 to 100 step 1 { s = s + i;if (s > 100) break; else continue;}" +
                        "return s;" +
                        "}")
                .build();
        library.compile();
        library.print("f", 1);
    }
}