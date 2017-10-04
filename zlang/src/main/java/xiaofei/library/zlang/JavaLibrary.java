package xiaofei.library.zlang;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Xiaofei on 2017/9/30.
 */

public class JavaLibrary {

    private final HashMap<String, HashMap<Integer, JavaFunction>> fixedArgsFunctions = new HashMap<String, HashMap<Integer, JavaFunction>>();

    private final HashMap<String, LinkedList<JavaFunction>> varArgsFunctions = new HashMap<String, LinkedList<JavaFunction>>();

    public JavaLibrary() {

    }

    JavaFunction get(String functionName, int parameterNumber) {
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

    public void addFunctions(JavaFunction[] functions) {
        for (JavaFunction function : functions) {
            addFunction(function);
        }
    }

    public void addFunction(JavaFunction function) {
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
}
