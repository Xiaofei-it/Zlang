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

import java.util.ArrayList;

/**
 * Created by Xiaofei on 2017/12/6.
 *
 * or_expression = and_exp || and_exp
 *
 * and_exp = comparison_exp && comparison_exp
 *
 * comparison_exp = numeric_exp > numeric_exp
 *
 * numeric_exp = term + term
 *
 * term = factor * factor
 */

class ProgramCompiler extends AbstractCompiler {

    ProgramCompiler(Library library) {
        super(library);
    }

    private void function() {
		breakRecorder.init();
        continueRecorder.init();
        symbolTable.clear();
        codes = new ArrayList<>();
        codeIndex = -1;
        if (nextSymbol == null) {
            moveToNextSymbol();
        }
        if (nextSymbol != Symbol.FUNCTION) {
            throw new CompileException(CompileError.MISSING_SYMBOL, linePos == 0 ? lineNumber - 1 : lineNumber, previousLinePos, "function");
        }
        moveToNextSymbol();
        if (nextSymbol != Symbol.ID) {
            throw new CompileException(CompileError.ILLEGAL_SYMBOL, linePos == 0 ? lineNumber - 1 : lineNumber, previousLinePos, "" + nextSymbol);
        }
        String functionName = (String) nextObject;
        moveToNextSymbol();
        int parameterNumber = 0;
        offset = -1;
        if (nextSymbol == Symbol.LEFT_PARENTHESIS) {
            moveToNextSymbol();
        } else {
            throw new CompileException(CompileError.MISSING_SYMBOL, linePos == 0 ? lineNumber - 1 : lineNumber, previousLinePos, "(");
        }
        while (nextSymbol != Symbol.RIGHT_PARENTHESIS) {
            if (nextSymbol != Symbol.ID) {
                throw new CompileException(CompileError.ILLEGAL_SYMBOL, linePos == 0 ? lineNumber - 1 : lineNumber, previousLinePos, "" + nextSymbol);
            }
            String id = (String) nextObject;
            ++parameterNumber;
            ++offset;
            symbolTable.put(id, offset);
            moveToNextSymbol();
            if (nextSymbol != Symbol.RIGHT_PARENTHESIS && nextSymbol != Symbol.COMMA) {
                throw new CompileException(CompileError.MISSING_SYMBOL, linePos == 0 ? lineNumber - 1 : lineNumber, previousLinePos, ") or ,");
            }
            if (nextSymbol == Symbol.COMMA) {
                moveToNextSymbol();
            }
        }
        moveToNextSymbol();
        generateCode(Fct.INT, 0);
        int tmp = codeIndex;
        statement(false);
        generateCode(Fct.VOID_RETURN, 0);
        modifyCodeOperand(tmp, offset + 1);
        library.put(functionName, parameterNumber, codes);
    }

    void compile() {
        program += "END ";
        do {
            function();
            if (nextSymbol == Symbol.END) {
                break;
            } else if (nextSymbol != Symbol.FUNCTION) {
                throw new CompileException(CompileError.MISSING_SYMBOL, linePos == 0 ? lineNumber - 1 : lineNumber, previousLinePos, "function");
            }
        } while (true);
//        library.compileDependencies();
        for (FunctionWrapper functionWrapper : neededFunctions) {
            if (!library.containsFunction(functionWrapper.functionName, functionWrapper.parameterNumber)) {
                throw new CompileException(
                        CompileError.UNDEFINED_FUNCTION, linePos == 0 ? lineNumber - 1 : lineNumber, previousLinePos,
                        "Function name: " + functionWrapper.functionName
                                + " Parameter number: " + functionWrapper.parameterNumber);
            }
        }
    }
}