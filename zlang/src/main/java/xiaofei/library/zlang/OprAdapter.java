package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/9/21.
 */

interface OprAdapter {

    int getOperandNumber();

    Object operate(Object[] stack, int start);

}
