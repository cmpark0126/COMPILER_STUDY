import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MyScanner {
private BufferedReader m_bufReader = null;
private String m_curLine = "";
private int m_lineLength = 0;
private int m_startIdx = 0;
private int m_endIdx = 0;
private int m_typeOfDelimiter = 0;
private String m_token = "";
HashMap<String, String> m_reservedSymbolMap = null;
HashMap<Integer, String> m_delimiterMap = null;

private static final int DIVISOR = 0x100;

private static final int ERROR = 0xff00; // rarely use group state
private static final int DELIMITER = 0xff01;
private static final int SKIP = 0xff02;
private static final int DELIMITER_FROM_DFA_OF_LITERAL = 0xff03;


public static void main(String[] args) {
    Scanner kb = new Scanner(System.in);
    String filename = "";
    File file = null;
    MyScanner scanner = null;
    String token = "";

    try {
        System.out.println("Sample running result is given below:");
        filename = kb.nextLine();
        file = new File(filename);

        scanner = new MyScanner(file);
        // Need to open java script file
        while(true) {
            token = scanner.Scan();
            if(token == null) break;
        }

    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }

    kb.close();
}

public MyScanner(File file){
    FileReader fileReader = null;
    try {
        fileReader = new FileReader(file);
        this.m_bufReader = new BufferedReader(fileReader);
        InitializeReservedSymbolMap();
        InitializeDelimiterMap();
    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
}

public boolean InitializeReservedSymbolMap(){
    try {
        m_reservedSymbolMap = new HashMap<>();

        // resulved symbol
        m_reservedSymbolMap.put("<script_start>","keyword");
        m_reservedSymbolMap.put("var","keyword");
        m_reservedSymbolMap.put("function","keyword");

        // use for special statement
        m_reservedSymbolMap.put("if","keyword");
        m_reservedSymbolMap.put("for","keyword");
        m_reservedSymbolMap.put("switch","keyword");
        m_reservedSymbolMap.put("case","keyword");
        m_reservedSymbolMap.put("break","keyword");
        m_reservedSymbolMap.put("default","keyword");
        m_reservedSymbolMap.put("do","keyword");
        m_reservedSymbolMap.put("while","keyword");

        // boolean symbol
        m_reservedSymbolMap.put("false","keyword");
        m_reservedSymbolMap.put("true","keyword");

        // for function return
        m_reservedSymbolMap.put("return","keyword");

        // special character
        m_reservedSymbolMap.put("(","character");
        m_reservedSymbolMap.put(")","character");
        m_reservedSymbolMap.put("{","character");
        m_reservedSymbolMap.put("}","character");
        m_reservedSymbolMap.put(";","character");
        m_reservedSymbolMap.put(":","character");
        m_reservedSymbolMap.put(",","character");

        // operator and sign
        m_reservedSymbolMap.put("=","operator");
        m_reservedSymbolMap.put("==","operator");
        m_reservedSymbolMap.put(">","operator");
        m_reservedSymbolMap.put(">=","operator");
        m_reservedSymbolMap.put(">>","operator");
        m_reservedSymbolMap.put(">>>","operator");
        m_reservedSymbolMap.put("<","operator");
        m_reservedSymbolMap.put("<=","operator");
        m_reservedSymbolMap.put("<<","operator");
        m_reservedSymbolMap.put("+","operator");
        m_reservedSymbolMap.put("+=","operator");
        m_reservedSymbolMap.put("++","operator");
        m_reservedSymbolMap.put("-","operator");
        m_reservedSymbolMap.put("-=","operator");
        m_reservedSymbolMap.put("--","operator");
        m_reservedSymbolMap.put("*","operator");
        m_reservedSymbolMap.put("*=","operator");
        m_reservedSymbolMap.put("/","operator");
        m_reservedSymbolMap.put("/=","operator");
        m_reservedSymbolMap.put("%","operator");
        m_reservedSymbolMap.put("%=","operator");

    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return true;
}

public boolean InitializeDelimiterMap(){
    try {
        m_delimiterMap = new HashMap<>();

        // resulved symbol
        m_delimiterMap.put(DELIMITER_FROM_DFA_OF_LITERAL,"literal");

    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }
    return true;
}

public String Scan(){
    InfoOfToken info = null;
    try {
        while(true){
            // System.out.println("m_startIdx : " + m_startIdx + "; m_lineLength : " + m_lineLength);
            if (m_startIdx >= m_lineLength){
                do{
                    if((m_curLine = m_bufReader.readLine()) == null) return null;
                    // m_curLine := nextLine from file pointer
                    m_lineLength = m_curLine.length(); // initialization
                    m_startIdx = 0; // initialization
                    m_endIdx = 0; // initialization
                } while(m_lineLength <= 0);
            }
            info = Scan(m_curLine, m_startIdx, m_endIdx);
            m_typeOfDelimiter = info.m_typeOfDelimiter;
            if(m_typeOfDelimiter != SKIP) break;

            m_endIdx = info.m_endIdx;
            m_startIdx = m_endIdx;
        }

        m_endIdx = info.m_endIdx;
        m_token = m_curLine.substring(m_startIdx, m_endIdx);
        m_startIdx = m_endIdx;

        AnalyzeToken(m_token, m_typeOfDelimiter); // after every implementation, we need to check blank character;

    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }

    return m_token;
}

public static InfoOfToken Scan(String line, int startIdx, int endIdx){
    int sizeOfLine = line.length();
    int curState = 0;
    InfoOfToken info = null;
    try {
        for(int i = startIdx; i < sizeOfLine; i++){
            curState = FindNextState(line.charAt(i), curState);
            // System.out.println(String.format("0x%08X", curState));
            if((curState / 0x100) == 0xff) { // special state case check
                if(curState == ERROR){
                    System.out.println(line.substring(startIdx, endIdx + 1) + " is Rejected! 1");
                    System.exit(-1);
                }
                break;
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

        info = new InfoOfToken();
        info.m_typeOfDelimiter = curState;
        info.m_endIdx = endIdx;

    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }

    return info;
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
                       else if(IsBlankChar(ch)) nextState = CalculateNextState(0x0b);
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
            case 0x0b: nextState = DFAForBlankAndTab(ch, curState);
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
                       else if(IsSpecialChar(ch) || IsOperatorOrSign(ch) || IsBlankChar(ch)) nextState = DELIMITER;
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
                       else if(IsSpecialChar(ch) || IsOperatorOrSign(ch) || IsBlankChar(ch)) nextState = DELIMITER;
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
            case 0x01: nextState = DELIMITER_FROM_DFA_OF_LITERAL;
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

public static int DFAForBlankAndTab(char ch, int curState){
    int nextState = 0;
    try {
        switch (curState % DIVISOR) { // Need to reduce redundancy
            case 0x00: if(IsBlankChar(ch)) nextState = curState;
                       else nextState = SKIP;
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
            ch == ',' )? true : false;
}

public static boolean IsOperatorOrSign(char ch){
    return (ch == '=' || ch == '>' ||
            ch == '<' || ch == '+' ||
            ch == '-' || ch == '*' ||
            ch == '/' || ch == '%' )? true : false;
}

public static boolean IsBlankChar(char ch){
    return (ch == ' ' || ch == '\t')? true : false;
}

public void AnalyzeToken(String token, int typeOfDelimiter){
    String infoOfToken = "";
    try {
        if ((infoOfToken = m_delimiterMap.get(typeOfDelimiter)) != null){
            System.out.println(token + " : " + infoOfToken);
        }
        else {
            if((infoOfToken = infoOfToken = m_reservedSymbolMap.get(token)) != null) System.out.println(token + " : " + infoOfToken);
            else {
                m_reservedSymbolMap.put(token,"user-defined id");
                System.out.println(token + " : " + m_reservedSymbolMap.get(token));
            }
        }

    } catch(Exception e) {
        e.printStackTrace();
        System.out.println(e);
        System.exit(-1);
    }

    return;
}

}

class InfoOfToken {
    public int m_typeOfDelimiter = 0;
    public int m_endIdx = 0;
}
