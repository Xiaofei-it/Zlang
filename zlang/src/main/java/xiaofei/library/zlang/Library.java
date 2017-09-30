package xiaofei.library.zlang;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xiaofei on 2017/9/23.
 */

public class Library {

    private ArrayList<Library> dependencies;

    private ArrayList<JavaLibrary> javaDependencies;

    private HashMap<String, HashMap<Integer, ArrayList<Code>>> codeMap;

    private String program;

    private Library() {
        dependencies = null;
        javaDependencies = null;
        codeMap = null;
        program = null;
    }

    boolean containsFunction(String functionName, int parameterNumber) {
        if (codeMap == null) {
            throw new CompilerException(null);
        }
        boolean contain = false;
        HashMap<Integer, ArrayList<Code>> codes = codeMap.get(functionName);
        if (codes != null) {
            contain = codes.containsKey(parameterNumber);
        }
        if (!contain) {
            for (Library library : dependencies) {
                if (library.containsFunction(functionName, parameterNumber)) {
                    contain = true;
                    break;
                }
            }
        }
        return contain;
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
            throw new CompilerException(null);
        }
        HashMap<Integer, ArrayList<Code>> codes = codeMap.get(functionName);
        ArrayList<Code> code = null;
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

    void put(String functionName, int parameterNumber, ArrayList<Code> codesToPut) {
        HashMap<Integer, ArrayList<Code>> codes = codeMap.get(functionName);
        if (codes == null) {
            codes = new HashMap<>();
            codeMap.put(functionName, codes);
        }
        if (codes.put(parameterNumber, codesToPut) != null) {
            throw new CompilerException(null);
        }
    }

    private void compile() {
        if (codeMap != null) {
            return;
        }
        codeMap = new HashMap<>();
        new Compiler(this).compile();
    }

    Object execute(String functionName, Object[] input) {
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
            program.append('\n').append(functions);
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

        public Library build() {
            Library library = new Library();
            library.dependencies = dependencies;
            ArrayList<JavaLibrary> javaLibraries = new ArrayList<>();
            javaLibraries.add(InternalJavaFunctions.INSTANCE);
            javaLibraries.addAll(javaDependencies);
            library.javaDependencies = javaLibraries;
            library.program = program.toString();
            library.compile();
            return library;
        }
    }

    static class FunctionSearchResult {
        final Library library;
        final ArrayList<Code> codes;
        FunctionSearchResult(Library library, ArrayList<Code> codes) {
            this.library = library;
            this.codes = codes;
        }
    }
}
