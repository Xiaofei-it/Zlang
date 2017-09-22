package xiaofei.library.zlang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by Xiaofei on 2017/9/9.
 */

public class Compiler {

    private static final Set<Character> SPACE_CHARS = new HashSet<Character>() {
        {
            add(' ');
            add('\t');
            add('\n');
        }
    };

    private static final Set<String> RESERVED_WORDS = new HashSet<String>() {
        {
            add("END");
            add("if");
            add("else");
            add("while");
            add("for");
            add("to");
            add("step");
            add("break");
            add("continue");
            add("return");
        }
    };

    private static final Set<String> LEADING_WORDS = new HashSet<String>() {
        {
            add("if");
            add("while");
            add("for");
            add("break");
            add("continue");
            add("return");
        }
    };

    private static final CodeStorage CODE_STORAGE = CodeStorage.getInstance();

    private static final FunctionStorage FUNCTION_STORAGE = FunctionStorage.getInstance();

    private int pos;

    private char nextChar; // After read, this points to the next char to read.

    private String nextSymbol; // After read, this points to the next symbol to read.

    private Object nextObject;

    private int offset;

    private LabelRecorder continueRecorder;

    private LabelRecorder breakRecorder;

    private Map<String, Integer> symbolTable = new HashMap<>();

    private Library library;

    private String program;

    private ArrayList<Code> codes = new ArrayList<>();

    private int codeIndex; // The last code index

    public Compiler(Library library) {
        // TODO two codes: one is the one we put the codes into, and the other is the one we import code from.
        program = library.getProgram();
        this.library = library;
        pos = -1;
        continueRecorder = new LabelRecorder();
        breakRecorder = new LabelRecorder();
    }

    private static boolean isAlpha(char ch) {
        return ch == '_' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
    }

    private static boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private void moveToNextChar() {
        ++pos;
        if (pos == program.length()) {
            throw new CompilerException(CompilerError.INCOMPLETE_PROGRAM);
        }
		nextChar = program.charAt(pos);
    }

    private void moveToNextSymbol() {
        while (SPACE_CHARS.contains(nextChar)) {
            moveToNextChar();
        }
        while (nextChar == '/' && program.charAt(pos + 1) == '*') {
            moveToNextChar();
            moveToNextChar();
            char tmp;
            do {
                tmp = nextChar;
                moveToNextChar();
            } while (tmp != '*' || nextChar != '/');
            moveToNextChar();
            while (SPACE_CHARS.contains(nextChar)) {
                moveToNextChar();
            }
        }
        if (isAlpha(nextChar)) {
            String id = "";
            do {
                id = id + nextChar;
                moveToNextChar();
            } while (isAlpha(nextChar) || isDigit(nextChar));
            if (RESERVED_WORDS.contains(id)) {
                nextSymbol = id;
            } else {
                nextSymbol = "id";
            }
        } else if (isDigit(nextChar)) {
            nextSymbol = "num";
            int intNum = nextChar - '0';
            moveToNextChar();
            while (isDigit(nextChar)) {
                intNum = intNum * 10 + nextChar - '0';
                moveToNextChar();
            }
            if (nextChar == '.') {
                double doubleNum = intNum;
                double tmp = 1;
                moveToNextChar();
                while (isDigit(nextChar)) {
                    tmp /= 10;
                    doubleNum = doubleNum + tmp * (nextChar - '0');
                    moveToNextChar();
                }
                nextObject = doubleNum;
            } else {
                nextObject = intNum;
            }
        } else if (nextChar == '<') {
            moveToNextChar();
            if (nextChar == '=') {
                nextSymbol = "<=";
                moveToNextChar();
            } else {
                nextSymbol = "<";
            }
        } else if (nextChar == '>') {
            moveToNextChar();
            if (nextChar == '=') {
                nextSymbol = ">=";
                moveToNextChar();
            } else {
                nextSymbol = ">";
            }
        } else if (nextChar == '=') {
            moveToNextChar();
            if (nextChar == '=') {
                nextSymbol = "==";
                moveToNextChar();
            } else {
                nextSymbol = "=";
            }
        } else if (nextChar == '!') {
            moveToNextChar();
            if (nextChar == '=') {
                nextSymbol = "!=";
                moveToNextChar();
            } else {
                nextSymbol = "!";
            }
        } else if (nextChar == '&') {
            moveToNextChar();
            if (nextChar == '&') {
                nextSymbol = "&&";
                moveToNextChar();
            } else {
                throw new CompilerException(CompilerError.WRONG_SYMBOL, "&");
            }
        } else if (nextChar == '|') {
            moveToNextChar();
            if (nextChar == '|') {
                nextSymbol = "||";
                moveToNextChar();
            } else {
                throw new CompilerException(CompilerError.WRONG_SYMBOL, "|");
            }
        } else {
            // , (  )
            nextSymbol = Character.toString(nextChar);
            moveToNextChar();
        }
    }

