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
        int length = input.length;
        for (int i = 0; i < length; ++i) {
            stack[i + 1] = input[i];
        }
        int pos = 0, base = 1, top = length;
        Object returnValue = null;
        do {
            Code code = codes.get(pos++);
            Fct fct = code.getOpr();
            Object operand = code.getOperand();
            switch (fct) {
                case LIT:
                    stack[++top] = operand;
                    break;
                case LOD:
                    stack[++top] = stack[base + (int) operand];
                    break;
                case STO:
                    stack[base + (int) operand] = stack[top--];
                    break;
                case INT://// TODO: 2017/9/27
                    top += (int) operand;
                    break;
                case JMP:
                    pos = (int) operand;
                    break;
                case JPC:
                    if (!(boolean) stack[top--]) {
                        pos = (int) operand;
                    }
                    break;
                case FUN:
                case PROC: {
                    String functionName = (String) operand;
                    int parameterNumber = (int) stack[top--];
                    if (functionName.startsWith("_")) {
                        Object[] parameters = new Object[parameterNumber];
                        // 0 -> top - num + 1, num - 1 -> top
                        for (int i = 0; i < parameterNumber; ++i) {
                            parameters[i] = stack[top - parameterNumber + 1 + i];
                        }
                        top -= parameterNumber;
                        Object result = InternalFunctions.call(functionName, parameters);
                        if (fct == Fct.FUN) {
                            stack[top++] = result;
                        }
                    } else {
                        // What if the function block?
                        for (int i = top; i >= top - parameterNumber + 1; --i) {
                            stack[i + 1] = stack[i];
                        }
                        stack[top - parameterNumber + 1] = new Frame(base, pos, codes, library, fct == Fct.FUN);
                        base = top - parameterNumber + 2;
                        ++top;
                        Library.FunctionSearchResult result = library.get(functionName, parameterNumber);
                        library = result.library;
                        codes = result.codes;
                        pos = 0;
                    }
                    break;
                }
                case FUN_RETURN: {
                    returnValue = stack[top];
                    Frame frame = (Frame) stack[top = base - 1];
                    pos = frame.pos;
                    base = frame.base;
                    library = frame.dependency;
                    codes = frame.codes;
                    stack[top] = returnValue;
                    break;
                }
                case VOID_RETURN: {
                    returnValue = VOID;
                    Frame frame = (Frame) stack[top = base - 1];
                    pos = frame.pos;
                    base = frame.base;
                    library = frame.dependency;
                    codes = frame.codes;
                    break;
                }
                case OPR: {
                    OprAdapter oprAdapter = OprAdapterFactory.getInstance((Opr) operand);
                    int num = oprAdapter.getOperandNumber();
                    top = top - num + 1;
                    stack[top] = oprAdapter.operate(stack, top);
                    break;
                }
                default:
                    throw new IllegalStateException("Illegal enum: " + fct);
            }
        } while (pos != -1);
        return returnValue;
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
