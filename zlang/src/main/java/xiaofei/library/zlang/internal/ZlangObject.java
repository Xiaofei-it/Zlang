package xiaofei.library.zlang.internal;

import java.util.concurrent.CopyOnWriteArrayList;

import xiaofei.library.zlang.executor.ZlangRuntimeException;

/**
 * Created by Xiaofei on 2017/12/13.
 */

class ZlangObject {

    final ZlangClass declaringClass;

    private final CopyOnWriteArrayList<Object> variables = new CopyOnWriteArrayList<>();

    ZlangObject(ZlangClass declaringClass) {
        this.declaringClass = declaringClass;
    }

    void set(int offset, Object value) {
        variables.set(offset, value);
    }

    void set(String name, Object value) {
        int offset = declaringClass.getVariableOffset(name);
        if (offset >= 0) {
            set(offset, value);
        } else {
            throw new ZlangRuntimeException(null);
        }
    }

    Object get(int offset) {
        return variables.get(offset);
    }

    Object get(String name) {
        int offset = declaringClass.getVariableOffset(name);
        if (offset >= 0) {
            return get(offset);
        } else {
            throw new ZlangRuntimeException(null);
        }
    }
}
