package xiaofei.library.zlang;

import org.junit.Test;

/**
 * Created by Xiaofei on 2017/10/1.
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

    private static class C {
        int f;
        C(int f) {
            this.f = f;
        }
    }
    @Test
    public void test2() {
        C c0 = new C(0), c1 = new C(1), c2 = new C(2), c3 = new C(3);
        Object[] cc1 = new Object[]{c0, c1, c2, c3};
        C[] cc2 = new C[12];
        System.arraycopy(cc1, 1, cc2, 8, 3);
        for (int i = 0; i < cc2.length; ++i) {
            System.out.println(i + " " + (cc2[i] == null ? null : cc2[i].f));
        }
        System.out.println(cc1[1] + " " + cc2[8]);
        System.out.println(cc1[2] + " " + cc2[9]);
        System.out.println(cc1[3] + " " + cc2[10]);
        System.out.println(cc1[1] == cc2[8]);
        System.out.println(cc1[2] == cc2[9]);
        System.out.println(cc1[3] == cc2[10]);
    }

    @Test
    public void test3() {
        Object[] cc1 = new Object[]{0, 1, 2, 3};
        int[] cc2 = new int[12];
        System.arraycopy(cc1, 1, cc2, 8, 3);
        for (int i = 0; i < cc2.length; ++i) {
            System.out.println(i + " " + cc2[i]);
        }
        System.out.println(cc1[1] + " " + cc2[8]);
        System.out.println(cc1[2] + " " + cc2[9]);
        System.out.println(cc1[3] + " " + cc2[10]);
    }
}
