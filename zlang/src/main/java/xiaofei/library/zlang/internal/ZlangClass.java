package xiaofei.library.zlang.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Xiaofei on 2017/12/12.
 */

public class ZlangClass {

    private static final ConcurrentHashMap<String, ZlangClass> ZLANG_CLASSES = new ConcurrentHashMap<>();

    private final String name;

    private final ZlangClass superClass;

    private volatile int variableNumber;

    private volatile int staticVariableNumber;

    private final ConcurrentHashMap<String, Integer> staticVariableOffsets = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Integer> variableOffsets = new ConcurrentHashMap<>();

    private final CopyOnWriteArrayList<Object> staticVariables = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, CopyOnWriteArrayList<Code>>> staticFunctions = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, CopyOnWriteArrayList<Code>>> functions = new ConcurrentHashMap<>();

    /**
     *
     * super.m  then find the var in the super class and its super classes, put the class in the code.  Runtime, get the offset and get the value
     * m        then find the var in the class and its super classes,... GET/PUT_FIELD_WITH_CLASS
     *
     * When accessing a ex-class var, just get the name at runtime, and modify the var table at runtime. GET/PUT_FIELD
     *
     * super.f(...) then find the method in the super class, put the class in the code. Runtime, get the code directly INVOKE_CLASS_METHOD
     *
     *  f(...)      then do not find the method. put the class in the code. Runtime, search. And modify the table at runtime.  INVOKE_VIRTUAL_WITH_CLASS
     *
     *  when accessing a ex-class function, just get the name at runtime. INVOKE_METHOD
     */

    private ZlangClass(String name, ZlangClass superClass) {
        this.name = name;
        this.superClass = superClass;
        this.variableNumber = superClass.variableNumber;
        this.staticVariableNumber = superClass.staticVariableNumber;
    }

    static ZlangClass getZlangClass(String name, ZlangClass superClass) {
        ZlangClass zlangClass = ZLANG_CLASSES.get(name);
        if (zlangClass != null) {
            return zlangClass;
        }
        zlangClass = new ZlangClass(name, superClass);
        ZlangClass tmp = ZLANG_CLASSES.putIfAbsent(name, zlangClass);
        if (tmp == null) {
            return zlangClass;
        } else {
            return tmp;
        }
    }

    boolean addStaticVariable(String name) {
        synchronized (this) {
            return staticVariableOffsets.put(name, staticVariableNumber++) != null;
        }
    }

    boolean addVariable(String name) {
        // This will only be called when it is reading the first part of a class.
        synchronized (this) {
            return variableOffsets.put(name, variableNumber++) == null;
        }
    }

    public int getVariableOffset(String name) {
        // -1 if undefined.
        Integer offset = variableOffsets.get(name);
        if (offset != null) {
            return offset;
        }
        return superClass == null ? -1 : superClass.getVariableOffset(name);
    }

    StaticVariablePosition getStaticVariablePosition(String name) {
        // null if undefined.
        Integer tmp = staticVariableOffsets.get(name);
        if (tmp != null) {
            return new StaticVariablePosition(this, tmp);
        }
        return superClass == null ? null: superClass.getStaticVariablePosition(name);
    }

    void setStaticVariable(int offset, Object value) {
        staticVariables.set(offset, value);
    }

    Object getStaticVariable(int offset) {
        return staticVariables.get(offset);
    }

    void setStaticVariable(String name, Object value) {
        Integer offset = staticVariableOffsets.get(name);
        if (offset != null) {
            setStaticVariable(offset, value);
        } else if (superClass != null) {
            superClass.setStaticVariable(name, value);
        } else {
            throw new xiaofei.library.zlang.executor.ZlangRuntimeException(null);
        }
    }

    Object getStaticVariable(String name) {
        Integer offset = staticVariableOffsets.get(name);
        if (offset != null) {
            return getStaticVariable(offset);
        } else if (superClass != null) {
            return superClass.getStaticVariable(name);
        } else {
            throw new xiaofei.library.zlang.executor.ZlangRuntimeException(null);
        }
    }

    static class StaticVariablePosition {
        final ZlangClass declaringClass;
        final int offset;
        StaticVariablePosition(ZlangClass declaringClass, int offset) {
            this.declaringClass = declaringClass;
            this.offset = offset;
        }
    }
}
