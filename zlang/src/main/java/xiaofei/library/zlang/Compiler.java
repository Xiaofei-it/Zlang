package xiaofei.library.zlang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Created by Xiaofei on 2017/9/9.
 */

class Compiler {

    private static final Set<Character> SPACE_CHARS = new HashSet<Character>() {
        {
            add(' ');
            add('\t');
            add('\n');
        }
    };

    private static final HashMap<String, Symbol> RESERVED_WORDS_SYMBOLS = new HashMap<String, Symbol>() {
        {
            put("END", Symbol.END);
            put("function", Symbol.FUNCTION);
            put("if", Symbol.IF);
            put("else", Symbol.ELSE);
            put("while", Symbol.WHILE);
            put("for", Symbol.FOR);
            put("to", Symbol.TO);
            put("step", Symbol.STEP);
            put("break", Symbol.BREAK);
            put("continue",Symbol.CONTINUE);
            put("return", Symbol.RETURN);
        }
    };

    private static final HashSet<Symbol> LEADING_WORDS = new HashSet<Symbol>() {
        {
            add(Symbol.IF);
            add(Symbol.WHILE);
            add(Symbol.FOR);
            add(Symbol.BREAK);
            add(Symbol.CONTINUE);
            add(Symbol.RETURN);
        }
    };

    private static final HashMap<Character, Symbol> CHARACTER_SYMBOLS = new HashMap<Character, Symbol>() {
        {
            put(',', Symbol.COMMA);
            put(';', Symbol.SEMICOLON);
            put('(', Symbol.LEFT_PARENTHESIS);
            put(')', Symbol.RIGHT_PARENTHESIS);
            put('{', Symbol.LEFT_BRACE);
            put('}', Symbol.RIGHT_BRACE);
            put('+', Symbol.PLUS);
            put('-', Symbol.MINUS);
            put('*', Symbol.TIMES);
            put('/', Symbol.DIVIDE);
        }
    };

    private int pos = -1;

    private char nextChar = ' '; // After read, this points to the next char to read.

    private Symbol nextSymbol; // After read, this points to the next symbol to read.

    private Object nextObject;

    private int offset;

    private int codeIndex; // The last code index

    private LabelRecorder continueRecorder = new LabelRecorder();

    private LabelRecorder breakRecorder = new LabelRecorder();

    private Map<String, Integer> symbolTable = new HashMap<>();

    private LinkedList<FunctionWrapper> mNeededFunctions = new LinkedList<>();

    private Library library;

    private String program;

    private ArrayList<Code> codes;

    Compiler(Library library) {
        program = library.getProgram();
        this.library = library;
    }

    private static boolean isAlpha(char ch) {
        return ch == '_' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
    }

