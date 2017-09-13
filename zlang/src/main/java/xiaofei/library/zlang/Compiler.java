package xiaofei.library.zlang;

import java.util.HashSet;
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

    private String program;

    public Compiler(String program) {
        this.program = program;
        pos = -1;
    }

    private void moveToNextChar() {
        ++pos;
        if (pos == program.length()) {
            throw new CompilerException(CompilerError.INCOMPLETE_PROGRAM);
        }
		nextChar = program.charAt(pos);
    }

    private static boolean isAlpha(char ch) {
        return ch == '_' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
    }

    private static boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
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
    public void compile(String program) {

    }

}
