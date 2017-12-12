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

package xiaofei.library.zlang.executor;

import java.lang.reflect.Array;
import java.util.concurrent.CopyOnWriteArrayList;

import xiaofei.library.zlang.internal.Code;
import xiaofei.library.zlang.internal.Fct;
import xiaofei.library.zlang.JavaFunction;
import xiaofei.library.zlang.Library;
import xiaofei.library.zlang.opr.Opr;
import xiaofei.library.zlang.opr.OprAdapter;
import xiaofei.library.zlang.opr.OprAdapterFactory;

/**
 * Created by Xiaofei on 2017/9/21.
 */

class Executor {

    static final Object NO_RETURN_VALUE = new Object();

    private Executor() {}

    static Object execute(Library inputLibrary, String functionName, Object[] input) {
        Object[] stack = new Object[1000];
        Library.FunctionSearchResult functionSearchResult = inputLibrary.getFunction(functionName, input.length);
        CopyOnWriteArrayList<Code> codes = functionSearchResult.codes;
        Library library = functionSearchResult.library;
        stack[0] = new Frame(0, -1, null, null, false);
        int length = input.length;
        for (int i = 0; i < length; ++i) {
            stack[i + 1] = input[i];
        }
        int pos = 0, base = 1, top = 0;
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
                case ALOD: {
                    int dimens = (int) stack[top--];
                    Object tmp = stack[base + (int) operand];
                    for (int i = top - dimens + 1; i <= top; ++i) {
                        tmp = Array.get(tmp, (int) stack[i]);
                    }
                    stack[top = top - dimens + 1] = tmp;
                    break;
                }
                case STO:
                    stack[base + (int) operand] = stack[top--];
                    break;
                case ASTO: {
                    Object value = stack[top--];
                    int dimens = (int) stack[top--];
                    Object tmp = stack[base + (int) operand];
                    for (int i = top - dimens + 1; i <= top - 1; ++i) {
                        tmp = Array.get(tmp, (int) stack[i]);
                    }
                    Array.set(tmp, (int) stack[top], value);
                    top -= dimens;
                    break;
                }
                case INT:
                    top += (int) operand;
                    break;
                case JMP:
                    pos = (int) operand;
                    break;
                case JPF:
                    if (!(boolean) stack[top--]) {
                        pos = (int) operand;
                    }
                    break;
                case JPF_SC:
                    if (!(boolean) stack[top]) {
                        pos = (int) operand;
                    }
                    break;
                case JPT_SC:
                    if ((boolean) stack[top]) {
                        pos = (int) operand;
                    }
                    break;
                case FUN:
                case PROC: {
                    String target = (String) operand;
                    int parameterNumber = (int) stack[top--];
                    JavaFunction javaFunction = library.getJavaFunction(target, parameterNumber);
                    if (javaFunction != null) {
                        Object[] parameters = new Object[parameterNumber];
                        // 0 -> top - num + 1, num - 1 -> top
                        for (int i = 0; i < parameterNumber; ++i) {
                            parameters[i] = stack[top - parameterNumber + 1 + i];
                        }
                        top -= parameterNumber;
                        Object result = javaFunction.call(parameters);
                        if (fct == Fct.FUN) {
                            stack[++top] = result;
                        }
                    } else {
                        // TODO What if the function block?
                        for (int i = top; i >= top - parameterNumber + 1; --i) {
                            stack[i + 1] = stack[i];
                        }
                        stack[top = top - parameterNumber + 1] = new Frame(base, pos, codes, library, fct == Fct.FUN);
                        base = top + 1;
                        Library.FunctionSearchResult result = library.getFunction(target, parameterNumber);
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
                    // TODO check
                    if (frame.isFunction) {
                        stack[top] = returnValue;
                    } else {
                        --top;
                    }
                    break;
                }
                case VOID_RETURN: {
                    returnValue = NO_RETURN_VALUE;
                    Frame frame = (Frame) stack[top = base - 1];
                    pos = frame.pos;
                    base = frame.base;
                    library = frame.dependency;
                    codes = frame.codes;
                    if (frame.isFunction) {
                        throw new ZlangRuntimeException(ZlangRuntimeError.NO_RETURN_VALUE);
                    } else {
                        --top;
                    }
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
                    throw new ZlangRuntimeException(ZlangRuntimeError.UNKNOWN_OPERATION, fct.toString());
            }
        } while (pos != -1);
        return returnValue;
    }

    private static class Frame {
        final int base;
        final int pos;
        final CopyOnWriteArrayList<Code> codes;
        final Library dependency;
        final boolean isFunction;
        Frame(int base, int pos, CopyOnWriteArrayList<Code> codes, Library dependency, boolean isFunction) {
            this.base = base;
            this.pos = pos;
            this.codes = codes;
            this.dependency =  dependency;
            this.isFunction = isFunction;
        }
    }
}
