package xiaofei.library.zlang;

/**
 * Created by zhaolifei on 2017/9/21.
 */

interface OprAdapter {

    int getOperandNumber();

    Object operate(Object[] stack, int start);

}
