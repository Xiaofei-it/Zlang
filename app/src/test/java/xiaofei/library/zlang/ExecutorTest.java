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

    @Test
    public void test5() {
        Library library = new Library.Builder()
                .addFunctions(
                        "function check(array, x) {" +
                                "len = _array_length(array);" +
                                "for i = 0 to len - 1 step 1 {" +
                                "  if (_equal(_array_get(array, i), x)) {" +
                                "      return i;" +
                                "  }" +
                                "}" +
                                "  return -1;" +
                                "}")
                .addFunctions(
                        "function plus(array) {" +
                                "len = _array_length(array);" +
                                "result = \"\";" +
                                "for i = 0 to len - 1 step 1 {" +
                                "  result = result + _array_get(array,i);" +
                                "  }" +
                                "  return result;" +
                                "}")
                .build();
        library.print("check", 2);
        System.out.println(library.execute("check", new Object[]{new String[]{"ab", "cd", "e"}, "e"}));
        System.out.println(library.execute("check", new Object[]{new String[]{"ab", "cd", "e"}, "abc"}));
        library.print("plus", 1);
        System.out.println(library.execute("plus", new Object[]{new String[]{"ab", "cd", "e"}}));
    }

    @Test
    public void test6() {
        JavaLibrary javaLibrary = new JavaLibrary();
        javaLibrary.addFunction(new JavaFunction() {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "test";
            }

            @Override
            public Object call(Object[] input) {
                return input[0].hashCode();
            }
        });
        try {
            Library library = new Library.Builder()
                    .addJavaDependency(javaLibrary)
                    .addFunctions(
                            "function check(x) {" +
                                    "if (x != null && test(x) > 0 && test(x) > 0) return 1; else return -1;}")
                    .build();
            library.print("check", 1);
            System.out.println(library.execute("check", new Object[]{"e"}));
            System.out.println(library.execute("check", new Object[]{null}));
        } catch (CompileException e) {
            System.out.println(e.error + " " + e.info);
        }

    }
}