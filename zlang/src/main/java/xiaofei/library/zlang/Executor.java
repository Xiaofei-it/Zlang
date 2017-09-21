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
            Fct fct = code.getOpr();
            switch (fct) {
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
                case PROC: {
                    String funName = (String) code.getOperand();
                    int paraNumber = (int) stack[t];
                    if (funName.startsWith("_")) {
                        Object[] parameters = new Object[paraNumber];
                        for (int i = t; i >= t - paraNumber + 1; --i) {
                            parameters[t - i] = stack[i];
                        }
                        t = t - paraNumber;
                        Object result = InternalFunctions.call(funName, parameters);
                        if (fct == Fct.FUN) {
                            stack[t++] = result;
                        }
                    } else {
                        for (int i = t; i >= t - paraNumber + 1; --i) {
                            stack[i + 3] = stack[i];
                        }
                        t = t - paraNumber;
                        stack[t + 1] = b;
                        stack[t + 2] = codes;
                        stack[t + 3] = p;
                        stack[t + 4] = fct == Fct.FUN;
                        b = t + 1;
                        codes = new ArrayList<>();
                        p = 0;
                    }
                    break;
                }
                case FUN_RETURN:
                case VOID_RETURN: {
                    int index = t;
                    t = b - 1;
                    codes = (ArrayList<Code>) stack[t + 2];
                    p = (int) stack[t + 3];
                    b = (int) stack[t + 1];
                    if ((boolean) stack[t + 4] && fct == Fct.FUN_RETURN) {
                        stack[++t] = stack[index];
                    }
                    break;
                }
                case OPR: {
                    OprAdapter oprAdapter = OprAdapterFactory.getInstance((Opr) code.getOperand());
                    int num = oprAdapter.getOperandNumber();
                    Object[] objects = new Object[num];
                    for (int i = 0; i < num; ++i) {
                        objects[i] = stack[t - num + i + 1];
                    }
                    stack[t = t - num + 1] = oprAdapter.operate(objects);
                    break;
                }
            }
        } while (p != -1);
        return stack[0];
    }
}