    private void generateCode(Fct fct, Object operand) {
        codes.add(new Code(fct, operand));
        ++codeIndex;
    }

    private void modifyCodeOperand(int codeIndex, Object operand) {
        codes.get(codeIndex).setOperand(operand);
    }

    private int callFunction() {
        int paraNumber = 0;
        moveToNextSymbol();
        while (!nextSymbol.equals(")")) {
            simpleExpression();
            ++paraNumber;
            if (nextSymbol.equals(",")) {
                moveToNextSymbol();
            } else if (!nextSymbol.equals(")")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')' or ','");
            }
        }
        generateCode(Fct.LIT, paraNumber);
        return paraNumber;
    }

    private void addIntoNeededFunctions(String functionName, int parameterNumber) {
        for (FunctionWrapper functionWrapper : mNeededFunction) {
            if (functionWrapper.functionName.equals(functionName) && functionWrapper.paraNumber == parameterNumber) {
                return;
            }
        }
        mNeededFunction.add(new FunctionWrapper(functionName, parameterNumber));
    }

    private void factor() {
        if (nextSymbol.equals("id")) {
            String id = (String) nextObject;
            moveToNextSymbol();
            if (nextSymbol.equals("(")) {
                int parameterNumber = callFunction();
                generateCode(Fct.FUN, id);// add a label to indicate we should not ignore the return value.
                addIntoNeededFunctions(id, parameterNumber);
            } else {
                Integer addr = symbolTable.get(id);
                if (addr == null) {
                    throw new CompilerException(CompilerError.UNINITIALIZED_VARIABLE, id);
                }
                generateCode(Fct.LOD, addr);
                moveToNextSymbol();
            }
        } else if (nextSymbol.equals("num")) {
            generateCode(Fct.LIT, nextObject);
            moveToNextSymbol();
        } else if (nextSymbol.equals("(")) {
            moveToNextSymbol();
            expression();
            if (nextSymbol.equals(")")) {
                moveToNextSymbol();
            } else {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')'");
            }
        } else if (nextSymbol.equals("!")) {
            moveToNextSymbol();
            factor();
            generateCode(Fct.OPR, Opr.NOT);
        } else {
            throw new CompilerException(CompilerError.WRONG_SYMBOL, nextSymbol);
        }
    }

    private void term() {
        factor();
        while (nextSymbol.equals("*") || nextSymbol.equals("/") || nextSymbol.equals("&&")) {
            String op = nextSymbol;
            moveToNextSymbol();
            factor();
            if (op.equals("*")) {
                generateCode(Fct.OPR, Opr.TIMES);
            } else if (op.equals("/")) {
                generateCode(Fct.OPR, Opr.DIVIDE);
            } else if (op.equals("&&")) {
                generateCode(Fct.OPR, Opr.AND);
            }
        }
    }

    private void simpleExpression() {
        if (nextSymbol.equals("+") || nextSymbol.equals("-")) {
            String op = nextSymbol;
            moveToNextSymbol();
            term();
            if (op.equals("-")) {
                generateCode(Fct.OPR, Opr.NEGATIVE);
            }
        } else {
            term();
        }
        while (nextSymbol.equals("+") || nextSymbol.equals("-") || nextSymbol.equals("||")) {
            String op =nextSymbol;
            moveToNextSymbol();
            term();
            if (op.equals("+")) {
                generateCode(Fct.OPR, Opr.PLUS);
            } else if (op.equals("-")) {
                generateCode(Fct.OPR, Opr.MINUS);
            } else if (op.equals("||")) {
                generateCode(Fct.OPR, Opr.OR);
            }
        }
    }

    private void expression() {
        simpleExpression();
        if (nextSymbol.equals("==") || nextSymbol.equals("!=") || nextSymbol.equals("<")
                || nextSymbol.equals(">") || nextSymbol.equals("<=") || nextSymbol.equals(">=")) {
            String op = nextSymbol;
            moveToNextSymbol();
            simpleExpression();
            if (op.equals("==")) {
                generateCode(Fct.OPR, Opr.EQUAL);
            } else if (op.equals("!=")) {
                generateCode(Fct.OPR, Opr.NOT_EQUAL);
            } else if (op.equals("<")) {
                generateCode(Fct.OPR, Opr.LESS);
            } else if (op.equals("<=")) {
                generateCode(Fct.OPR, Opr.LESS_EQUAL);
            } else if (op.equals(">")) {
                generateCode(Fct.OPR, Opr.GREATER);
            } else if (op.equals(">=")) {
                generateCode(Fct.OPR, Opr.GREATER_EQUAL);
            }
        }
    }

