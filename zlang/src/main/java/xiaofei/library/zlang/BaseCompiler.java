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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Xiaofei on 2017/9/9.
 *
 * or_expression = and_exp || and_exp
 *
 * and_exp = comparison_exp && comparison_exp
 *
 * comparison_exp = numeric_exp > numeric_exp
 *
 * numeric_exp = term + term
 *
 * term = factor * factor
 */

abstract class BaseCompiler {

    private static final HashSet<Character> SPACE_CHARS = new HashSet<Character>() {
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
            put('[', Symbol.LEFT_BRACKET);
            put(']', Symbol.RIGHT_BRACKET);
            put('+', Symbol.PLUS);
            put('-', Symbol.MINUS);
            put('*', Symbol.TIMES);
            put('/', Symbol.DIVIDE);
        }
    };

    protected final ReadState readState;

    protected LabelRecorder continueRecorder = new LabelRecorder();

    protected LabelRecorder breakRecorder = new LabelRecorder();

    protected Map<String, Integer> symbolTable = new HashMap<>();

    private LinkedList<FunctionWrapper> neededFunctions = new LinkedList<>();

//    private final Library library;

    private final String program;

    protected ArrayList<Code> codes;

//    BaseCompiler(Library library) {
//        program = library.getProgram();
////        this.library = library;
//    }

    protected BaseCompiler(String program, ReadState readState) {
        this.program = program;
        this.readState = readState;
    }

    private static boolean isAlpha(char ch) {
        return ch == '_' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
    }

    private static boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }

    private void moveToNextChar() {
        if (++readState.pos == program.length()) {
            throw new CompileException(CompileError.INCOMPLETE_PROGRAM,  readState, "Program incomplete!");
        }
        readState.nextChar = program.charAt(readState.pos);
//        if (nextChar == '\r') {
//
//        }
        if (readState.nextChar == '\n') {
            ++readState.lineNumber;
            readState.linePos = 0;
        } else {
            ++readState.linePos;
        }
    }

    protected void moveToNextSymbol() {
        while (SPACE_CHARS.contains(readState.nextChar)) {
            moveToNextChar();
        }
        while (readState.nextChar == '/' && program.charAt(readState.pos + 1) == '*') {
            moveToNextChar();
            moveToNextChar();
            char tmp;
            do {
                tmp = readState.nextChar;
                moveToNextChar();
            } while (tmp != '*' || readState.nextChar != '/');
            moveToNextChar();
            while (SPACE_CHARS.contains(readState.nextChar)) {
                moveToNextChar();
            }
        }
//        previousPos = pos;
        readState.previousLinePos = readState.linePos;
        if (isAlpha(readState.nextChar)) {
            String id = "";
            do {
                id = id + readState.nextChar;
                moveToNextChar();
            } while (isAlpha(readState.nextChar) || isDigit(readState.nextChar));
            if (RESERVED_WORDS_SYMBOLS.containsKey(id)) {
                readState.nextSymbol = RESERVED_WORDS_SYMBOLS.get(id);
                readState.nextObject = readState.nextSymbol;
            } else if (id.equals("true") || id.equals("false")) {
                readState.nextSymbol = Symbol.BOOLEAN;
                readState.nextObject = id.equals("true");
            } else if (id.equals("null")) {
                readState.nextSymbol = Symbol.NULL;
                readState.nextObject = null;
            } else {
                readState.nextSymbol = Symbol.ID;
                readState.nextObject = id;
            }
        } else if (isDigit(readState.nextChar)) {
            readState.nextSymbol = Symbol.NUMBER;
            int intNum = readState.nextChar - '0';
            moveToNextChar();
            while (isDigit(readState.nextChar)) {
                intNum = intNum * 10 + readState.nextChar - '0';
                moveToNextChar();
            }
            if (readState.nextChar == '.') {
                double doubleNum = intNum;
                double tmp = 1;
                moveToNextChar();
                while (isDigit(readState.nextChar)) {
                    tmp /= 10;
                    doubleNum = doubleNum + tmp * (readState.nextChar - '0');
                    moveToNextChar();
                }
                readState.nextObject = doubleNum;
            } else {
                readState.nextObject = intNum;
            }
        } else if (readState.nextChar == '\'') {
            readState.nextSymbol = Symbol.CHARACTER;
            moveToNextChar();
            if (readState.nextChar == '\\') {
                moveToNextChar();
            }
            readState.nextObject = readState.nextChar;
            moveToNextChar();
            moveToNextChar();
        } else if (readState.nextChar == '\"') {
            readState.nextSymbol = Symbol.STRING;
            String data = "";
            moveToNextChar();
            while (readState.nextChar != '\"') {
                if (readState.nextChar == '\\') {
                    moveToNextChar();
                }
                data += readState.nextChar;
                moveToNextChar();
            }
            readState.nextObject = data;
            moveToNextChar();
        } else if (readState.nextChar == '<') {
            moveToNextChar();
            if (readState.nextChar == '=') {
                readState.nextSymbol = Symbol.LESS_EQUAL;
                moveToNextChar();
            } else {
                readState.nextSymbol = Symbol.LESS;
            }
            readState.nextObject = readState.nextSymbol;
        } else if (readState.nextChar == '>') {
            moveToNextChar();
            if (readState.nextChar == '=') {
                readState.nextSymbol = Symbol.GREATER_EQUAL;
                moveToNextChar();
            } else {
                readState.nextSymbol = Symbol.GREATER;
            }
            readState.nextObject = readState.nextSymbol;
        } else if (readState.nextChar == '=') {
            moveToNextChar();
            if (readState.nextChar == '=') {
                readState.nextSymbol = Symbol.EQUAL;
                moveToNextChar();
            } else {
                readState.nextSymbol = Symbol.ASSIGN;
            }
            readState.nextObject = readState.nextSymbol;
        } else if (readState.nextChar == '!') {
            moveToNextChar();
            if (readState.nextChar == '=') {
                readState.nextSymbol = Symbol.NOT_EQUAL;
                moveToNextChar();
            } else {
                readState.nextSymbol = Symbol.NOT;
            }
            readState.nextObject = readState.nextSymbol;
        } else if (readState.nextChar == '&') {
            moveToNextChar();
            if (readState.nextChar == '&') {
                readState.nextSymbol = Symbol.AND;
                moveToNextChar();
            } else {
                throw new CompileException(CompileError.ILLEGAL_SYMBOL, readState, "&");
            }
            readState.nextObject = readState.nextSymbol;
        } else if (readState.nextChar == '|') {
            moveToNextChar();
            if (readState.nextChar == '|') {
                readState.nextSymbol = Symbol.OR;
                moveToNextChar();
            } else {
                throw new CompileException(CompileError.ILLEGAL_SYMBOL, readState, "|");
            }
            readState.nextObject = readState.nextSymbol;
        } else {
            readState.nextSymbol = CHARACTER_SYMBOLS.get(readState.nextChar);
            if (readState.nextSymbol == null) {
                throw new CompileException(CompileError.ILLEGAL_SYMBOL, readState, Character.toString(readState.nextChar));
            }
            readState.nextObject = readState.nextSymbol;
            moveToNextChar();
        }
    }

    protected void generateCode(Fct fct, Object operand) {
        codes.add(new Code(fct, operand));
        ++readState.codeIndex;
    }

    protected void modifyCodeOperand(int codeIndex, Object operand) {
        codes.get(codeIndex).setOperand(operand);
    }

    private int callFunction() {
        int parameterNumber = 0;
        moveToNextSymbol();
        while (readState.nextSymbol != Symbol.RIGHT_PARENTHESIS) {
            disjunctionExpression();
            ++parameterNumber;
            if (readState.nextSymbol == Symbol.COMMA) {
                moveToNextSymbol();
            } else if (readState.nextSymbol != Symbol.RIGHT_PARENTHESIS) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ") or ,");
            }
        }
        moveToNextSymbol();
        generateCode(Fct.LIT, parameterNumber);
        return parameterNumber;
    }

    private void addIntoNeededFunctions(String functionName, int parameterNumber) {
//        if (functionName.startsWith("_")) {
//            return;
//        }
        for (FunctionWrapper functionWrapper : neededFunctions) {
            if (functionWrapper.functionName.equals(functionName) && functionWrapper.parameterNumber == parameterNumber) {
                return;
            }
        }
        neededFunctions.add(new FunctionWrapper(functionName, parameterNumber));
    }

    private void factor() {
        if (readState.nextSymbol == Symbol.ID) {
            String id = (String) readState.nextObject;
            moveToNextSymbol();
            if (readState.nextSymbol == Symbol.LEFT_PARENTHESIS) {
                int parameterNumber = callFunction();
                generateCode(Fct.FUN, id);// add a label to indicate we should not ignore the return value.
                addIntoNeededFunctions(id, parameterNumber);
            } else if (readState.nextSymbol == Symbol.LEFT_BRACKET) {
                Integer address = symbolTable.get(id);
                if (address == null) {
                    throw new CompileException(CompileError.UNINITIALIZED_VARIABLE, readState, id);
                }
                int dimens = 0;
                do {
                    ++dimens;
                    moveToNextSymbol();
                    numericExpression();
                    if (readState.nextSymbol == Symbol.RIGHT_BRACKET) {
                        moveToNextSymbol();
                    } else {
                        throw new CompileException(CompileError.MISSING_SYMBOL, readState, "]");
                    }
                } while (readState.nextSymbol == Symbol.LEFT_BRACKET);
                generateCode(Fct.LIT, dimens);
                generateCode(Fct.ALOD, address);
            } else {
                Integer address = symbolTable.get(id);
                if (address == null) {
                    throw new CompileException(CompileError.UNINITIALIZED_VARIABLE, readState, id);
                }
                generateCode(Fct.LOD, address);
            }
        } else if (readState.nextSymbol == Symbol.NUMBER
                || readState.nextSymbol == Symbol.BOOLEAN
                || readState.nextSymbol == Symbol.CHARACTER
                || readState.nextSymbol == Symbol.STRING
                || readState.nextSymbol == Symbol.NULL) {
            generateCode(Fct.LIT, readState.nextObject);
            moveToNextSymbol();
        } else if (readState.nextSymbol == Symbol.LEFT_PARENTHESIS) {
            moveToNextSymbol();
            disjunctionExpression();
            if (readState.nextSymbol == Symbol.RIGHT_PARENTHESIS) {
                moveToNextSymbol();
            } else {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ")");
            }
        } else if (readState.nextSymbol == Symbol.NOT) {
            moveToNextSymbol();
            factor();
            generateCode(Fct.OPR, Opr.NOT);
        } else {
            throw new CompileException(CompileError.ILLEGAL_SYMBOL, readState, "" + readState.nextSymbol);
        }
    }

    private void term() {
        factor();
        while (readState.nextSymbol == Symbol.TIMES || readState.nextSymbol == Symbol.DIVIDE) {
            Symbol op = readState.nextSymbol;
            moveToNextSymbol();
            factor();
            if (op == Symbol.TIMES) {
                generateCode(Fct.OPR, Opr.TIMES);
            } else if (op == Symbol.DIVIDE) {
                generateCode(Fct.OPR, Opr.DIVIDE);
            }
        }
    }

    private void numericExpression() {
        if (readState.nextSymbol == Symbol.PLUS || readState.nextSymbol == Symbol.MINUS) {
            Symbol op = readState.nextSymbol;
            moveToNextSymbol();
            term();
            if (op == Symbol.MINUS) {
                generateCode(Fct.OPR, Opr.NEGATIVE);
            }
        } else {
            term();
        }
        while (readState.nextSymbol == Symbol.PLUS || readState.nextSymbol == Symbol.MINUS) {
            Symbol op = readState.nextSymbol;
            moveToNextSymbol();
            term();
            if (op == Symbol.PLUS) {
                generateCode(Fct.OPR, Opr.PLUS);
            } else if (op == Symbol.MINUS) {
                generateCode(Fct.OPR, Opr.MINUS);
            }
        }
    }

    private void comparisonExpression() {
        numericExpression();
        if (readState.nextSymbol == Symbol.EQUAL || readState.nextSymbol == Symbol.NOT_EQUAL
                || readState.nextSymbol == Symbol.LESS || readState.nextSymbol == Symbol.LESS_EQUAL
                || readState.nextSymbol == Symbol.GREATER || readState.nextSymbol == Symbol.GREATER_EQUAL) {
            Symbol op = readState.nextSymbol;
            moveToNextSymbol();
            numericExpression();
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

    private void conjunctionExpression() {
        ArrayList<Integer> codeIndexes = new ArrayList<>();
        comparisonExpression();
        while (readState.nextSymbol == Symbol.AND) {
            generateCode(Fct.JPF_SC, 0);
            codeIndexes.add(readState.codeIndex);
            moveToNextSymbol();
            comparisonExpression();
            generateCode(Fct.OPR, Opr.AND);
        }
        for (int index : codeIndexes) {
            modifyCodeOperand(index, readState.codeIndex + 1);
        }
    }

    private void disjunctionExpression() {
        ArrayList<Integer> codeIndexes = new ArrayList<>();
        conjunctionExpression();
        while (readState.nextSymbol == Symbol.OR) {
            generateCode(Fct.JPT_SC, 0);
            codeIndexes.add(readState.codeIndex);
            moveToNextSymbol();
            conjunctionExpression();
            generateCode(Fct.OPR, Opr.OR);
        }
        for (int index : codeIndexes) {
            modifyCodeOperand(index, readState.codeIndex + 1);
        }
    }

    protected void statement(boolean inLoop) {
        if (readState.nextSymbol == Symbol.SEMICOLON) {
            moveToNextSymbol();
        } else if (readState.nextSymbol == Symbol.ID) {
            String id = (String) readState.nextObject;
            moveToNextSymbol();
            if (readState.nextSymbol == Symbol.ASSIGN) {
                Integer address = symbolTable.get(id);
                if (address == null) {
                    symbolTable.put(id, address = ++readState.offset);
                }
                moveToNextSymbol();
                disjunctionExpression();
                generateCode(Fct.STO, address);
            } else if (readState.nextSymbol == Symbol.LEFT_PARENTHESIS) {
                int parameterNumber = callFunction();
                generateCode(Fct.PROC, id);
                addIntoNeededFunctions(id, parameterNumber);
            } else if (readState.nextSymbol == Symbol.LEFT_BRACKET) {
                Integer address = symbolTable.get(id);
                if (address == null) {
                    throw new CompileException(CompileError.UNINITIALIZED_ARRAY, readState, id);
                }
                int dimens = 0;
                do {
                    ++dimens;
                    moveToNextSymbol();
                    numericExpression();
                    if (readState.nextSymbol == Symbol.RIGHT_BRACKET) {
                        moveToNextSymbol();
                    } else {
                        throw new CompileException(CompileError.MISSING_SYMBOL, readState, "]");
                    }
                } while (readState.nextSymbol == Symbol.LEFT_BRACKET);
                generateCode(Fct.LIT, dimens);
                if (readState.nextSymbol != Symbol.ASSIGN) {
                    throw new CompileException(CompileError.MISSING_SYMBOL, readState, "=");
                }
                moveToNextSymbol();
                disjunctionExpression();
                generateCode(Fct.ASTO, address);
            } else {
                throw new CompileException(
                        CompileError.MISSING_SYMBOL, readState, "= or (");
            }
            if (readState.nextSymbol != Symbol.SEMICOLON) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ";");
            }
            moveToNextSymbol();
        } else if (readState.nextSymbol == Symbol.IF) {
            moveToNextSymbol();
            if (readState.nextSymbol != Symbol.LEFT_PARENTHESIS) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, "(");
            }
            moveToNextSymbol();
            disjunctionExpression();
            if (readState.nextSymbol != Symbol.RIGHT_PARENTHESIS) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ")");
            }
            moveToNextSymbol();
            generateCode(Fct.JPF, 0); // if false then jump.
            int tmp = readState.codeIndex;
            statement(inLoop);
            modifyCodeOperand(tmp, readState.codeIndex + 1);
            if (readState.nextSymbol == Symbol.ELSE) {
                modifyCodeOperand(tmp, readState.codeIndex + 2);
                generateCode(Fct.JMP, 0);
                tmp = readState.codeIndex;
                moveToNextSymbol();
                statement(inLoop);
                modifyCodeOperand(tmp, readState.codeIndex + 1);
            }
        } else if (readState.nextSymbol == Symbol.LEFT_BRACE) {
            moveToNextSymbol();
            statement(inLoop);
            while (readState.nextSymbol == Symbol.LEFT_BRACE || LEADING_WORDS.contains(readState.nextSymbol) || readState.nextSymbol == Symbol.ID) {
                Symbol tmp = readState.nextSymbol;
                statement(inLoop);
                if (tmp == Symbol.LEFT_BRACE) {
                    if (readState.nextSymbol == Symbol.RIGHT_BRACE) {
                        moveToNextSymbol();
                    } else {
                        throw new CompileException(CompileError.MISSING_SYMBOL, readState, "}");
                    }
                }
            }
            if (readState.nextSymbol != Symbol.RIGHT_BRACE) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, "}");
            }
            moveToNextSymbol();
        } else if (readState.nextSymbol == Symbol.WHILE) {
            int tmp1 = readState.codeIndex + 1;
            moveToNextSymbol();
            if (readState.nextSymbol != Symbol.LEFT_PARENTHESIS) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, "(");
            }
            moveToNextSymbol();
            disjunctionExpression();
            if (readState.nextSymbol != Symbol.RIGHT_PARENTHESIS) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ")");
            }
            moveToNextSymbol();
            generateCode(Fct.JPF, 0); //false then jump
            int tmp2 = readState.codeIndex;
            breakRecorder.createNewLabel();
            continueRecorder.createNewLabel();
            statement(true);
            generateCode(Fct.JMP, tmp1);
            modifyCodeOperand(tmp2, readState.codeIndex + 1);
            breakRecorder.modifyCode(readState.codeIndex + 1);
            breakRecorder.deleteCurrentLabel();
            continueRecorder.modifyCode(tmp1);
            continueRecorder.deleteCurrentLabel();
        } else if (readState.nextSymbol == Symbol.BREAK) {
            if (!inLoop) {
                throw new CompileException(CompileError.SEMANTIC_ERROR, readState, "'break' appears outside a loop.");
            }
            generateCode(Fct.JMP, 0);
            breakRecorder.addCode(readState.codeIndex);
            moveToNextSymbol();
            if (readState.nextSymbol != Symbol.SEMICOLON) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ";");
            }
            moveToNextSymbol();
        } else if (readState.nextSymbol == Symbol.FOR) {//for j=a to b step c
            moveToNextSymbol();
            if (readState.nextSymbol != Symbol.ID) {
                throw new CompileException(CompileError.ILLEGAL_SYMBOL, readState, "" + readState.nextSymbol);
            }
            String id = (String) readState.nextObject;
            Integer address = symbolTable.get(id);
            if (address == null) {
                symbolTable.put(id, address = ++readState.offset);
            }
            moveToNextSymbol();
            if (readState.nextSymbol != Symbol.ASSIGN) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, "=");
            }
            moveToNextSymbol();
            numericExpression();
            generateCode(Fct.STO, address);
            int tmp1 = readState.codeIndex + 1;
            if (readState.nextSymbol != Symbol.TO) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, "to");
            }
            moveToNextSymbol();
            numericExpression();
            generateCode(Fct.LOD, address);
            generateCode(Fct.OPR, Opr.GREATER_EQUAL);
            generateCode(Fct.JPF, 0);
            int tmp2 = readState.codeIndex;
            generateCode(Fct.JMP, 0);
            int tmp3 = readState.codeIndex;
            int tmp4 = readState.codeIndex + 1;
            if (readState.nextSymbol != Symbol.STEP) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, "step");
            }
            moveToNextSymbol();
            numericExpression();
            generateCode(Fct.LOD, address);
            generateCode(Fct.OPR, Opr.PLUS);
            generateCode(Fct.STO, address);
            generateCode(Fct.JMP, tmp1);
            modifyCodeOperand(tmp3, readState.codeIndex + 1);
            breakRecorder.createNewLabel();
            continueRecorder.createNewLabel();
            statement(true);
            generateCode(Fct.JMP, tmp4);
            modifyCodeOperand(tmp2, readState.codeIndex + 1);
            breakRecorder.modifyCode(readState.codeIndex + 1);
            breakRecorder.deleteCurrentLabel();
            continueRecorder.modifyCode(tmp4);
            continueRecorder.deleteCurrentLabel();
        } else if (readState.nextSymbol == Symbol.CONTINUE) {
            if (!inLoop) {
                throw new CompileException(CompileError.SEMANTIC_ERROR, readState, "'continue' appears outside a loop.");
            }
            generateCode(Fct.JMP, 0);
            continueRecorder.addCode(readState.codeIndex);
            moveToNextSymbol();
            if (readState.nextSymbol != Symbol.SEMICOLON) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ";");
            }
            moveToNextSymbol();
        } else if (readState.nextSymbol == Symbol.RETURN) {
            moveToNextSymbol();
            if (readState.nextSymbol != Symbol.SEMICOLON) {
                disjunctionExpression();
                generateCode(Fct.FUN_RETURN, 0);
            } else {
                generateCode(Fct.VOID_RETURN, 0);
            }
            if (readState.nextSymbol != Symbol.SEMICOLON) {
                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ";");
            }
            moveToNextSymbol();
        }
    }

