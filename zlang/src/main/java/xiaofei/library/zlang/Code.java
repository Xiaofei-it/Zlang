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
 * Created by Xiaofei on 2017/9/9.
 */

class Code {

    private final Fct fct;

    private volatile Object operand;

    Code(Fct fct, Object operand) {
        this.fct = fct;
        this.operand = operand;
    }

    Fct getOpr() {
        return fct;
    }

    Object getOperand() {
        return operand;
    }

    void setOperand(Object operand) {
        this.operand = operand;
    }
}