    private static boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private void moveToNextChar() {
        if (++pos == program.length()) {
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
            if (RESERVED_WORDS_SYMBOLS.containsKey(id)) {
                nextSymbol = RESERVED_WORDS_SYMBOLS.get(id);
            } else if (id.equals("true") || id.equals("false")) {
                nextSymbol = Symbol.BOOLEAN;
                nextObject = id.equals("true");
            } else if (id.equals("null")) {
                nextSymbol = Symbol.NULL;
                nextObject = null;
            } else {
                nextSymbol = Symbol.ID;
                nextObject = id;
            }
        } else if (isDigit(nextChar)) {
            nextSymbol = Symbol.NUMBER;
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
        } else if (nextChar == '\'') {
            nextSymbol = Symbol.CHARACTER;
            moveToNextChar();
            if (nextChar == '\\') {
                moveToNextChar();
            }
            nextObject = nextChar;
            moveToNextChar();
            moveToNextChar();
        } else if (nextChar == '\"') {
            nextSymbol = Symbol.STRING;
            String data = "";
            moveToNextChar();
            while (nextChar != '\"') {
                if (nextChar == '\\') {
                    moveToNextChar();
                }
                data += nextChar;
                moveToNextChar();
            }
            nextObject = data;
            moveToNextChar();
        } else if (nextChar == '<') {
            moveToNextChar();
            if (nextChar == '=') {
                nextSymbol = Symbol.LESS_EQUAL;
                moveToNextChar();
            } else {
                nextSymbol = Symbol.LESS;
            }
        } else if (nextChar == '>') {
            moveToNextChar();
            if (nextChar == '=') {
                nextSymbol = Symbol.GREATER_EQUAL;
                moveToNextChar();
            } else {
                nextSymbol = Symbol.GREATER;
            }
        } else if (nextChar == '=') {
            moveToNextChar();
            if (nextChar == '=') {
                nextSymbol = Symbol.EQUAL;
                moveToNextChar();
            } else {
                nextSymbol = Symbol.ASSIGN;
            }
        } else if (nextChar == '!') {
            moveToNextChar();
            if (nextChar == '=') {
                nextSymbol = Symbol.NOT_EQUAL;
                moveToNextChar();
            } else {
                nextSymbol = Symbol.NOT;
            }
        } else if (nextChar == '&') {
            moveToNextChar();
            if (nextChar == '&') {
                nextSymbol = Symbol.AND;
                moveToNextChar();
            } else {
                throw new CompilerException(CompilerError.WRONG_SYMBOL, "&");
            }
        } else if (nextChar == '|') {
            moveToNextChar();
            if (nextChar == '|') {
                nextSymbol = Symbol.OR;
                moveToNextChar();
            } else {
                throw new CompilerException(CompilerError.WRONG_SYMBOL, "|");
            }
        } else {
            nextSymbol = CHARACTER_SYMBOLS.get(nextChar);
            if (nextSymbol == null) {
                throw new CompilerException(CompilerError.WRONG_SYMBOL, Character.toString(nextChar));
            }
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
        int parameterNumber = 0;
        moveToNextSymbol();
        while (nextSymbol != Symbol.RIGHT_PARENTHESIS) {
            simpleExpression();
            ++parameterNumber;
            if (nextSymbol == Symbol.COMMA) {
                moveToNextSymbol();
            } else if (nextSymbol != Symbol.RIGHT_PARENTHESIS) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')' or ','");
            }
        }
        moveToNextSymbol();
        generateCode(Fct.LIT, parameterNumber);
        return parameterNumber;
    }

    private void addIntoNeededFunctions(String functionName, int parameterNumber) {
        if (functionName.startsWith("_")) {
            return;
        }
        for (FunctionWrapper functionWrapper : mNeededFunctions) {
            if (functionWrapper.functionName.equals(functionName) && functionWrapper.parameterNumber == parameterNumber) {
                return;
            }
        }
        mNeededFunctions.add(new FunctionWrapper(functionName, parameterNumber));
    }

    private void factor() {
        if (nextSymbol == Symbol.ID) {
            String id = (String) nextObject;
            moveToNextSymbol();
            if (nextSymbol == Symbol.LEFT_PARENTHESIS) {
                int parameterNumber = callFunction();
                generateCode(Fct.FUN, id);// add a label to indicate we should not ignore the return value.
                addIntoNeededFunctions(id, parameterNumber);
            } else {
                Integer address = symbolTable.get(id);
                if (address == null) {
                    throw new CompilerException(CompilerError.UNINITIALIZED_VARIABLE, id);
                }
                generateCode(Fct.LOD, address);
            }
        } else if (nextSymbol == Symbol.NUMBER
                || nextSymbol == Symbol.BOOLEAN
                || nextSymbol == Symbol.CHARACTER
                || nextSymbol == Symbol.STRING
                || nextSymbol == Symbol.NULL) {
            generateCode(Fct.LIT, nextObject);
            moveToNextSymbol();
        } else if (nextSymbol == Symbol.LEFT_PARENTHESIS) {
            moveToNextSymbol();
            expression();
            if (nextSymbol == Symbol.RIGHT_PARENTHESIS) {
                moveToNextSymbol();
            } else {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')'");
            }
        } else if (nextSymbol == Symbol.NOT) {
            moveToNextSymbol();
            factor();
            generateCode(Fct.OPR, Opr.NOT);
        } else {
            throw new CompilerException(CompilerError.WRONG_SYMBOL, nextSymbol.toString());
        }
    }

