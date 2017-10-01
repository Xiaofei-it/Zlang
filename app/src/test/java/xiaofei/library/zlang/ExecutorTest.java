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
                .addFunctions("function f2(a, b) {return a + b;}")
                .addFunctions("function f3(a) {return f2(a + 1, a - 1);}")
                .build();
        library.print("f3", 1);
        System.out.println(library.execute("f1", new Object[]{100, 3}));
        System.out.println(library.execute("f1", new Object[]{0, 3}));
        System.out.println(library.execute("g", new Object[]{100}));
        System.out.println(library.execute("f3", new Object[]{100}));
    }

    @Test
    public void test2() throws Exception {
        Library library = new Library.Builder()
                .addFunctions("function f(a) {b = _test_add(a, a - 1); return _test_add(b, a/3, a);}")
                .build();
        library.print("f", 1);
        System.out.println(library.execute("f", new Object[]{100}));//199 + 100 + 33
    }

    @Test
    public void test3() throws Exception {
        Library library = new Library.Builder()
                .addFunctions("function f(a) {if (a) return \"t\\\"\"; else return \"j\" + 'k';}")
                .addFunctions("function f1(a) {if (a == true) return null; else return 2;}")
                .addFunctions("function f2(a) {if (a) return 't'; else return 1;}")
                .build();
        library.print("f1", 1);
        System.out.println(library.execute("f1", new Object[]{true}));
        System.out.println(library.execute("f1", new Object[]{false}));
        System.out.println(library.execute("f2", new Object[]{true}));
        System.out.println(library.execute("f2", new Object[]{false}));
        System.out.println(library.execute("f", new Object[]{true}));
        System.out.println(library.execute("f", new Object[]{false}));
    }

    private static final class TestA {
        String f(String s) {
            return "s=" + s;
        }
    }
    @Test
    public void test4() {
        Library library = new Library.Builder()
                .addFunctions(
                        "function new_instance(className) {" +
                                "class = _for_name(className); if (class == null) return null;" +
                                "cons = _class_get_declared_constructor(class);" +
                                "return _constructor_new_instance(cons);}")
                .addFunctions(
                        "function call_function(className) {" +
                                "class = _for_name(className);" +
                                "object = new_instance(className);" +
                                "method = _class_get_declared_method(class, \"f\", _for_name(\"java.lang.String\"));" +
                                "return _method_invoke(method, object, \"kkk\");}"
                )
                .build();
        library.print("new_instance", 1);
        System.out.println(library.execute("new_instance", new Object[]{TestA.class.getName()}));
        System.out.println(library.execute("call_function", new Object[]{TestA.class.getName()}));
    }
}