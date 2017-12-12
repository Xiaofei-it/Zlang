/**
 *
 * Copyright 2011-2017 Xiaofei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xiaofei.library.zlang;

import org.junit.Assert;
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

//    @Test
//    public void test2() throws Exception {
//        Library library = new Library.Builder()
//                .addFunctions("function f(a) {b = _test_add(a, a - 1); return _test_add(b, a/3, a);}")
//                .build();
//        library.print("f", 1);
//        System.out.println(library.execute("f", new Object[]{100}));//199 + 100 + 33
//    }

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

//    private static final class TestA {
//        String f(String s) {
//            return "s=" + s;
//        }
//    }
//    @Test
//    public void test4() {
//        Library library = new Library.Builder()
//                .addFunctions(
//                        "function new_instance(className) {" +
//                                "class = _for_name(className); if (class == null) return null;" +
//                                "cons = _class_get_declared_constructor(class);" +
//                                "return _constructor_new_instance(cons);}")
//                .addFunctions(
//                        "function call_function(className) {" +
//                                "class = _for_name(className);" +
//                                "object = new_instance(className);" +
//                                "method = _class_get_declared_method(class, \"f\", _for_name(\"java.lang.String\"));" +
//                                "return _method_invoke(method, object, \"kkk\");}"
//                )
//                .build();
//        library.print("new_instance", 1);
//        System.out.println(library.execute("new_instance", new Object[]{TestA.class.getName()}));
//        System.out.println(library.execute("call_function", new Object[]{TestA.class.getName()}));
//    }

    @Test
    public void test5() {
        Library library = new Library.Builder()
                .addFunctions(
                        "function check(array, x) {" +
                                "len = _length(array);" +
                                "for i = 0 to len - 1 step 1 {" +
                                "  if (_equal(array[i], x)) {" +
                                "      return i;" +
                                "  }" +
                                "}" +
                                "  return -1;" +
                                "}")
                .addFunctions(
                        "function plus(array) {" +
                                "len = _length(array);" +
                                "result = \"\";" +
                                "for i = 0 to len - 1 step 1 {" +
                                "  result = result + array[i];" +
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
        JavaLibrary javaLibrary = new JavaLibrary.Builder().addFunction(new JavaFunction() {
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
        }).build();
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
            System.out.print(e);
        }

    }

    @Test
    public void test7() {
        JavaLibrary javaLibrary = new JavaLibrary.Builder().addFunction(new JavaFunction() {
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
        }).build();
        try {
            Library library = new Library.Builder()
                    .addJavaDependency(javaLibrary)
                    .addFunctions(
                            "function check(x) {" +
                                    "if (x == null || x != null && test(x) > 0 || x != null && test(x) < 0) return 1; else return -1;}")
                    .build();
            library.print("check", 1);
            System.out.println(library.execute("check", new Object[]{"e"}));
            System.out.println(library.execute("check", new Object[]{null}));
        } catch (CompileException e) {
            System.out.println(e);
        }

    }

    @Test
    public void test8() {
        Library library = new Library.Builder()
                .addFunctions("function f(a) {a[9-8][2][3] = 4 * 5; b = 3; return a[b-2][8-6][b] + 1;}")
                .addFunctions("function g(a) {" +
                        "sum = 0; for i = 0 to 3 step 1 for j = 0 to 4 step 1 for k = 0 to 2 step 1 {a[i][j][k] = i * j * k; sum = sum + a[i][j][k];} return sum;" +
                        "}")
                .build();
        library.print("f", 1);
        library.print("g", 1);
        System.out.println(library.execute("f", new Object[]{new int[2][3][4]}));
        int[][][] a = new int[4][5][3];
        int tmp = (int) library.execute("g", new Object[]{a});
        int sum = 0;
        for (int i = 0; i <= 3; ++i) {
            for (int j = 0; j <= 4; ++j) {
                for (int k = 0; k <=2; ++k) {
                    sum += a[i][j][k];
                    System.out.println(i + " " + j + " "  + k + " "  + a[i][j][k]);
                }
            }
        }
        System.out.println(tmp);
        Assert.assertEquals(tmp, sum);
    }

    @Test
    public void test9() {
        Library library = new Library.Builder()
                .addFunctions("function f(a) {_println(a); _print(a); _println();}")
                .build();
        System.out.println(library.execute("f", new Object[]{null}));
        System.out.println(library.execute("f", new Object[]{1}));
        System.out.println(library.execute("f", new Object[]{1.2}));
    }

    private static class A {
        int f;
        public int f2;
        int f1(int j) {
            return j + 1;
        }
        int f1(int i, int j) {
            return i + j + 1;
        }
        public int f2(int j) {
            return j + 1;
        }
        public int f2(int i, int j) {
            return i + j + 1;
        }
    }
    private static class B extends A {
        private B(int g) {
            this.g = g;
        }
        int g;
        public int g2;
        int g1(int j) {
            return j;
        }
        int g1(int i, int j) {
            return i + j;
        }

        public int g2(int j) {
            return j;
        }
        public int g2(int i, int j) {
            return i + j;
        }
    }
    private static class A2 {
        static int f;
        public static int f2;
        static int f1(int j) {
            return j + 1;
        }
        static int f1(int i, int j) {
            return i + j + 1;
        }
        public static int f2(int j) {
            return j + 1;
        }
        public static int f2(int i, int j) {
            return i + j + 1;
        }
    }
    private static class B2 extends A2 {
        private B2(int g) {
            this.g = g;
        }
        static int g;
        public static int g2;
        static int g1(int j) {
            return j;
        }
        static int g1(int i, int j) {
            return i + j;
        }

        public static int g2(int j) {
            return j;
        }
        public static int g2(int i, int j) {
            return i + j;
        }
    }
    @Test
    public void test10() {
        Library library = new Library.Builder()
                .addFunctions("function f() {b = _new_instance(\"" + B.class.getName() + "\", _new_instance(\"java.lang.Integer\",3)); _println(_get_field(b, \"g\")); _set_field(b, \"f\", 100);_println(_get_field(b, \"f\"));_println(_invoke_method(b, \"g1\", 5, _new_instance(\"java.lang.Integer\",6)));_println(_invoke_method(b, \"g1\", 5));_println(\"f1 \" + _invoke_method(b, \"f1\", 5, 6));_println(\"f1 \" + _invoke_method(b, \"f1\", 5));}")
                .addFunctions("function g() {b = _new_instance(\"" + B.class.getName() + "\", 3); _set_public_field(b, \"f2\", 100);_set_public_field(b, \"g2\", _new_instance(\"java.lang.Integer\",101));_println(_get_public_field(b, \"g2\"));_println(_get_public_field(b, \"f2\"));_println(_invoke_public_method(b, \"g2\", 5, 6));_println(_invoke_public_method(b, \"g2\", 5));_println(\"f2 \" + _invoke_public_method(b, \"f2\", 5, 6));_println(\"f2 \" + _invoke_public_method(b, \"f2\", 5));}")
                .build();
        library.execute("f", new Object[]{});
        library.execute("g", new Object[]{});
    }
    @Test
    public void test11() {
        Library library = new Library.Builder()
                .addFunctions("function f() {b = _new_instance(\"" + B2.class.getName() + "\", _new_instance(\"java.lang.Integer\",3)); _println(_get_static_field(b, \"g\")); _set_static_field(b, \"f\", 100);_println(_get_static_field(b, \"f\"));_println(_invoke_static_method(b, \"g1\", 5, _new_instance(\"java.lang.Integer\",6)));_println(_invoke_static_method(b, \"g1\", 5));_println(\"f1 \" + _invoke_static_method(b, \"f1\", 5, 6));_println(\"f1 \" + _invoke_static_method(b, \"f1\", 5));}")
                .addFunctions("function g() {b = _new_instance(\"" + B2.class.getName() + "\", 3); _set_public_field(b, \"f2\", 100);_set_static_public_field(b, \"g2\", _new_instance(\"java.lang.Integer\",101));_println(_get_public_field(b, \"g2\"));_println(_get_static_public_field(b, \"f2\"));_println(_invoke_static_public_method(b, \"g2\", 5, 6));_println(_invoke_static_public_method(b, \"g2\", 5));_println(\"f2 \" + _invoke_static_public_method(b, \"f2\", 5, 6));_println(\"f2 \" + _invoke_static_public_method(b, \"f2\", 5));}")
                .build(); // non-static can be used on static
        library.execute("f", new Object[]{});
        library.execute("g", new Object[]{});
    }
    @Test
    public void test12() {
        Library library = new Library.Builder()
                .addFunctions("function f() {_println(_get_static_field(\"" + B2.class.getName() + "\", \"g\")); _set_static_field(\"" + B2.class.getName() + "\", \"f\", 100);_println(_get_static_field(\"" + B2.class.getName() + "\", \"f\"));_println(_invoke_static_method(\"" + B2.class.getName() + "\", \"g1\", 5, _new_instance(\"java.lang.Integer\",6)));_println(_invoke_static_method(\"" + B2.class.getName() + "\", \"g1\", 5));_println(\"f1 \" + _invoke_static_method(\"" + B2.class.getName() + "\", \"f1\", 5, 6));_println(\"f1 \" + _invoke_static_method(\"" + B2.class.getName() + "\", \"f1\", 5));}")
                .addFunctions("function g() {_set_static_public_field(\"" + B2.class.getName() + "\", \"f2\", 100);_set_static_public_field(\"" + B2.class.getName() + "\", \"g2\", _new_instance(\"java.lang.Integer\",101));_println(_get_static_public_field(\"" + B2.class.getName() + "\", \"g2\"));_println(_get_static_public_field(\"" + B2.class.getName() + "\", \"f2\"));_println(_invoke_static_public_method(\"" + B2.class.getName() + "\", \"g2\", 5, 6));_println(_invoke_static_public_method(\"" + B2.class.getName() + "\", \"g2\", 5));_println(\"f2 \" + _invoke_static_public_method(\"" + B2.class.getName() + "\", \"f2\", 5, 6));_println(\"f2 \" + _invoke_static_public_method(\"" + B2.class.getName() + "\", \"f2\", 5));}")
                .build();
        library.execute("f", new Object[]{});
        library.execute("g", new Object[]{});
    }
    @Test
    public void test13() {
        Library library = new Library.Builder()
                .addFunctions("function f() {a = _new_array(\"int\", 3); _println(a[0]);a = _new_array(\"java.lang.Integer\", 3); _println(a[0]); g(3);}")
                .addFunctions("function g(a) {_println(_get_class_name(a));}")
                .build();
        library.execute("f", new Object[]{});
        library.execute("g", new Object[]{2}); // Always java.lang.Integer
    }
    @Test
    public void test14() {
        Library library = new Library.Builder()
                .addFunctions("function g(a) {_println(_instance_of(a, \"int\"));_println(_instance_of(2, \"int\"));_println(_is_int(2));}")
                .build();
        library.execute("g", new Object[]{2}); // Always java.lang.Integer
    }
}