    private void term() {
        factor();
        while (nextSymbol == Symbol.TIMES || nextSymbol == Symbol.DIVIDE || nextSymbol == Symbol.AND) {
            Symbol op = nextSymbol;
            moveToNextSymbol();
            factor();
            if (op == Symbol.TIMES) {
                generateCode(Fct.OPR, Opr.TIMES);
            } else if (op == Symbol.DIVIDE) {
                generateCode(Fct.OPR, Opr.DIVIDE);
            } else if (op == Symbol.AND) {
                generateCode(Fct.OPR, Opr.AND);
            }
        }
    }

    private void simpleExpression() {
        if (nextSymbol == Symbol.PLUS || nextSymbol == Symbol.MINUS) {
            Symbol op = nextSymbol;
            moveToNextSymbol();
            term();
            if (op == Symbol.MINUS) {
                generateCode(Fct.OPR, Opr.NEGATIVE);
            }
        } else {
            term();
        }
        while (nextSymbol == Symbol.PLUS || nextSymbol == Symbol.MINUS || nextSymbol == Symbol.OR) {
            Symbol op =nextSymbol;
            moveToNextSymbol();
            term();
            if (op == Symbol.PLUS) {
                generateCode(Fct.OPR, Opr.PLUS);
            } else if (op == Symbol.MINUS) {
                generateCode(Fct.OPR, Opr.MINUS);
            } else if (op == Symbol.OR) {
                generateCode(Fct.OPR, Opr.OR);
            }
        }
    }

    private void expression() {
        simpleExpression();
        if (nextSymbol == Symbol.EQUAL || nextSymbol == Symbol.NOT_EQUAL
                || nextSymbol == Symbol.LESS || nextSymbol == Symbol.LESS_EQUAL
                || nextSymbol == Symbol.GREATER || nextSymbol == Symbol.GREATER_EQUAL) {
            Symbol op = nextSymbol;
            moveToNextSymbol();
            simpleExpression();
            if (op == Symbol.EQUAL) {
                generateCode(Fct.OPR, Opr.EQUAL);
            } else if (op == Symbol.NOT_EQUAL) {
                generateCode(Fct.OPR, Opr.NOT_EQUAL);
            } else if (op == Symbol.LESS) {
                generateCode(Fct.OPR, Opr.LESS);
            } else if (op == Symbol.LESS_EQUAL) {
                generateCode(Fct.OPR, Opr.LESS_EQUAL);
            } else if (op == Symbol.GREATER) {
                generateCode(Fct.OPR, Opr.GREATER);
            } else if (op == Symbol.GREATER_EQUAL) {
                generateCode(Fct.OPR, Opr.GREATER_EQUAL);
            }
        }
    }

