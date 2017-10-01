package xiaofei.library.zlang;

import org.junit.Test;

/**
 * Created by zhaolifei on 2017/10/1.
 */

public class ClassTest {
    class B {

    }
    @Test
    public void test1() {
        class A {
            class C {}
            void f() {
                System.out.println(C.class.getEnclosingClass());
                System.out.println(C.class.getEnclosingMethod());
                System.out.println(C.class.getDeclaringClass());
            }
        }
        Runnable c = new Runnable() {
            @Override
            public void run() {

            }
        };
        System.out.println(A.class.getEnclosingClass());
        System.out.println(A.class.getEnclosingMethod());
        System.out.println(A.class.getDeclaringClass());

        System.out.println(B.class.getEnclosingClass());
        System.out.println(B.class.getEnclosingMethod());
        System.out.println(B.class.getDeclaringClass());

        Class<?> cc = c.getClass();
        System.out.println(cc.getEnclosingClass());
        System.out.println(cc.getEnclosingMethod());
        System.out.println(cc.getDeclaringClass());

        new A().f();
    }
}
