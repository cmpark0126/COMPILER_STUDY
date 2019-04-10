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
    private int m_lineLength = 0;
    private int m_startIdx = 0;
    private int m_endIdx = 0;
    private int m_typeOfDelimiter = 0;
    private String m_token = "";
    private HashMap<String, String> m_reservedSymbolMap = null;
    private HashMap<Integer, String> m_delimiterMap = null;

    private static final int DIVISOR = 0x100;
    private static final int LOOP_BREAKER = 0xff;

    private static final int ERROR = 0xff00; // rarely use group state
    private static final int DELIMITER = 0xff01;
    private static final int SKIP = 0xff02;
    private static final int DELIMITER_FROM_DFA_OF_NUMBER = 0xff03;
    private static final int DELIMITER_FROM_DFA_OF_LITERAL = 0xff04;
    private static final int DELIMITER_WITH_STARTING_ANNOTATION = 0xff05;

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        String filename = "";
        File file = null;
        MyScanner scanner = null;
        InfoOfToken info = null;
        SimpleGrammarChecker sgc = null;

        try {
            System.out.println("Sample running result is given below:");
            filename = kb.nextLine();
            file = new File(filename);

            scanner = new MyScanner(file);
            // Need to open java script file
            sgc = new SimpleGrammarChecker();
            while(true) {
                info = scanner.Scan();
                if(info == null) break;
                scanner.AnalyzeToken(info.m_token, info.m_typeOfDelimiter);
                // if(sgc.ScanWithGrammarChecker(scanner)) break;
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

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }
        return true;
    }

    public InfoOfToken Scan(){
        InfoOfToken info = null;
        boolean isStartToken = true;
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
                    isStartToken = true;
                }
                info = Scan(m_curLine, m_startIdx, m_endIdx, isStartToken);
                m_typeOfDelimiter = info.m_typeOfDelimiter;
                if(m_typeOfDelimiter == SKIP) {
                    m_endIdx = info.m_endIdx;
                    m_startIdx = m_endIdx;
                } else if (m_typeOfDelimiter == DELIMITER_WITH_STARTING_ANNOTATION){
                    info.m_endIdx = m_lineLength;
                    break;
                } else break;
            }
            isStartToken = false;

            m_endIdx = info.m_endIdx;
            info.m_token = m_curLine.substring(m_startIdx, m_endIdx);
            m_startIdx = m_endIdx;

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        return info;
    }

    public InfoOfToken Scan(String line, int startIdx, int endIdx, boolean isStartToken){
        int sizeOfLine = line.length();
        int curState = 0;
        InfoOfToken info = null;
        try {
            // realize this now analyzing token is first token in line;
            // for <script_start> <script_end>
            if (isStartToken) curState = CalculateNextState(curState, 0x01);

            for(int i = startIdx; i < sizeOfLine; i++){
                curState = FindNextState(line.charAt(i), curState);
                // System.out.println(String.format("0x%08X", curState));
                if((curState / DIVISOR) == LOOP_BREAKER) { // special state case check
                    if(curState == ERROR){
                        System.out.println(line.substring(startIdx, endIdx + 1) + " is Rejected! 1");
                        System.exit(-1);
                    }
                    break;
                }
                endIdx++;
            }

            if (endIdx == sizeOfLine){
                if(FindNextState(' ', curState) / DIVISOR != LOOP_BREAKER) {
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
        // do not consider Grammar issue
        int nextState = 0;
        try {
            switch (curState / DIVISOR) { // Need to reduce redundancy
                case 0x00: if(IsDigit(ch)) nextState = CalculateNextState(0x01);
                           else if(IsLetter(ch) || IsSpecialCharForId(ch)) nextState = CalculateNextState(0x02);
                           else if(IsSpecialChar(ch)) nextState = CalculateNextState(0x03);
                           else if(ch == '\"') nextState = CalculateNextState(0x04);
                           else if(ch == '=') nextState = CalculateNextState(0x05);
                           else if(ch == '>') nextState = CalculateNextState(0x06);
                           else if(ch == '<') {
                               if((curState % DIVISOR) == 0x00) nextState = CalculateNextState(0x07);
                               if((curState % DIVISOR) == 0x01) nextState = CalculateNextState(0x0d);
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
                case 0x00: if(IsLetter(ch) || IsSpecialCharForId(ch)) nextState = curState;
                           else if(ch == '>') nextState = CalculateNextState(curState, 0x01);
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
        ScanLiteral scanner = null;
        try {
            // for Optional task
            if(typeOfDelimiter == DELIMITER_FROM_DFA_OF_LITERAL){
                scanner = new ScanLiteral(token);
                if(scanner.Scan() == true) return;
            }

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
    public String m_token = "";
}

// class SimpleGrammarChecker {
//     private ArrayList<InfoOfToken> m_arrayOfToken = null;
//     private HashMap<String, Integer> m_tokenMap = null;
//     private int m_curState = 0;
//
//     private static final int DELIMITER = -1;
//     private static final int DELIMITER_WITH_NEW_TOKEN = -2;
//     private static final int ERROR = -3;
//
//     public SimpleGrammarChecker(){
//         try {
//             m_arrayOfToken = new ArrayList<InfoOfToken>();
//             m_tokenMap = new HashMap<>();
//             InitializeTokenMap();
//         } catch(Exception e) {
//             e.printStackTrace();
//             System.out.println(e);
//             System.exit(-1);
//         }
//     }
//
//     public boolean InitializeTokenMap(){
//         try {
//             m_tokenMap.put("<", 1);
//             m_tokenMap.put("/", 2);
//             m_tokenMap.put(">", 3);
//
//         } catch(Exception e) {
//             e.printStackTrace();
//             System.out.println(e);
//             System.exit(-1);
//         }
//         return true;
//     }
//
//     public boolean ScanWithGrammarChecker(MyScanner scanner){
//         InfoOfToken info = null;
//         Integer mappedId = null;
//         int curState = 0;
//         StringBuffer sb = null;
//         boolean isEnd = false;
//         try {
//             while(true) {
//                 info = scanner.Scan();
//                 if(info == null) {isEnd = true; break;}
//                 m_arrayOfToken.add(info);
//
//                 // Grammar check
//                 // System.out.println(info.m_token);
//                 mappedId = m_tokenMap.get(info.m_token);
//                 if(mappedId == null) mappedId = new Integer(0);
//                 // System.out.println(mappedId.intValue());
//                 m_curState = FindNextState(mappedId.intValue(), m_curState);
//                 // System.out.println(m_curState);
//
//                 if(m_curState == DELIMITER || m_curState == DELIMITER_WITH_NEW_TOKEN) break;
//                 else if(m_curState == ERROR) {
//                     System.out.println("error");
//                     System.exit(-1);
//                 }
//             }
//             // System.out.println("============");
//
//             if(m_curState == DELIMITER || isEnd == true) {
//                 for(InfoOfToken subInfo: m_arrayOfToken){
//                     scanner.AnalyzeToken(subInfo.m_token, subInfo.m_typeOfDelimiter);
//                 }
//             } else if(m_curState == DELIMITER_WITH_NEW_TOKEN) {
//                 sb = new StringBuffer();
//                 for(InfoOfToken subInfo: m_arrayOfToken){
//                     sb.append(subInfo.m_token);
//                 }
//                 scanner.AnalyzeToken(sb.toString(), 0xff00);
//             }
//
//             m_arrayOfToken.clear();
//             m_curState = 0;
//
//         } catch(Exception e) {
//             e.printStackTrace();
//             System.out.println(e);
//             System.exit(-1);
//         }
//         return isEnd;
//     }
//
//     public int FindNextState(int mappedId, int curState){
//         int nextState = 0;
//         try {
//             switch(curState){
//                 case 0: if(mappedId == 1) nextState = 1;
//                         else nextState = DELIMITER;
//                         break;
//                 case 1: if(mappedId == 0) nextState = 2;
//                         else nextState = DELIMITER;
//                         break;
//                 case 2: if(mappedId == 3) nextState = DELIMITER_WITH_NEW_TOKEN;
//                         else nextState = DELIMITER;
//                         break;
//                 default: nextState = ERROR;
//             }
//         } catch(Exception e) {
//             e.printStackTrace();
//             System.out.println(e);
//             System.exit(-1);
//         }
//         return nextState;
//     }
// }

class ScanLiteral {
    private String m_curLine = "";
    private int m_lineLength = 0;
    private int m_startIdx = 0;
    private int m_endIdx = 0;
    private int m_typeOfDelimiter = 0;
    private String m_token = "";
    private boolean m_isThereTag = false;

    private static final int ERROR = -1;
    private static final int DELIMITER_FROM_DFA_OF_TAG = -2;
    private static final int DELIMITER_FROM_DFA_Without_TAG = -3;

    public ScanLiteral(String line){
        try {
            m_curLine = line.substring(1, line.length() - 1);
            m_lineLength = m_curLine.length();
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }
    }

    public boolean Scan(){
        InfoOfToken info = null;
        try {
            while(m_startIdx < m_lineLength){
                info = Scan(m_curLine, m_startIdx, m_endIdx);
                m_typeOfDelimiter = info.m_typeOfDelimiter;
                m_endIdx = info.m_endIdx;

                if(m_typeOfDelimiter == DELIMITER_FROM_DFA_OF_TAG) m_isThereTag = true;

                // if((m_startIdx == 0) && m_endIdx == m_lineLength && m_typeOfDelimiter != DELIMITER_FROM_DFA_OF_TAG) {
                //     return false;
                // }

                if(m_isThereTag == true && m_endIdx == m_lineLength && m_typeOfDelimiter != DELIMITER_FROM_DFA_OF_TAG) {
                    m_typeOfDelimiter = DELIMITER_FROM_DFA_Without_TAG;
                }

                m_token = m_curLine.substring(m_startIdx, m_endIdx);
                m_startIdx = m_endIdx;

                AnalyzeToken(m_token, m_typeOfDelimiter);
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        return m_isThereTag;
    }

    public static InfoOfToken Scan(String line, int startIdx, int endIdx){
        int sizeOfLine = line.length();
        int curState = 0;
        InfoOfToken info = null;
        try {
            for(int i = startIdx; i < sizeOfLine; i++){
                curState = FindNextState(line.charAt(i), curState);
                // System.out.println(curState);
                if(curState == DELIMITER_FROM_DFA_OF_TAG ||
                   curState == DELIMITER_FROM_DFA_Without_TAG) { // special state case check
                    break;
                } else if(curState == ERROR){
                    System.out.println(line.substring(startIdx, endIdx + 1) + " is Rejected! 1");
                    System.exit(-1);
                }
                endIdx++;
            }

            if (endIdx == sizeOfLine){
                if((curState = FindNextState(' ', curState)) == ERROR) {
                    // System.out.println(curState);
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

    public static int FindNextState(char ch, int curState){
        // we need to simplify the algorithm, and increase extencability
        // do not consider Grammar issue
        int nextState = 0;
        try {
            switch (curState) { // Need to reduce redundancy
                case 0: if(ch == '<') nextState = 2;
                        else nextState = 1;
                        break;
                case 1: if(ch == '<') nextState = DELIMITER_FROM_DFA_Without_TAG;
                        else nextState = curState;
                        break;
                case 2: if(ch == '>') nextState = 3;
                        else nextState = curState;
                        break;
                case 3: nextState = DELIMITER_FROM_DFA_OF_TAG;
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

    public void AnalyzeToken(String token, int typeOfDelimiter){
        try {
            if (typeOfDelimiter == DELIMITER_FROM_DFA_OF_TAG)
                System.out.println(token + " : " + "Tag");
            else if(typeOfDelimiter == DELIMITER_FROM_DFA_Without_TAG)
                System.out.println("\"" + token + "\"" + " : " + "between or around the Tag");
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        return;
    }

}
