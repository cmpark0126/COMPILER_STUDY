import java.util.Scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MyScanner {
private File m_file = null;
private String m_curLine = null;
private int m_lineLength = 0;
private int m_startIdx = 0;
private int m_endIdx = 0;
private Scanner kb = null; // dummy

private static final int DIVISOR = 0x100;
private static final int DELIMITER = 0xff00; // rarely use state
private static final int ERROR = 0xfe00;

public static void main(String[] args) {
    try {
        File file = null; // dummy
        MyScanner scanner = new MyScanner(file);
        // Need to open java script file
        while(true) {
            scanner.Scan();
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
}

public MyScanner(File file){
    try {
        this.m_file = file;
        this.kb = new Scanner(System.in);
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
}

public String Scan(){
    String token = null;
    try {
        // System.out.println("m_startIdx : " + m_startIdx + "; m_lineLength : " + m_lineLength);
        if (m_startIdx >= m_lineLength){
            // m_curLine := nextLine from file pointer
            m_curLine = kb.nextLine(); // dummy
            m_lineLength = m_curLine.length(); // initialization
            m_startIdx = 0; // initialization
            m_endIdx = 0; // initialization
        }
        m_endIdx = Scan(m_curLine, m_startIdx, m_endIdx);
        token = m_curLine.substring(m_startIdx, m_endIdx);
        AnalyzeToken(token); // after every implementation, we need to check blank character;

        m_startIdx = m_endIdx;
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }

    return token;
}

public static int Scan(String line, int startIdx, int endIdx){
    int sizeOfLine = line.length();
    int curState = 0;
    try {
        for(int i = startIdx; i < sizeOfLine; i++){
            curState = FindNextState(line.charAt(i), curState);
            // System.out.println(String.format("0x%08X", curState));
            if(curState == DELIMITER) {
                break;
            }
            else if(curState == ERROR) {
                System.out.println(line.substring(startIdx, endIdx + 1) + " is Rejected! 1");
                System.exit(-1);
            }
            endIdx++;
        }

        if (endIdx == sizeOfLine){
            if(FindNextState(' ', curState) != DELIMITER) {
                // System.out.println(String.format("0x%08X", curState));
                System.out.println(line.substring(startIdx, endIdx) + " is Rejected! 2");
                System.exit(-1);
            }
        }

    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }

    return endIdx;
}

public static int CalculateNextState(int groupState){
    return groupState * DIVISOR;
}

public static int CalculateNextState(int curState, int nextLocalState){
    return CalculateNextState(curState / DIVISOR) + nextLocalState;
}

public static int FindNextState(char ch, int curState){
    // we need to simplify the algorithm, and increase extencability
    // do not consider grammer issue
    int nextState = 0;
    try {
        switch (curState / DIVISOR) { // Need to reduce redundancy
            case 0x00: if(IsDigit(ch)) nextState = CalculateNextState(0x01);
                       else if(IsLetter(ch) || IsSpecialCharForId(ch)) nextState = CalculateNextState(0x02);
                       else if(IsSpecialChar(ch)) nextState = CalculateNextState(0x03);
                       else if(ch == '\"') nextState = CalculateNextState(0x04);
                       else if(ch == '=') nextState = CalculateNextState(0x05);
                       else if(ch == '>') nextState = CalculateNextState(0x06);
                       else if(ch == '<') nextState = CalculateNextState(0x07);
                       else if(ch == '+') nextState = CalculateNextState(0x08);
                       else if(ch == '-') nextState = CalculateNextState(0x09);
                       else if(ch == '*' || ch == '/' || ch == '%') nextState = CalculateNextState(0x0a);
                       else nextState = ERROR;
                       break;
            case 0x01: nextState = DFAForNumber(ch, curState);
                       break;
            case 0x02: nextState = DFAForId(ch, curState);
                       break;
            case 0x03: nextState = DFAForSpecialChar(ch, curState);
                       break;
            case 0x04: nextState = DFAForLiteral(ch, curState);
                       break;
            case 0x05: nextState = DFAForStartWithEqualSign(ch, curState);
                       break;
            case 0x06: nextState = DFAForStartWithGreaterThan(ch, curState);
                       break;
            case 0x07: nextState = DFAForStartWithLessThan(ch, curState);
                       break;
            case 0x08: nextState = DFAForStartWithPlusSign(ch, curState);
                       break;
            case 0x09: nextState = DFAForStartWithMinusSign(ch, curState);
                       break;
            case 0x0a: nextState = DFAForStartWithOtherSign(ch, curState);
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }

    return nextState;
}

public static int DFAForNumber(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(IsDigit(ch)) nextState = curState;
                       else if(ch == '.') nextState = CalculateNextState(curState, 0x01);
                       else if(IsSpecialChar(ch) || IsOperatorOrSign(ch)) nextState = DELIMITER;
                       break;
            case 0x01: if(IsDigit(ch)) nextState = CalculateNextState(curState, 0x00);
                       else nextState = ERROR;
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return nextState;
}

public static int DFAForId(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(IsLetter(ch) || IsSpecialCharForId(ch) || IsDigit(ch) ) nextState = curState;
                       else if(ch == '.') nextState = CalculateNextState(curState, 0x01);
                       else if(IsSpecialChar(ch) || IsOperatorOrSign(ch)) nextState = DELIMITER;
                       break;
            case 0x01: if(IsLetter(ch)) nextState = CalculateNextState(curState, 0x00);
                       else nextState = ERROR;
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return nextState;
}

public static int DFAForSpecialChar(char ch, int curState){
    return DELIMITER;
}

public static int DFAForLiteral(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(ch == '\"') nextState = CalculateNextState(curState, 0x01);
                       else nextState = curState;
                       break;
            case 0x01: nextState = DELIMITER;
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return nextState;
}

public static int DFAForStartWithEqualSign(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(ch == '=') nextState = CalculateNextState(curState, 0x01);
                       else if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            case 0x01: if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return nextState;
}

public static int DFAForStartWithGreaterThan(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(ch == '=') nextState = CalculateNextState(curState, 0x01);
                       else if(ch == '>') nextState = CalculateNextState(curState, 0x02);
                       else if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            case 0x01: if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            case 0x02: if(ch == '>') nextState = CalculateNextState(curState, 0x01);
                       else if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return nextState;
}

public static int DFAForStartWithLessThan(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(ch == '=') nextState = CalculateNextState(curState, 0x01);
                       else if(ch == '<') nextState = CalculateNextState(curState, 0x01);
                       else if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            case 0x01: if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return nextState;
}

public static int DFAForStartWithPlusSign(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(ch == '=' || ch == '+') nextState = CalculateNextState(curState, 0x01);
                       else if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            case 0x01: if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return nextState;
}

public static int DFAForStartWithMinusSign(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(ch == '=' || ch == '-') nextState = CalculateNextState(curState, 0x01);
                       else if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            case 0x01: if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return nextState;
}

public static int DFAForStartWithOtherSign(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(ch == '=') nextState = CalculateNextState(curState, 0x01);
                       else if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            case 0x01: if(IsOperatorOrSign(ch)) nextState = ERROR;
                       else nextState = DELIMITER;
                       break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return nextState;
}

public static boolean IsDigit(char ch){
    return (ch >= 48 && ch <= 57)? true : false;
}

public static boolean IsLetter(char ch){
    return ((ch >= 65 && ch <= 90) ||
            (ch >= 97 && ch <= 122))? true : false;
}

public static boolean IsSpecialCharForId(char ch){
    return (ch == '_' || ch == '$')? true : false;
}

public static boolean IsSpecialChar(char ch){
    return (ch == '(' || ch == ')' ||
            ch == '{' || ch == '}' ||
            ch == ';' || ch == ':' ||
            ch == ',' || ch == ' ' )? true : false;
}

public static boolean IsOperatorOrSign(char ch){
    return (ch == '=' || ch == '>' ||
            ch == '<' || ch == '+' ||
            ch == '-' || ch == '*' ||
            ch == '/' || ch == '%' )? true : false;
}

public static void AnalyzeToken(String token){
    try {
        System.out.println("analysis: " + token);
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }

    return;
}

}