//    private void function() {
//		breakRecorder.init();
//        continueRecorder.init();
//        symbolTable.clear();
//        codes = new ArrayList<>();
//        readState.codeIndex = -1;
//        if (readState.nextSymbol == null) {
//            moveToNextSymbol();
//        }
//        if (readState.nextSymbol != Symbol.FUNCTION) {
//            throw new CompileException(CompileError.MISSING_SYMBOL, readState, "function");
//        }
//        moveToNextSymbol();
//        if (readState.nextSymbol != Symbol.ID) {
//            throw new CompileException(CompileError.ILLEGAL_SYMBOL, readState, "" + readState.nextSymbol);
//        }
//        String functionName = (String) readState.nextObject;
//        moveToNextSymbol();
//        int parameterNumber = 0;
//        readState.offset = -1;
//        if (readState.nextSymbol == Symbol.LEFT_PARENTHESIS) {
//            moveToNextSymbol();
//        } else {
//            throw new CompileException(CompileError.MISSING_SYMBOL, readState, "(");
//        }
//        while (readState.nextSymbol != Symbol.RIGHT_PARENTHESIS) {
//            if (readState.nextSymbol != Symbol.ID) {
//                throw new CompileException(CompileError.ILLEGAL_SYMBOL, readState, "" + readState.nextSymbol);
//            }
//            String id = (String) readState.nextObject;
//            ++parameterNumber;
//            ++readState.offset;
//            symbolTable.put(id, readState.offset);
//            moveToNextSymbol();
//            if (readState.nextSymbol != Symbol.RIGHT_PARENTHESIS && readState.nextSymbol != Symbol.COMMA) {
//                throw new CompileException(CompileError.MISSING_SYMBOL, readState, ") or ,");
//            }
//            if (readState.nextSymbol == Symbol.COMMA) {
//                moveToNextSymbol();
//            }
//        }
//        moveToNextSymbol();
//        generateCode(Fct.INT, 0);
//        int tmp = readState.codeIndex;
//        statement(false);
//        generateCode(Fct.VOID_RETURN, 0);
//        modifyCodeOperand(tmp, readState.offset + 1);
//        library.put(functionName, parameterNumber, codes);
//    }

