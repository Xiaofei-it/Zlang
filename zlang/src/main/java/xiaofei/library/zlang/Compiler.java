package xiaofei.library.zlang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Xiaofei on 2017/9/9.
 */

public class Compiler {

    private static final Set<Character> SPACE_CHAR = new HashSet<Character>() {
        {
            add(' ');
            add('\t');
            add('\n');
        }
    };

    private static final Set<String> RESERVED_WORDS = new HashSet<String>() {
        {
            add("while");
        }
    };

    private int pos;

    private char nextChar;

    private String nextSymbol;

    private Object nextObject;

    private int offset;

    private Map<String, Integer> symbolTable = new HashMap<>();

    private String program;

    private ArrayList<Code> codes = new ArrayList<>();

    private int codeIndex;

    public Compiler(String program) {
        this.program = program;
        pos = -1;
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
        while (SPACE_CHAR.contains(nextChar)) {
            moveToNextChar();
        }
//        while (nextChar == '/' &&input.charAt(pos+1)=='*') {
//                getch();getch();
//                char ch1;
//                do
//                {
//                    ch1=ch;
//                    getch();
//                }while (!(ch1=='*'&&ch=='/'));
//                getch();
//                while (ch==' '||ch=='\t'||ch=='\n')
//                    getch();
//            }
//        }
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
            nextSymbol = Character.toString(nextChar);
            moveToNextChar();
            //, ) (
        }
    }

    private void generateCode(Fct fct, Object operand) {
        codes.add(new Code(fct, operand));
        ++codeIndex;
    }

    private void modifyCodeOperand(int codeIndex, Object operand) {
        codes.get(codeIndex).setOperand(operand);
    }

    private void factor() {
        if (nextSymbol.compareTo("id") == 0) {
            String id = (String) nextObject;
            moveToNextSymbol();
            if (nextSymbol.compareTo("(") == 0) {

            } else {
                Integer addr = symbolTable.get(id);
                if (addr == null) {
                    throw new CompilerException(CompilerError.UNINITIALIZED_VARIABLE, id);
                }
                generateCode(Fct.LOD, addr);
                moveToNextSymbol();
            }
        } else if (nextSymbol.compareTo("num") == 0) {
            generateCode(Fct.LIT, nextObject);
            moveToNextSymbol();
        } else if (nextSymbol.compareTo("(") == 0) {
            moveToNextSymbol();
            expression();
            if (nextSymbol.compareTo(")") == 0) {
                moveToNextSymbol();
            } else {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')'");
            }
        } else if (nextSymbol.compareTo("!")==0) {
            moveToNextSymbol();
            factor();
            generateCode(Fct.OPR, Opr.NOT);
        } else {
            throw new CompilerException(CompilerError.WRONG_SYMBOL, nextSymbol);
        }
    }

    private void term() {
        factor();
        while (nextSymbol.compareTo("*") == 0
                || nextSymbol.compareTo("/") == 0
                || nextSymbol.compareTo("&&") == 0) {
            String op = nextSymbol;
            moveToNextSymbol();
            factor();
            if (op.compareTo("*") == 0) {
                generateCode(Fct.OPR, Opr.TIMES);
            } else if (op.compareTo("/") == 0) {
                generateCode(Fct.OPR, Opr.DIVIDE);
            } else if (op.compareTo("&&") == 0) {
                generateCode(Fct.OPR, Opr.AND);
            }
        }
    }

    private void simpleExpression() {
        if (nextSymbol.compareTo("+") == 0 || nextSymbol.compareTo("-") == 0) {
            String op = nextSymbol;
            moveToNextSymbol();
            term();
            if (op.compareTo("-") == 0) {
                generateCode(Fct.OPR, Opr.NEGATIVE);
            }
        } else {
            term();
        }
        while (nextSymbol.compareTo("+") == 0
                || nextSymbol.compareTo("-") == 0
                || nextSymbol.compareTo("||") == 0) {
            String op =nextSymbol;
            moveToNextSymbol();
            term();
            if (op.compareTo("+") == 0) {
                generateCode(Fct.OPR, Opr.PLUS);
            } else if (op.compareTo("-") == 0) {
                generateCode(Fct.OPR, Opr.MINUS);
            } else if (op.compareTo("||") == 0) {
                generateCode(Fct.OPR, Opr.OR);
            }
        }
    }

    private void expression() {
        simpleExpression();
        if (nextSymbol.compareTo("==") == 0
                || nextSymbol.compareTo("!=") == 0
                || nextSymbol.compareTo("<") == 0
                || nextSymbol.compareTo(">") == 0
                || nextSymbol.compareTo("<=") == 0
                || nextSymbol.compareTo(">=") == 0) {
            String op = nextSymbol;
            moveToNextSymbol();
            simpleExpression();
            if (op.compareTo("==") == 0) {
                generateCode(Fct.OPR, Opr.EQUAL);
            } else if (op.compareTo("!=") == 0) {
                generateCode(Fct.OPR, Opr.NOT_EQUAL);
            } else if (op.compareTo("<") == 0) {
                generateCode(Fct.OPR, Opr.LESS);
            } else if (op.compareTo("<=") == 0) {
                generateCode(Fct.OPR, Opr.LESS_EQUAL);
            } else if (op.compareTo(">") == 0) {
                generateCode(Fct.OPR, Opr.GREATER);
            } else if (op.compareTo(">=") == 0) {
                generateCode(Fct.OPR, Opr.GREATER_EQUAL);
            }
        }
    }

    private void statement(boolean InLoop) {
        //; and {} is right.
        if (nextSymbol.compareTo(";") == 0) {
            moveToNextSymbol();
        } else if (nextSymbol.compareTo("id") == 0) {
            String id = (String) nextObject;
            Integer addr = symbolTable.get(id);
            if (addr == null) {
                addr = ++offset;
                symbolTable.put(id, addr);
            }
            moveToNextSymbol();
            if (nextSymbol.compareTo("=") == 0) {
                moveToNextSymbol();
                expression();
                generateCode(Fct.STO, addr);
            } else {
                throw new CompilerException(CompilerError.ASSIGN_ERROR);
            }
            if (nextSymbol.compareTo(";") != 0) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        } else if (nextSymbol.compareTo("if") == 0) {
            moveToNextSymbol();
            if (nextSymbol.compareTo("(") != 0) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "'('");
            }
            moveToNextSymbol();
            expression();
            if (nextSymbol.compareTo(")") != 0) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')'");
            }
            moveToNextSymbol();
            generateCode(Fct.JPC, 0);
            int tmp = codeIndex;
            statement(InLoop);
            modifyCodeOperand(tmp, codeIndex + 1);
            if (nextSymbol.compareTo("else") == 0) {
                modifyCodeOperand(tmp, codeIndex + 2);
                generateCode(Fct.JMP, 0);
                tmp = codeIndex;
                moveToNextSymbol();
                statement(InLoop);
                modifyCodeOperand(tmp, codeIndex + 1);
            }
        }else if (nextSymbol.compareTo("{") == 0) {
            moveToNextSymbol();
            statement(InLoop);
            if (nextSymbol.compareTo("}") != 0) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "'}'");
            }
            moveToNextSymbol();
        } else if (nextSymbol.compareTo("while") == 0) {
            int tmp1 = codeIndex;
            if (nextSymbol.compareTo("(") != 0) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "'('");
            }
            moveToNextSymbol();
            expression();
            if (nextSymbol.compareTo(")") != 0) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')'");
            }
            moveToNextSymbol();
            generateCode(Fct.JPC, 0); //false then jump
            int tmp2 = codeIndex;
