package xiaofei.library.zlang;

import java.util.ArrayList;

/**
 * Created by Xiaofei on 2017/9/21.
 */

class Executor {

    static final Object VOID = new Object();

    Executor() {}

    Object run(Library inputLibrary, String function, Object[] input) {
        Object[] stack = new Object[1000];
        Library.FunctionSearchResult functionSearchResult = inputLibrary.get(function, input.length);
        ArrayList<Code> codes = functionSearchResult.codes;
        Library library = functionSearchResult.library;
        stack[0] = new Frame(0, -1, null, null, true);
        int pos = 0, base = 1, top = 0;
        do {
            Code code = codes.get(pos++);
            Fct fct = code.getOpr();
            switch (fct) {
                case LIT:
                    stack[++top] = code.getOperand();
                    break;
                case LOD:
                    stack[++top] = stack[base + (int) code.getOperand()];
                    break;
                case STO:
                    stack[base + (int) code.getOperand()] = stack[top--];
                    break;
                case INT:
                    top += (int) code.getOperand();
                    break;
                case JMP:
                    pos = (int) code.getOperand();
                    break;
                case JPC:
                    if (!(boolean) stack[top--]) {
                        pos = (int) code.getOperand();
                    }
                    break;
                case FUN:
                case PROC: {
                    String functionName = (String) code.getOperand();
                    int parameterNumber = (int) stack[top];
                    if (functionName.startsWith("_")) {
                        Object[] parameters = new Object[parameterNumber];
                        for (int i = top; i >= top - parameterNumber + 1; --i) {
                            parameters[top - i] = stack[i];
                        }
                        top -= parameterNumber;
                        Object result = InternalFunctions.call(functionName, parameters);
                        if (fct == Fct.FUN) {
                            stack[top++] = result;
                        }
                    } else {
                        for (int i = top; i >= top - parameterNumber + 1; --i) {
                            stack[i + 3] = stack[i];
                        }
                        top = top - parameterNumber;
                        stack[top + 1] = new Frame(base, pos, codes, library, fct == Fct.FUN);
                        base = top + 1;
                        Library.FunctionSearchResult result = library.get(functionName, parameterNumber);
                        library = result.library;
                        codes = result.codes;
                        pos = 0;
                    }
                    break;
                }
                case FUN_RETURN:
                case VOID_RETURN: {
                    int index = top;
                    top = base - 1;
                    codes = (ArrayList<Code>) stack[top + 2];
                    pos = (int) stack[top + 3];
                    base = (int) stack[top + 1];
                    if ((boolean) stack[top + 4] && fct == Fct.FUN_RETURN) {
                        stack[++top] = stack[index];
                    }
                    break;
                }
                case OPR: {
                    OprAdapter oprAdapter = OprAdapterFactory.getInstance((Opr) code.getOperand());
                    int num = oprAdapter.getOperandNumber();
                    Object[] objects = new Object[num];
                    for (int i = 0; i < num; ++i) {
                        objects[i] = stack[top - num + i + 1];
                    }
                    stack[top = top - num + 1] = oprAdapter.operate(objects);
                    break;
                }
                default:
                    throw new CompilerException(null);
            }
        } while (pos != -1);
        return stack[0];
    }

    private static class Frame {
        final int base;
        final int pos;
        final ArrayList<Code> codes;
        final Library dependency;
        final boolean isFunction;
        Frame(int base, int pos, ArrayList<Code> codes, Library dependency, boolean isFunction) {
            this.base = base;
            this.pos = pos;
            this.codes = codes;
            this.dependency =  dependency;
            this.isFunction = isFunction;
        }
    }
}
