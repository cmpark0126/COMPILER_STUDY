import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

public class MyScanner {
    private BufferedReader m_bufReader = null;
    private String m_curLine = "";
    private int m_curLinenum = 0;
    private int m_lineLength = 0;
    private int m_startIdx = 0;
    private int m_endIdx = 0;
    private int m_typeOfDelimiter = 0;
    private int m_specialState = 0;
    private String m_token = "";
    private HashMap<String, String> m_reservedSymbolMap = null;
    private HashMap<Integer, String> m_delimiterMap = null;

    private static final int DIVISOR = 0x100;
    private static final int LOOP_BREAKER = 0xff;

    // type of delimiter
    private static final int ERROR = 0xff00; // rarely use group state
    private static final int DELIMITER = 0xff01;
    private static final int SKIP = 0xff02;
    private static final int DELIMITER_FROM_DFA_OF_NUMBER = 0xff03;
    private static final int DELIMITER_FROM_DFA_OF_LITERAL = 0xff04;
    private static final int DELIMITER_WITH_STARTING_ANNOTATION = 0xff05;
    private static final int DELIMITER_WITH_DOUBLEQOUTE_BEFORE_OPENBRAKET = 0xff06; // "<
    private static final int DELIMITER_WITH_END_OF_LITERAL = 0xff07; // ......"
    private static final int DELIMITER_WITH_TAG_FORM = 0xff08; // <...>

    // type of specialState
    private static final int IS_NORMAL_STATE = 0x00;
    private static final int IS_FIRST_TOKEN_CURRENT_LINE = 0x01;
    private static final int IS_IN_LITERAL = 0x02;

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        String filename = "";
        File file = null;
        MyScanner scanner = null;
        InfoOfToken info = null;

        try {
            System.out.print("Enter the filename: ");
            filename = kb.nextLine();
            file = new File(filename);

            scanner = new MyScanner(file);
            // Need to open java script file
            while(true) {
                info = scanner.Scan();
                if(info == null) break;
                scanner.AnalyzeToken(info.m_token, info.m_typeOfDelimiter);
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
            m_bufReader = new BufferedReader(fileReader);
            m_reservedSymbolMap = new HashMap<>();
            m_delimiterMap = new HashMap<>();
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
            // resulved symbol
            m_reservedSymbolMap.put("<script_start>","script start keyword");
            m_reservedSymbolMap.put("<script_end>","script end keyword");
            m_reservedSymbolMap.put("var","keyword");
            m_reservedSymbolMap.put("function","keyword");

            // use for special statement
            m_reservedSymbolMap.put("if","if keyword");
            m_reservedSymbolMap.put("for","for keyword");
            m_reservedSymbolMap.put("switch","switch keyword");
            m_reservedSymbolMap.put("case","case keyword used at switch statement");
            m_reservedSymbolMap.put("break","escape loop keyword");
            m_reservedSymbolMap.put("default","default case keyword used at switch statement");
            m_reservedSymbolMap.put("do","do keyword used at do-while statement");
            m_reservedSymbolMap.put("while","while loop keyword used at while statement of do-while statement");

            // boolean symbol
            m_reservedSymbolMap.put("false","boolean false keyword");
            m_reservedSymbolMap.put("true","boolean true keyword");

            // for function return
            m_reservedSymbolMap.put("return","return appropriate value to caller function and escape current function");

            // special character
            m_reservedSymbolMap.put("(","left parentheses character");
            m_reservedSymbolMap.put(")","right parentheses character");
            m_reservedSymbolMap.put("{","left brace character");
            m_reservedSymbolMap.put("}","right brace character");
            m_reservedSymbolMap.put(";","semi colon character");
            m_reservedSymbolMap.put(":","colon character");
            m_reservedSymbolMap.put(",","punctuation character");

            // operator and sign
            m_reservedSymbolMap.put("=","Assignment operator");
            m_reservedSymbolMap.put("==","equal to");
            m_reservedSymbolMap.put(">","greater than");
            m_reservedSymbolMap.put(">=","greater than or equal to");
            m_reservedSymbolMap.put(">>","Right shift");
            m_reservedSymbolMap.put(">>>","operator");
            m_reservedSymbolMap.put("<","less than");
            m_reservedSymbolMap.put("<=","less than or equal to");
            m_reservedSymbolMap.put("<<","Left shift");
            m_reservedSymbolMap.put("+","Addition operator");
            m_reservedSymbolMap.put("+=","Addition Assignment operator");
            m_reservedSymbolMap.put("++","Increment operator");
            m_reservedSymbolMap.put("-","Subtraction operator");
            m_reservedSymbolMap.put("-=","SubtractionAssignment operator");
            m_reservedSymbolMap.put("--","Decrement operator");
            m_reservedSymbolMap.put("*","Multiplication operator");
            m_reservedSymbolMap.put("*=","Multiplication Assignment operator");
            m_reservedSymbolMap.put("/","Division operator");
            m_reservedSymbolMap.put("/=","Division Assignment operator");
            m_reservedSymbolMap.put("%","Modulus operator");
            m_reservedSymbolMap.put("%=","Modulus Assignment operator");

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }
        return true;
    }