//                BreakCheater.createNewLabel();
//                ContinueCheater.createNewLabel();
            statement(true);
            generateCode(Fct.JMP, tmp1);
            modifyCodeOperand(tmp2, codeIndex + 1);
//                BreakCheater.modifyBreakCmd(cx+1);
//                BreakCheater.deleteCurrentLabel();
//                ContinueCheater.modifyContinueCmd(cx1);
//                ContinueCheater.deleteCurrentLabel();
        } else if (nextSymbol.compareTo("break") == 0) {
            if (!InLoop) {
                throw new CompilerException(CompilerError.BREAK_ERROR);
            }
            generateCode(Fct.JMP, 0);
//                BreakCheater.addBreakCmd(cx);
            moveToNextSymbol();
            if (nextSymbol.compareTo(";") != 0) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        } else if (nextSymbol.compareTo("for") == 0) {//for j=a to b step c
            moveToNextSymbol();
            if (nextSymbol.compareTo("id") != 0) {
                throw new CompilerException(CompilerError.FOR_ERROR, "ID");
            }
            String id = (String) nextObject;
            Integer addr = symbolTable.get(id);
            if (addr == null) {
                symbolTable.put(id, addr = ++offset);
            }
            moveToNextSymbol();
            if (nextSymbol.compareTo("=") != 0) {
                throw new CompilerException(CompilerError.FOR_ERROR, "=");
            }
            moveToNextSymbol();
            simpleExpression();
            generateCode(Fct.STO, addr);
            int tmp1 = codeIndex + 1;
            if (nextSymbol.compareTo("to") != 0) {
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
            if (nextSymbol.compareTo("step") != 0) {
                throw new CompilerException(CompilerError.FOR_ERROR, "step");
            }
            moveToNextSymbol();
            simpleExpression();
            generateCode(Fct.LOD, addr);
            generateCode(Fct.OPR, Opr.PLUS);
            generateCode(Fct.STO, addr);
            generateCode(Fct.JMP, tmp1);
            modifyCodeOperand(tmp3, codeIndex + 1);
//                BreakCheater.createNewLabel();
//                ContinueCheater.createNewLabel();
            statement(true);
            generateCode(Fct.JMP, tmp4);
            //Code.code[cx1].aInteger=cx+1;
            modifyCodeOperand(tmp2, codeIndex + 1);
//                BreakCheater.modifyBreakCmd(cx+1);
//                BreakCheater.deleteCurrentLabel();
//                ContinueCheater.modifyContinueCmd(cx5);
//                ContinueCheater.deleteCurrentLabel();
        } else if (nextSymbol.compareTo("continue") == 0) {
            if (!InLoop) {
                throw new CompilerException(CompilerError.CONTINUE_ERROR);
            }
            generateCode(Fct.JMP, 0);
//                ContinueCheater.addContinueCmd(cx);
            moveToNextSymbol();
            if (nextSymbol.compareTo(";") != 0) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "';'");
            }
            moveToNextSymbol();
        } else if (nextSymbol.compareTo("return") == 0) {
            moveToNextSymbol();
            simpleExpression();
            generateCode(Fct.FUN_RETURN, 0);
            if (nextSymbol.compareTo(";") != 0) {
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
//            BreakCheater.init();
//            ContinueCheater.init();
        moveToNextSymbol();
        if (nextSymbol.compareTo("function") != 0) {
            throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "function");
        }
        moveToNextSymbol();
        if (nextSymbol.compareTo("id")==0) {
            moveToNextSymbol();
        } else {
            throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "ID");
        }
        String functionName = (String) nextObject;
        int para=0;
        offset = -1;
        if (nextSymbol.compareTo("(") == 0) {
            moveToNextSymbol();
        } else {
            throw new CompilerException(CompilerError.MISSING_SYMBOL, "'('");
        }
        while (nextSymbol.compareTo(")") !=0 ) {
            if (nextSymbol.compareTo("id") != 0) {
                throw new CompilerException(CompilerError.FUNCTION_DECLARATION_ERROR, "para");
            }
            String id = (String) nextObject;
            ++para;
            ++offset;
            symbolTable.put(id, offset);
            moveToNextSymbol();
            if (nextSymbol.compareTo(")") != 0 && nextSymbol.compareTo(",") != 0) {
                throw new CompilerException(CompilerError.MISSING_SYMBOL, "')' or ','");
            }
            if (nextSymbol.compareTo(",") == 0) {
                moveToNextSymbol();
            }
        }
        moveToNextSymbol();
        generateCode(Fct.INT, offset + 1);//????????????????????要不要加1
//            if (!Functions.setAfterCheck(FunctionName,para,cx))
//                throw new CompilerException(CompilerError.ParameterNumberWrong,FunctionName);
        statement(false);
        generateCode(Fct.VOID_RETURN, 0);//This is different from funReturn  here when meet this, is a error.
    }

    public void compile(String program) {

    }

}