//    void compile() {
//        program += "END ";
//        do {
//            function();
//            if (readState.nextSymbol == Symbol.END) {
//                break;
//            } else if (readState.nextSymbol != Symbol.FUNCTION) {
//                throw new CompileException(CompileError.MISSING_SYMBOL, readState, "function");
//            }
//        } while (true);
////        library.compileDependencies();
//        for (FunctionWrapper functionWrapper : neededFunctions) {
//            if (!library.containsFunction(functionWrapper.functionName, functionWrapper.parameterNumber)) {
//                throw new CompileException(
//                        CompileError.UNDEFINED_FUNCTION, readState,
//                        "Function name: " + functionWrapper.functionName
//                                + " Parameter number: " + functionWrapper.parameterNumber);
//            }
//        }
//    }

    abstract CompileResult compile();

    protected static class CompileResult {
        final String functionName;
        final int parameterNumber;
        final ArrayList<Code> codes;
        final ReadState readState;
        CompileResult(String functionName, int parameterNumber, ArrayList<Code> codes, ReadState readState) {
            this.functionName = functionName;
            this.parameterNumber = parameterNumber;
            this.codes = codes;
            this.readState = readState;
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
    protected class LabelRecorder {
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

    protected static class ReadState {
        int pos = -1;

        int lineNumber = 1;

        int linePos = 0;

        int previousLinePos;

        char nextChar = ' '; // After read, this points to the next char to read.

        Symbol nextSymbol; // After read, this points to the next symbol to read.

        Object nextObject;

        int offset;

        int codeIndex; // The last code index
    }
}