    private void statement(boolean inLoop) {
        if (nextSymbol == Symbol.SEMICOLON) {
            moveToNextSymbol();
        } else if (nextSymbol == Symbol.ID) {
            moveToNextSymbol();
            String id = (String) nextObject;
            if (nextSymbol == Symbol.ASSIGN) {
                Integer address = symbolTable.get(id);
                if (address == null) {
                    symbolTable.put(id, address = ++offset);
                }
                moveToNextSymbol();
                expression();
                generateCode(Fct.STO, address);
            } else if (nextSymbol == Symbol.LEFT_PARENTHESIS) {
                int parameterNumber = callFunction();
                generateCode(Fct.PROC, id);
                addIntoNeededFunctions(id, parameterNumber);
            } else {
                throw new CompilerException(CompilerError.ASSIGN_OR_CALL_FUNCTION_ERROR);
            }
            if (nextSymbol != Symbol.SEMICOLON) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        } else if (nextSymbol == Symbol.IF) {
            moveToNextSymbol();
            if (nextSymbol != Symbol.LEFT_PARENTHESIS) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "'('");
            }
            moveToNextSymbol();
            expression();
            if (nextSymbol != Symbol.RIGHT_PARENTHESIS) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')'");
            }
            moveToNextSymbol();
            generateCode(Fct.JPC, 0); // if false then jump.
            int tmp = codeIndex;
            statement(inLoop);
            modifyCodeOperand(tmp, codeIndex + 1);
            if (nextSymbol == Symbol.ELSE) {
                modifyCodeOperand(tmp, codeIndex + 2);
                generateCode(Fct.JMP, 0);
                tmp = codeIndex;
                moveToNextSymbol();
                statement(inLoop);
                modifyCodeOperand(tmp, codeIndex + 1);
            }
        } else if (nextSymbol == Symbol.LEFT_BRACE) {
            moveToNextSymbol();
            statement(inLoop);
            while (nextSymbol == Symbol.LEFT_BRACE || LEADING_WORDS.contains(nextSymbol) || nextSymbol == Symbol.ID) {
                Symbol tmp = nextSymbol;
                statement(inLoop);
                if (tmp == Symbol.LEFT_BRACE) {
                    if (nextSymbol == Symbol.RIGHT_BRACE) {
                        moveToNextSymbol();
                    } else {
                        throw new CompilerException(CompilerError.MISSING_SYMBOL, "}");
                    }
                }
            }
            if (nextSymbol != Symbol.RIGHT_BRACE) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "'}'");
            }
            moveToNextSymbol();
        } else if (nextSymbol == Symbol.WHILE) {
            int tmp1 = codeIndex + 1;
            moveToNextSymbol();
            if (nextSymbol != Symbol.LEFT_PARENTHESIS) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "'('");
            }
            moveToNextSymbol();
            expression();
            if (nextSymbol != Symbol.RIGHT_PARENTHESIS) {
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
        } else if (nextSymbol == Symbol.BREAK) {
            if (!inLoop) {
                throw new CompilerException(CompilerError.BREAK_ERROR);
            }
            generateCode(Fct.JMP, 0);
            breakRecorder.addCode(codeIndex);
            moveToNextSymbol();
            if (nextSymbol != Symbol.SEMICOLON) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        } else if (nextSymbol == Symbol.FOR) {//for j=a to b step c
            moveToNextSymbol();
            if (nextSymbol != Symbol.ID) {
                throw new CompilerException(CompilerError.FOR_ERROR, "ID");
            }
            String id = (String) nextObject;
            Integer address = symbolTable.get(id);
            if (address == null) {
                symbolTable.put(id, address = ++offset);
            }
            moveToNextSymbol();
            if (nextSymbol != Symbol.ASSIGN) {
                throw new CompilerException(CompilerError.FOR_ERROR, "=");
            }
            moveToNextSymbol();
            simpleExpression();
            generateCode(Fct.STO, address);
            int tmp1 = codeIndex + 1;
            if (nextSymbol != Symbol.TO) {
                throw new CompilerException(CompilerError.FOR_ERROR, "to");
            }
            moveToNextSymbol();
            simpleExpression();
            generateCode(Fct.LOD, address);
            generateCode(Fct.OPR, Opr.GREATER_EQUAL);
            generateCode(Fct.JPC, 0);
            int tmp2 = codeIndex;
            generateCode(Fct.JMP, 0);
            int tmp3 = codeIndex;
            int tmp4 = codeIndex + 1;
            if (nextSymbol != Symbol.STEP) {
                throw new CompilerException(CompilerError.FOR_ERROR, "step");
            }
            moveToNextSymbol();
            simpleExpression();
            generateCode(Fct.LOD, address);
            generateCode(Fct.OPR, Opr.PLUS);
            generateCode(Fct.STO, address);
            generateCode(Fct.JMP, tmp1);
            modifyCodeOperand(tmp3, codeIndex + 1);
            breakRecorder.createNewLabel();
            continueRecorder.createNewLabel();
            statement(true);
            generateCode(Fct.JMP, tmp4);
            modifyCodeOperand(tmp2, codeIndex + 1);
            breakRecorder.modifyCode(codeIndex + 1);
            breakRecorder.deleteCurrentLabel();
            continueRecorder.modifyCode(tmp4);
            continueRecorder.deleteCurrentLabel();
        } else if (nextSymbol == Symbol.CONTINUE) {
            if (!inLoop) {
                throw new CompilerException(CompilerError.CONTINUE_ERROR);
            }
            generateCode(Fct.JMP, 0);
            continueRecorder.addCode(codeIndex);
            moveToNextSymbol();
            if (nextSymbol != Symbol.SEMICOLON) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        } else if (nextSymbol == Symbol.RETURN) {
            moveToNextSymbol();
            if (nextSymbol != Symbol.SEMICOLON) {
                expression();
                generateCode(Fct.FUN_RETURN, 0);
            } else {
                generateCode(Fct.VOID_RETURN, 0);
            }
            if (nextSymbol != Symbol.SEMICOLON) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        }
    }

    private void function() {
		breakRecorder.init();
        continueRecorder.init();
        symbolTable.clear();
        codes = new ArrayList<>();
        codeIndex = -1;
        if (nextSymbol == null) {
            moveToNextSymbol();
        }
        if (nextSymbol != Symbol.FUNCTION) {
            throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "function");
        }
        moveToNextSymbol();
        if (nextSymbol == Symbol.ID) {
            moveToNextSymbol();
        } else {
            throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "ID");
        }
        String functionName = (String) nextObject;
        int parameterNumber = 0;
        offset = -1;
        if (nextSymbol == Symbol.LEFT_PARENTHESIS) {
            moveToNextSymbol();
        } else {
            throw new CompilerException(CompilerError.MISSING_SYMBOL, "'('");
        }
        while (nextSymbol != Symbol.RIGHT_PARENTHESIS) {
            if (nextSymbol != Symbol.ID) {
                throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "parameter");
            }
            String id = (String) nextObject;
            ++parameterNumber;
            ++offset;
            symbolTable.put(id, offset);
            moveToNextSymbol();
            if (nextSymbol != Symbol.RIGHT_PARENTHESIS && nextSymbol != Symbol.COMMA) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')' or ','");
            }
            if (nextSymbol == Symbol.COMMA) {
                moveToNextSymbol();
            }
        }
        moveToNextSymbol();
        generateCode(Fct.INT, 0);
        int tmp = codeIndex;
        statement(false);
        generateCode(Fct.VOID_RETURN, 0);
        modifyCodeOperand(tmp, offset + 1);
        library.put(functionName, parameterNumber, codes);
    }

    void compile() {
        program += "END ";
        do {
            function();
            if (nextSymbol == Symbol.END) {
                break;
            } else if (nextSymbol != Symbol.FUNCTION) {
                throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "function");
            }
        } while (true);
