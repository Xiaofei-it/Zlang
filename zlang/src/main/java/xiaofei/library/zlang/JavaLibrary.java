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

/**
 * Created by Xiaofei on 2017/9/30.
 */

public abstract class JavaLibrary {

    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, JavaFunction>> fixedArgsFunctions;

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<JavaFunction>> varArgsFunctions;

    protected JavaLibrary() {
        fixedArgsFunctions = new ConcurrentHashMap<>();
        varArgsFunctions = new ConcurrentHashMap<>();
        JavaFunction[] functions = onProvideJavaFunctions();
        for (JavaFunction function : functions) {
            addFunction(function);
        }
    }

    protected abstract JavaFunction[] onProvideJavaFunctions();

    private void addFunction(JavaFunction function) {
        String name = function.getFunctionName();
        if (function.isVarArgs()) {
            ConcurrentLinkedQueue<JavaFunction> list = varArgsFunctions.get(name);
            if (list == null) {
                list = new ConcurrentLinkedQueue<>();
                varArgsFunctions.put(name, list);
            }
            list.add(function);
        } else {
            ConcurrentHashMap<Integer, JavaFunction> map = fixedArgsFunctions.get(name);
            if (map == null) {
                map = new ConcurrentHashMap<>();
                fixedArgsFunctions.put(name, map);
            }
            map.put(function.getParameterNumber(), function);
        }
    }

    final JavaFunction get(String functionName, int parameterNumber) {
        ConcurrentHashMap<Integer, JavaFunction> functionMap = fixedArgsFunctions.get(functionName);
        if (functionMap != null) {
            JavaFunction function = functionMap.get(parameterNumber);
            if (function != null) {
                return function;
            }
        }
        ConcurrentLinkedQueue<JavaFunction> functionList = varArgsFunctions.get(functionName);
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
