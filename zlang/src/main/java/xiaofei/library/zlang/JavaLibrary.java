package xiaofei.library.zlang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Xiaofei on 2017/9/30.
 */

public abstract class JavaLibrary {

    private final HashMap<String, HashMap<Integer, JavaFunction>> fixedArgsFunctions;

    private final HashMap<String, LinkedList<JavaFunction>> varArgsFunctions;

    protected JavaLibrary() {
        fixedArgsFunctions = new HashMap<>();
        varArgsFunctions = new HashMap<>();
        JavaFunction[] functions = onProvideJavaFunctions();
        for (JavaFunction function : functions) {
            addFunction(function);
        }
    }

    protected abstract JavaFunction[] onProvideJavaFunctions();

    private void addFunction(JavaFunction function) {
        String name = function.getFunctionName();
        if (function.isVarArgs()) {
            LinkedList<JavaFunction> list = varArgsFunctions.get(name);
            if (list == null) {
                list = new LinkedList<>();
                varArgsFunctions.put(name, list);
            }
            list.add(function);
        } else {
            HashMap<Integer, JavaFunction> map = fixedArgsFunctions.get(name);
            if (map == null) {
                map = new HashMap<>();
                fixedArgsFunctions.put(name, map);
            }
            map.put(function.getParameterNumber(), function);
        }
    }

    final JavaFunction get(String functionName, int parameterNumber) {
        HashMap<Integer, JavaFunction> functionMap = fixedArgsFunctions.get(functionName);
        if (functionMap != null) {
            JavaFunction function = functionMap.get(parameterNumber);
            if (function != null) {
                return function;
            }
        }
        LinkedList<JavaFunction> functionList = varArgsFunctions.get(functionName);
        if (functionList == null) {
            return null;
        }
        for (JavaFunction function : functionList) {
            if (parameterNumber >= function.getParameterNumber()) {
                return function;
            }
        }
        return null;
    }

    public static class Builder {

        private final ArrayList<JavaFunction> functions;

        public Builder() {
            functions = new ArrayList<>();
        }

        public Builder addFunctions(JavaFunction[] functions) {
            for (JavaFunction function : functions) {
                addFunction(function);
            }
            return this;
        }

        public Builder addFunction(JavaFunction function) {
            functions.add(function);
            return this;
        }

        public JavaLibrary build() {
            return new JavaLibrary() {
                @Override
                protected JavaFunction[] onProvideJavaFunctions() {
                    int length = functions.size();
                    JavaFunction[] result = new JavaFunction[length];
                    for (int i = 0; i < length; ++i) {
                        result[i] = functions.get(i);
                    }
                    return result;
                }
            };
        }
    }
}