//        library.compileDependencies();
        for (FunctionWrapper functionWrapper : mNeededFunctions) {
            if (!library.containsFunction(functionWrapper.functionName, functionWrapper.parameterNumber)) {
                throw new CompilerException(CompilerError.UNDEFINED_FUNCTION,
                        "Function name: " + functionWrapper.functionName
                                + " Parameter number: " + functionWrapper.parameterNumber);
            }
        }
    }

    private static class FunctionWrapper {
        final String functionName;
        final int parameterNumber;
        FunctionWrapper(String functionName, int parameterNumber) {
            this.functionName = functionName;
            this.parameterNumber = parameterNumber;
        }
    }
    private class LabelRecorder {
        private HashMap<Integer, HashSet<Integer>> labels;
        private int currentLabel;
        void init() {
            currentLabel = 0;
            labels = new HashMap<Integer,HashSet<Integer>>();
        }

        void addCode(int codeIndex) {
            labels.get(currentLabel).add(codeIndex);
        }

        void createNewLabel() {
            labels.put(++currentLabel, new HashSet<Integer>());
        }

        void modifyCode(int target) {
            HashSet<Integer> previousCodeIndexes = labels.get(currentLabel);
            for (int index :previousCodeIndexes) {
                codes.get(index).setOperand(target);
            }
        }

        void deleteCurrentLabel() {
            labels.remove(currentLabel--);
        }
    }
}
// TODO && ||