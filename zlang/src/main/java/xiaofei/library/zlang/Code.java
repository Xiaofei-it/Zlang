package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/9/9.
 */

class Code {

    private Fct fct;

    private Object operand;

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