    private void statement(boolean inLoop) {
        //; and {} is right.
        if (nextSymbol.equals(";")) {
            moveToNextSymbol();
        } else if (nextSymbol.equals("id")) {
            moveToNextSymbol();
            String id = (String) nextObject;
            if (nextSymbol.equals("=")) {
                Integer addr = symbolTable.get(id);
                if (addr == null) {
                    symbolTable.put(id, addr = ++offset);
                    // TODO modify the operand
                }
                moveToNextSymbol();
                expression();
                generateCode(Fct.STO, addr);
            } else if (nextSymbol.equals("(")) {
                int parameterNumber = callFunction();
                generateCode(Fct.PROC, id);
                addIntoNeededFunctions(id, parameterNumber);
            } else {
                throw new CompilerException(CompilerError.ASSIGN_OR_CALL_FUNCTION_ERROR);
            }
            if (!nextSymbol.equals(";")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        } else if (nextSymbol.equals("if")) {
            moveToNextSymbol();
            if (!nextSymbol.equals("(")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "'('");
            }
            moveToNextSymbol();
            expression();
            if (!nextSymbol.equals(")")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')'");
            }
            moveToNextSymbol();
            generateCode(Fct.JPC, 0); // if false then jump.
            int tmp = codeIndex;
            statement(inLoop);
            modifyCodeOperand(tmp, codeIndex + 1);
            if (nextSymbol.equals("else")) {
                modifyCodeOperand(tmp, codeIndex + 2);
                generateCode(Fct.JMP, 0);
                tmp = codeIndex;
                moveToNextSymbol();
                statement(inLoop);
                modifyCodeOperand(tmp, codeIndex + 1);
            }
        } else if (nextSymbol.equals("{")) {
            moveToNextSymbol();
            statement(inLoop);
            while (nextSymbol.equals("{") || LEADING_WORDS.contains(nextSymbol) || nextSymbol.equals("id")) {
                String tmp = nextSymbol;
                statement(inLoop);
                if (tmp.equals("{")) {
                    if (nextSymbol.equals("}")) {
                        moveToNextSymbol();
                    } else {
                        throw new CompilerException(CompilerError.MISSING_SYMBOL, "}");
                    }
                }
            }
            if (!nextSymbol.equals("}")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "'}'");
            }
            moveToNextSymbol();
        } else if (nextSymbol.equals("while")) {
            int tmp1 = codeIndex;
            if (!nextSymbol.equals("(")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "'('");
            }
            moveToNextSymbol();
            expression();
            if (!nextSymbol.equals(")")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')'");
            }
            moveToNextSymbol();
            generateCode(Fct.JPC, 0); //false then jump
            int tmp2 = codeIndex;
            breakRecorder.createNewLabel();
            continueRecorder.createNewLabel();
            statement(true);
            generateCode(Fct.JMP, tmp1);
            modifyCodeOperand(tmp2, codeIndex + 1);
            breakRecorder.modifyCode(codeIndex + 1);
            breakRecorder.deleteCurrentLabel();
            continueRecorder.modifyCode(tmp1);
            continueRecorder.deleteCurrentLabel();
        } else if (nextSymbol.equals("break")) {
            if (!inLoop) {
                throw new CompilerException(CompilerError.BREAK_ERROR);
            }
            generateCode(Fct.JMP, 0);
            breakRecorder.addCode(codeIndex);
            moveToNextSymbol();
            if (!nextSymbol.equals(";")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        } else if (nextSymbol.equals("for")) {//for j=a to b step c
            moveToNextSymbol();
            if (!nextSymbol.equals("id")) {
                throw new CompilerException(CompilerError.FOR_ERROR, "ID");
            }
            String id = (String) nextObject;
            Integer addr = symbolTable.get(id);
            if (addr == null) {
                symbolTable.put(id, addr = ++offset);
            }
            moveToNextSymbol();
            if (!nextSymbol.equals("=")) {
                throw new CompilerException(CompilerError.FOR_ERROR, "=");
            }
            moveToNextSymbol();
            simpleExpression();
            generateCode(Fct.STO, addr);
            int tmp1 = codeIndex + 1;
            if (!nextSymbol.equals("to")) {
                throw new CompilerException(CompilerError.FOR_ERROR, "to");
            }
            moveToNextSymbol();
            simpleExpression();
            generateCode(Fct.LOD, addr);
            generateCode(Fct.OPR, Opr.GREATER_EQUAL);
            generateCode(Fct.JPC, 0);
            int tmp2 = codeIndex;
            generateCode(Fct.JMP, 0);
            int tmp3 = codeIndex;
            int tmp4 = codeIndex + 1;
            if (!nextSymbol.equals("step")) {
                throw new CompilerException(CompilerError.FOR_ERROR, "step");
            }
            moveToNextSymbol();
            simpleExpression();
            generateCode(Fct.LOD, addr);
            generateCode(Fct.OPR, Opr.PLUS);
            generateCode(Fct.STO, addr);
            generateCode(Fct.JMP, tmp1);
            modifyCodeOperand(tmp3, codeIndex + 1);
            // TODO check
            breakRecorder.createNewLabel();
            continueRecorder.createNewLabel();
            statement(true);
            generateCode(Fct.JMP, tmp4);
            modifyCodeOperand(tmp2, codeIndex + 1);
            breakRecorder.modifyCode(codeIndex + 1);
            breakRecorder.deleteCurrentLabel();
            continueRecorder.modifyCode(tmp4);
            continueRecorder.deleteCurrentLabel();
        } else if (nextSymbol.equals("continue")) {
            if (!inLoop) {
                throw new CompilerException(CompilerError.CONTINUE_ERROR);
            }
            generateCode(Fct.JMP, 0);
            continueRecorder.addCode(codeIndex);
            moveToNextSymbol();
            if (!nextSymbol.equals(";")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        } else if (nextSymbol.equals("return")) {
            moveToNextSymbol();
            if (!nextSymbol.equals(";")) {
                simpleExpression();
                generateCode(Fct.FUN_RETURN, 0);
            } else {
                generateCode(Fct.VOID_RETURN, 0);
            }
            if (!nextSymbol.equals(";")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        }
    }

    private void function() {
		/*
		 * 刚刚显示错误没事的
		 * function id()
		 * {....} END
		 */
        nextChar = ' ';
        pos=-1;
        breakRecorder.init();
        continueRecorder.init();
        moveToNextSymbol();
        if (!nextSymbol.equals("function")) {
            throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "function");
        }
        moveToNextSymbol();
        if (nextSymbol.equals("id")) {
            moveToNextSymbol();
        } else {
            throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "ID");
        }
        String functionName = (String) nextObject;
        int paraNumber = 0;
        offset = -1;
        if (nextSymbol.equals("(")) {
            moveToNextSymbol();
        } else {
            throw new CompilerException(CompilerError.MISSING_SYMBOL, "'('");
        }
        while (!nextSymbol.equals(")")) {
            if (!nextSymbol.equals("id")) {
                throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "para");
            }
            String id = (String) nextObject;
            ++paraNumber;
            ++offset;
            symbolTable.put(id, offset);
            moveToNextSymbol();
            if (!nextSymbol.equals(")") && !nextSymbol.equals(",")) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')' or ','");
            }
            if (nextSymbol.equals(",")) {
                moveToNextSymbol();
            }
        }
        moveToNextSymbol();
        generateCode(Fct.INT, offset + 1);//????????????????????要不要加1
