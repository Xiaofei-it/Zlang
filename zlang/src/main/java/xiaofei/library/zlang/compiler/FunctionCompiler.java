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

package xiaofei.library.zlang.compiler;

import java.util.ArrayList;

import xiaofei.library.zlang.internal.Fct;
import xiaofei.library.zlang.internal.Symbol;

/**
 * Created by Xiaofei on 2017/12/12.
 *
 */

class FunctionCompiler extends BaseCompiler {

    FunctionCompiler(BaseCompiler baseCompiler) {
        super(baseCompiler);
    }

    CompileResult compile() {
		breakRecorder.init();
        continueRecorder.init();
        symbolTable.clear();
        codes = new ArrayList<>();
        codeIndex = -1;
        if (readState.nextSymbol == null) {
            moveToNextSymbol();
        }
        if (readState.nextSymbol != Symbol.FUNCTION) {
            throw new CompileException(CompileError.MISSING_SYMBOL, readState, "function");
        }
        moveToNextSymbol();
        if (readState.nextSymbol != Symbol.ID) {
            throw new CompileException(CompileError.ILLEGAL_SYMBOL, readState, "" + readState.nextSymbol);
        }
        String functionName = (String) readState.nextObject;
        moveToNextSymbol();
        int parameterNumber = 0;
        offset = -1;
        if (readState.nextSymbol == Symbol.LEFT_PARENTHESIS) {
            moveToNextSymbol();
        } else {
            throw new CompileException(CompileError.MISSING_SYMBOL, readState, "(");
        }
        while (readState.nextSymbol != Symbol.RIGHT_PARENTHESIS) {
            if (readState.nextSymbol != Symbol.ID) {
                throw new CompileException(CompileError.ILLEGAL_SYMBOL, readState, "" + readState.nextSymbol);
            }
            String id = (String) readState.nextObject;
            ++parameterNumber;
            ++offset;
            symbolTable.put(id, offset);
            moveToNextSymbol();
            if (readState.nextSymbol != Symbol.RIGHT_PARENTHESIS && readState.nextSymbol != Symbol.COMMA) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ") or ,");
            }
            if (readState.nextSymbol == Symbol.COMMA) {
                moveToNextSymbol();
            }
        }
        moveToNextSymbol();
        generateCode(Fct.INT, 0);
        int tmp = codeIndex;
        statement(false);
        generateCode(Fct.VOID_RETURN, 0);
        modifyCodeOperand(tmp, offset + 1);
        return new CompileResult(functionName, parameterNumber, codes);
    }
}