    public boolean InitializeDelimiterMap(){
        try {
            m_delimiterMap.put(DELIMITER_FROM_DFA_OF_NUMBER,"number");
            m_delimiterMap.put(DELIMITER_FROM_DFA_OF_LITERAL,"literal");
            m_delimiterMap.put(DELIMITER_WITH_STARTING_ANNOTATION,"comment");
            m_delimiterMap.put(DELIMITER_WITH_TAG_FORM,"keyword tag name");

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }
        return true;
    }

    public InfoOfToken Scan(){
        InfoOfToken info = null;
        try {
            while(true){
                if (m_startIdx >= m_lineLength){
                    // System.out.println("m_startIdx : " + m_startIdx + "; m_lineLength : " + m_lineLength);
                    do{
                        m_curLinenum++;
                        if((m_curLine = m_bufReader.readLine()) == null) return null;
                        // m_curLine := nextLine from file pointer
                        m_lineLength = m_curLine.length(); // initialization
                        m_startIdx = 0; // initialization
                        m_endIdx = 0; // initialization
                    } while(m_lineLength <= 0);
                    m_specialState = IS_FIRST_TOKEN_CURRENT_LINE;
                }
                info = Scan(m_curLine, m_startIdx, m_endIdx, m_specialState);

                // select action as per dilimiter and curren special state
                m_typeOfDelimiter = info.m_typeOfDelimiter;
                if(m_typeOfDelimiter == SKIP) {
                    m_endIdx = info.m_endIdx;
                    m_startIdx = m_endIdx;
                } else if (m_typeOfDelimiter == DELIMITER_WITH_DOUBLEQOUTE_BEFORE_OPENBRAKET){
                    m_endIdx = info.m_endIdx;
                    m_startIdx = m_endIdx;
                    m_specialState = IS_IN_LITERAL;
                } else if (m_typeOfDelimiter == DELIMITER_WITH_END_OF_LITERAL){
                    m_endIdx = info.m_endIdx;
                    m_startIdx = m_endIdx;
                    m_specialState = IS_NORMAL_STATE;
                } else if (m_typeOfDelimiter == DELIMITER_WITH_STARTING_ANNOTATION){
                    info.m_endIdx = m_lineLength;
                    break;
                } else if (m_specialState == IS_IN_LITERAL && m_typeOfDelimiter != DELIMITER_WITH_TAG_FORM){
                    m_endIdx = info.m_endIdx;
                    m_startIdx = m_endIdx;
                } else break;
            }
            if(m_specialState == IS_FIRST_TOKEN_CURRENT_LINE) m_specialState = IS_NORMAL_STATE;


            m_endIdx = info.m_endIdx;
            info.m_token = m_curLine.substring(m_startIdx, m_endIdx);
            AnalyzeToken(info.m_token, info.m_typeOfDelimiter);
            info.m_symbolInfo = m_reservedSymbolMap.get(info.m_token);
            m_startIdx = m_endIdx;

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        return info;
    }

    public static InfoOfToken Scan(String line, int startIdx, int endIdx, int specialState){
        int sizeOfLine = line.length();
        int curState = 0;
        InfoOfToken info = null;
        try {
            // recognize special state
            // for <script_start> <script_end>
            // System.out.println(String.format("0x%04X", specialState));
            curState = CalculateNextState(curState, specialState);

            for(int i = startIdx; i < sizeOfLine; i++){
                // System.out.println(String.format("0x%04X", curState));
                curState = FindNextState(line.charAt(i), curState);
                if((curState / DIVISOR) == LOOP_BREAKER) { // special state case check
                    if(curState == ERROR){
                        System.out.println(line.substring(startIdx, endIdx + 1) + " is Rejected! 1");
                        System.exit(-1);
                    }
                    break;
                }
                endIdx++;
            }
            // System.out.println(String.format("0x%04X", curState));

            if (endIdx == sizeOfLine){
                if(FindNextState(' ', curState) / DIVISOR != LOOP_BREAKER) {
                    // System.out.println(String.format("0x%04X", curState));
                    System.out.println(line.substring(startIdx, endIdx) + " is Rejected! 2");
                    System.exit(-1);
                } else if(specialState == IS_IN_LITERAL && curState != DELIMITER_WITH_END_OF_LITERAL){
                    // System.out.println(String.format("0x%04X", curState));
                    System.out.println(line.substring(startIdx, endIdx) + " is need \'\"\' symbol to close sentence");
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
        // do not consider Grammar issue
        int nextState = 0;
        try {
            switch (curState / DIVISOR) { // Need to reduce redundancy
                case 0x00: if(IsDigit(ch)) nextState = CalculateNextState(0x01);
                           else if(IsLetter(ch) || IsSpecialCharForId(ch)) nextState = CalculateNextState(0x02);
                           else if(IsSpecialChar(ch)) nextState = CalculateNextState(0x03);
                           else if(ch == '\"') {
                               if((curState % DIVISOR) == IS_NORMAL_STATE) nextState = CalculateNextState(0x04);
                               else if((curState % DIVISOR) == IS_IN_LITERAL) nextState = CalculateNextState(0x0e);
                           }
                           else if(ch == '=') nextState = CalculateNextState(0x05);
                           else if(ch == '>') nextState = CalculateNextState(0x06);
                           else if(ch == '<') {
                               if((curState % DIVISOR) == IS_NORMAL_STATE) nextState = CalculateNextState(0x07);
                               else if((curState % DIVISOR) == IS_FIRST_TOKEN_CURRENT_LINE) nextState = CalculateNextState(0x0d);
                               else if((curState % DIVISOR) == IS_IN_LITERAL) nextState = CalculateNextState(0x0d); // case is similar in 0x01
                           }
                           else if(ch == '+') nextState = CalculateNextState(0x08);
                           else if(ch == '-') nextState = CalculateNextState(0x09);
                           else if(ch == '*' || ch == '%') nextState = CalculateNextState(0x0a);
                           else if(IsBlankChar(ch)) nextState = CalculateNextState(0x0b);
                           else if(ch == '/') nextState = CalculateNextState(0x0c);
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
                case 0x0c: nextState = DFAForSlashChar(ch, curState);
                           break;
                case 0x0d: nextState = DFAForSpecialKeywordForRecognizingScriptCode(ch, curState);
                           break;
                case 0x0e: nextState = DELIMITER_WITH_END_OF_LITERAL;
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
                           else if(IsSpecialChar(ch) || IsOperatorOrSign(ch) || IsBlankChar(ch)) nextState = DELIMITER_FROM_DFA_OF_NUMBER;
                           else nextState = ERROR;
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
                           else if(IsSpecialChar(ch) || IsOperatorOrSign(ch) || IsBlankChar(ch) || ch == '\"') nextState = DELIMITER;
                           else nextState = ERROR;
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
                case 0x00: if(ch == '\"') nextState = CalculateNextState(curState, 0x02);
                           // else if(ch == '<') nextState = DELIMITER_WITH_DOUBLEQOUTE_BEFORE_OPENBRAKET;
                           else nextState = CalculateNextState(curState, 0x01);
                           break;
                case 0x01: if(ch == '\"') nextState = CalculateNextState(curState, 0x02);
                           else nextState = curState;
                           break;
                case 0x02: nextState = DELIMITER_FROM_DFA_OF_LITERAL;
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

    public static int DFAForSlashChar(char ch, int curState){
        int nextState = 0;
        try {
            switch (curState % DIVISOR) { // Need to reduce redundancy
                case 0x00: if(ch == '=') nextState = CalculateNextState(curState, 0x01);
                           else if(ch == '/') nextState = CalculateNextState(curState, 0x02);
                           else if(IsOperatorOrSign(ch)) nextState = ERROR;
                           else nextState = DELIMITER;
                           break;
                case 0x01: if(IsOperatorOrSign(ch)) nextState = ERROR;
                           else nextState = DELIMITER;
                           break;
                case 0x02: nextState = DELIMITER_WITH_STARTING_ANNOTATION;
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


    public static int DFAForSpecialKeywordForRecognizingScriptCode(char ch, int curState){
        int nextState = 0;
        try {
            switch (curState % DIVISOR) { // Need to reduce redundancy
                case 0x00: if(IsLetter(ch) || IsSpecialCharForId(ch) || IsDigit(ch) || ch == '/' || IsBlankChar(ch)) nextState = curState;
                           else if(ch == '>') nextState = CalculateNextState(curState, 0x01);
                           else nextState = ERROR;
                           break;
                case 0x01: nextState = DELIMITER_WITH_TAG_FORM;
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
            if((infoOfToken = m_reservedSymbolMap.get(token)) != null) {
                // System.out.println(token + " : " + infoOfToken);
                return;
            }
            else if((infoOfToken = m_delimiterMap.get(typeOfDelimiter)) != null){
                m_reservedSymbolMap.put(token, infoOfToken);
                // System.out.println(token + " : " + infoOfToken);
            }
            else {
                m_reservedSymbolMap.put(token,"user-defined id");
                // System.out.println(token + " : " + m_reservedSymbolMap.get(token));
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        return;
    }

    public String GetCurLine(){
        return m_curLine;
    }

    public int GetCurLineNum(){
        return m_curLinenum;
    }

    // public String GetReservedSymbolInfo(String token){
    //     return m_reservedSymbolMap.get(token);
    // }
}

class InfoOfToken {
    public int m_typeOfDelimiter = 0;
    public int m_endIdx = 0;
    public String m_token = "";
    public String m_symbolInfo = "";
}
