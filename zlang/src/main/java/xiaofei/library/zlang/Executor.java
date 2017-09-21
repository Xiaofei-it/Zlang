package xiaofei.library.zlang;

import java.util.ArrayList;

/**
 * Created by Xiaofei on 2017/9/21.
 */

class Executor {

    static final Object VOID = new Object();

    private Object[] stack = new Object[1000];

    Object run() {
        ArrayList<Code> codes = new ArrayList<>();
        int p = 0, b = 0, t = -1;
        stack[0] = 0;
        stack[1] = -1;
        do {
            Code code = codes.get(p++);
            switch (code.getOpr()) {
                case LIT:
                    stack[++t] = code.getOperand();
                    break;
                case LOD:
                    stack[++t] = stack[b + (int) code.getOperand()];
                    break;
                case STO:
                    stack[b + (int) code.getOperand()] = stack[t--];
                    break;
                case INT:
                    t += (int) code.getOperand();
                    break;
                case JMP:
                    p = (int) code.getOperand();
                    break;
                case JPC:
                    if (!(boolean) stack[t--]) {
                        p = (int) code.getOperand();
                    }
                    break;
                case FUN:
                    int paraNumber = (int) stack[t];
                    // TODO
                    break;
                case PROC:
                    break;
                case FUN_RETURN:
                case VOID_RETURN:
                    break;
                case OPR:
                    OprAdapter oprAdapter = OprAdapterFactory.getInstance((Opr) code.getOperand());
                    int num = oprAdapter.getOperandNumber();
                    Object[] objects = new Object[num];
                    for (int i = 0; i < num; ++i) {
                        objects[i] = stack[t - num + i + 1];
                    }
                    stack[t = t - num + 1] = oprAdapter.operate(objects);
                    break;
            }
        } while (p != -1);
        return stack[0];
    }
}
