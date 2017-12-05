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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Xiaofei on 2017/9/23.
 */

public class Library {

    public static final Object NO_RETURN_VALUE = Executor.NO_RETURN_VALUE;

    private final ConcurrentLinkedQueue<Library> dependencies;

    private final ConcurrentLinkedQueue<JavaLibrary> javaDependencies;

    private volatile ConcurrentHashMap<String, ConcurrentHashMap<Integer, CopyOnWriteArrayList<Code>>> codeMap;

    private final String program;

    private Library(ConcurrentLinkedQueue<Library> dependencies,
                    ConcurrentLinkedQueue<JavaLibrary> javaDependencies,
                    String program) {
        this.dependencies = dependencies;
        this.javaDependencies = javaDependencies;
        this.codeMap = null;
        this.program = program;
    }

    boolean containsFunction(String functionName, int parameterNumber) {
        if (codeMap == null) {
            throw new CompileException(CompileError.NOT_COMPILED, -1, -1, "Library " + this + " is not compiled.");
        }
        ConcurrentHashMap<Integer, CopyOnWriteArrayList<Code>> codes = codeMap.get(functionName);
        if (codes != null) {
            if (codes.containsKey(parameterNumber)) {
                return true;
            }
        }
        for (JavaLibrary javaLibrary : javaDependencies) {
            if (javaLibrary.get(functionName, parameterNumber) != null) {
                return true;
            }
        }
        for (Library library : dependencies) {
            if (library.containsFunction(functionName, parameterNumber)) {
                return true;
            }
        }
        return false;
    }

    JavaFunction getJavaFunction(String functionName, int parameter) {
        for (JavaLibrary javaLibrary : javaDependencies) {
            JavaFunction function = javaLibrary.get(functionName, parameter);
            if (function != null) {
                return function;
            }
        }
        for (Library library : dependencies) {
            JavaFunction function = library.getJavaFunction(functionName, parameter);
            if (function != null) {
                return function;
            }
        }
        return null;
    }

    FunctionSearchResult getFunction(String functionName, int parameterNumber) {
        if (codeMap == null) {
            throw new CompileException(CompileError.NOT_COMPILED, -1, -1, "Library " + this + " is not compiled.");
        }
        ConcurrentHashMap<Integer, CopyOnWriteArrayList<Code>> codes = codeMap.get(functionName);
        CopyOnWriteArrayList<Code> code = null;
        if (codes != null) {
            code = codes.get(parameterNumber);
        }
        if (code != null) {
            return new FunctionSearchResult(this, code);
        }
        for (Library library : dependencies) {
            FunctionSearchResult result  = library.getFunction(functionName, parameterNumber);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    synchronized void put(String functionName, int parameterNumber, ArrayList<Code> codesToPut) {
        ConcurrentHashMap<Integer, CopyOnWriteArrayList<Code>> codes = codeMap.get(functionName);
        if (codes == null) {
            codes = new ConcurrentHashMap<>();
            codeMap.put(functionName, codes);
        }
        if (codes.put(parameterNumber, new CopyOnWriteArrayList<>(codesToPut)) != null) {
            throw new CompileException(CompileError.FUNCTION_ALREADY_EXIST, -1, -1,
                    "function name: " + functionName + " parameter number: " + parameterNumber);
        }
    }

    private void compile() {
        if (codeMap != null) {
            return;
        }
        codeMap = new ConcurrentHashMap<>();
        new Compiler(this).compile();
    }

    public Object execute(String functionName, Object[] input) {
        return Executor.execute(this, functionName, input);
    }

    String getProgram() {
        return program;
    }
//
//    void compileDependencies() {
//        for (Library library : dependencies) {
//            library.compile();
//        }
//    }

    /**
     * For unit test only!
     *
     * @param functionName
     * @param parameterNumber
     */
    void print(String functionName, int parameterNumber) {
        FunctionSearchResult result = getFunction(functionName, parameterNumber);
        if (result == null) {
            System.out.println("No such function.");
        } else {
            System.out.println(functionName + " " + parameterNumber);
            int size = result.codes.size();
            for (int i = 0; i < size; ++i) {
                Code code = result.codes.get(i);
                System.out.println(i+ "\t" + code.getOpr() + "\t" + code.getOperand());
            }
            System.out.println("End.");
        }
    }

    public static class Builder {

        private StringBuilder program;

        private ArrayList<Library> dependencies;

        private ArrayList<JavaLibrary> javaDependencies;

        public Builder() {
            program = new StringBuilder();
            dependencies = new ArrayList<>();
            javaDependencies = new ArrayList<>();
        }

        public Builder addFunctions(String functions) {
            program.append(functions).append('\n');
            return this;
        }

        public Builder addDependency(Library library) {
            dependencies.add(library);
            return this;
        }

        public Builder addJavaDependency(JavaLibrary library) {
            javaDependencies.add(library);
            return this;
        }

        public Library build() { // NOT thread-safe
            ArrayList<JavaLibrary> javaLibraries = new ArrayList<>();
            javaLibraries.add(InternalJavaFunctions.INSTANCE);
            javaLibraries.addAll(javaDependencies);
            Library library = new Library(
                    new ConcurrentLinkedQueue<>(dependencies),
                    new ConcurrentLinkedQueue<>(javaLibraries),
                    program.toString());
            library.compile();
            return library;
        }
    }

    static class FunctionSearchResult {
        final Library library;
        final CopyOnWriteArrayList<Code> codes;
        FunctionSearchResult(Library library, CopyOnWriteArrayList<Code> codes) {
            this.library = library;
            this.codes = codes;
        }
    }
}
