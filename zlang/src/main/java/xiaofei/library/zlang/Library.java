package xiaofei.library.zlang;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xiaofei on 2017/9/23.
 */

public class Library {

    private ArrayList<Library> subLibraries;

    private HashMap<String, HashMap<Integer, ArrayList<Code>>> codeMap;

    private String program;

    private Library() {
        subLibraries = null;
        codeMap = null;
        program = null;
    }

    boolean containFunction(String functionName, int parameterNumber) {
        if (codeMap == null) {
            throw new CompilerException(null);
        }
        boolean contain = false;
        HashMap<Integer, ArrayList<Code>> codes = codeMap.get(functionName);
        if (codes != null) {
            contain = codes.containsKey(parameterNumber);
        }
        if (!contain) {
            for (Library library : subLibraries) {
                if (library.containFunction(functionName, parameterNumber)) {
                    contain = true;
                    break;
                }
            }
        }
        return contain;
    }

    FunctionSearchResult get(String functionName, int parameterNumber) {
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
        for (Library library : subLibraries) {
            FunctionSearchResult result  = library.get(functionName, parameterNumber);
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
        new Compiler(this).compile();
    }

    String getProgram() {
        return program;
    }

    void compileSubLibraries() {
        for (Library library : subLibraries) {
            library.compile();
        }
    }

    public static class Builder {

        private StringBuilder program;

        private ArrayList<Library> subLibraries;

        public Builder() {
            program = new StringBuilder();
            subLibraries = new ArrayList<>();
        }

        public Builder addFunctions(String functions) {
            program.append('\n').append(functions);
            return this;
        }

        public Builder addSubLibrary(Library library) {
            subLibraries.add(library);
            return this;
        }

        public Library build() {
            Library library = new Library();
            library.subLibraries = subLibraries;
            library.program = program.toString();
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
