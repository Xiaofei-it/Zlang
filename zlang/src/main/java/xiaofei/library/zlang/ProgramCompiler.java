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

/**
 * Created by Xiaofei on 2017/12/12.
 *
 */

class ProgramCompiler extends BaseCompiler {

    private final Library library;

    ProgramCompiler(Library library) {
        super(library.getProgram() + " END ", new ReadState());
        this.library = library;
    }

    CompileResult compile() {
        if (readState.nextSymbol == null) {
            moveToNextSymbol();
        }
        do {
            if (readState.nextSymbol == Symbol.FUNCTION) {
                CompileResult compileResult = new FunctionCompiler(this).compile();
                library.put(compileResult.functionName, compileResult.parameterNumber, compileResult.codes);
            } else if (readState.nextSymbol == Symbol.END) {
                break;
            } else {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, "function");
            }
        } while (true);
//        for (FunctionWrapper functionWrapper : neededFunctions) {
//            if (!library.containsFunction(functionWrapper.functionName, functionWrapper.parameterNumber)) {
//                throw new CompileException(
//                        CompileError.UNDEFINED_FUNCTION, readState,
//                        "Function name: " + functionWrapper.functionName
//                                + " Parameter number: " + functionWrapper.parameterNumber);
//            }
//        }
//        return new CompileResult(functionName, parameterNumber, codes, readState);
        return null;
    }
}