//        if (!Functions.setAfterCheck(FunctionName,para,cx))
//                throw new CompilerException(CompilerError.ParameterNumberWrong,FunctionName);
        statement(false);
        generateCode(Fct.VOID_RETURN, 0);//This is different from funReturn  here when meet this, is a error.
        library.put(functionName, paraNumber, codes);
    }

    void compile() {
        do {
            function();
            if (nextSymbol.equals("END")) {
                break;
            } else if (!nextSymbol.equals("function")) {
                throw new CompilerException(null);
            }
        } while (true);
        library.compileSubLibraries();
        for (FunctionWrapper functionWrapper : mNeededFunction) {
            if (!library.containFunction(functionWrapper.functionName, functionWrapper.paraNumber)) {
                throw new CompilerException(null);
            }
        }
    }

    private LinkedList<FunctionWrapper> mNeededFunction = new LinkedList<>();

    private static class FunctionWrapper {
        final String functionName;
        final int paraNumber;
        FunctionWrapper(String functionName, int paraNumber) {
            this.functionName = functionName;
            this.paraNumber = paraNumber;
        }
    }
    private class LabelRecorder {
        private HashMap<Integer, HashSet<Integer>> labels;
        private int currentLabel;
        void init() {
            currentLabel = 0;
            labels = new HashMap<Integer,HashSet<Integer>>();
        }

        void addCode(int cx) {
            labels.get(currentLabel).add(cx);
        }

        void createNewLabel() {
            ++currentLabel;
            labels.put(currentLabel, new HashSet<Integer>());
        }

        void modifyCode(int cx) {
            HashSet<Integer> cxs = labels.get(currentLabel);
//            if (s==null)
//                return;
            for (int c :cxs) {
                codes.get(c).setOperand(cx);
            }
        }

        void deleteCurrentLabel() {
            labels.remove(currentLabel);
            --currentLabel;
        }
    }
}
// TODO override    string
