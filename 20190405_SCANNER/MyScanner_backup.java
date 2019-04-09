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

private static final int DELIMITER = -1;
private static final int ERROR = -2;

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
            curState = nextState(line.charAt(i), curState);
            if(curState == DELIMITER) {
                break;
            }
            else if(curState == ERROR) {
                System.out.println(line.substring(startIdx, endIdx + 1) + " is Rejected!");
                System.exit(-1);
            }
            endIdx++;
        }

        if (endIdx == sizeOfLine){
            if(nextState(' ', curState) != DELIMITER) {
                System.out.println(line.substring(startIdx, endIdx) + " is Rejected!");
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

public static int nextState(char ch, int curState){
    // we need to simplify the algorithm, and increase extencability
    // do not consider grammer issue
    System.out.println("nextState(ch:char, curState:int):int");
    int nextState = 0;
    try {
        switch (curState) { // Need to reduce redundancy
            case 0: if(isLetter(ch)) nextState = 1;
                    else if(ch == ';' || ch == ':' || ch == ',' || ch == ' ') nextState = 5;
                    else if(ch == '=' || ch == '>' ||
                            ch == '<' || ch == '*' ||
                            ch == '/' || ch == '%') nextState = 2;
                    else if(isDigit(ch)) nextState = 4;
                    else if(ch == '(') nextState = 7;
                    else if(ch == '\"') nextState = 9;
                    else if(ch == '+' || ch == '-') nextState = 8;
                    else nextState = ERROR;
                    break;
            case 1: if(isLetter(ch) || isDigit(ch)) nextState = 1;
                    else if(ch == '.') nextState = 6;
                    else if(ch == ' ' || ch == ';' || ch == ':' ||
                            ch == ',' || ch == '=' ||
                            ch == '>' || ch == '<' ||
                            ch == '+' || ch == '-' ||
                            ch == '*' || ch == '/' ||
                            ch == '%' || ch == '(') nextState = DELIMITER;
                    else nextState = ERROR;
                    break;
            case 2: if(ch == '=') nextState = 3;
                    else if(ch == ' ' || isLetter(ch) || isDigit(ch)) nextState = DELIMITER;
                    else nextState = ERROR;
                    break;
            case 3: if(ch == ' ' || isLetter(ch) || isDigit(ch)) nextState = DELIMITER;
                    else nextState = ERROR;
                    break;
            case 4: if(isDigit(ch)) nextState = 4;
                    else if(ch == ' ' || ch == ';' || ch == ':' ||
                            ch == ',' || ch == '=' ||
                            ch == '>' || ch == '<' ||
                            ch == '+' || ch == '-' ||
                            ch == '*' || ch == '/' ||
                            ch == '%') nextState = DELIMITER;
                    else nextState = ERROR;
                    break;
            case 5: nextState = DELIMITER;
                    break;
            case 6: if(isLetter(ch)) nextState = 1;
                    else nextState = ERROR;
                    break;
            case 7: if(ch == ')') nextState = 5;
                    else nextState = 7;
                    break;
            case 8: if(ch == '=') nextState = 3;
                    if(ch == '+' || ch == '-') nextState = 5;
                    else if(ch == ' ' || isLetter(ch) || isDigit(ch)) nextState = DELIMITER;
                    else nextState = ERROR;
                    break;
            case 9: if(ch == '\"') nextState = 5;
                    else nextState = 9;
                    break;
            default: nextState = ERROR;
        }
    } catch(Exception e) {
        System.out.println("Usage : nextState(ch:char, curState:int):int fault");
    }

    return nextState;
}

public static boolean isDigit(char ch){
    return (ch >= 48 && ch <= 57)? true : false;
}

public static boolean isLetter(char ch){
    return ((ch >= 65 && ch <= 90) ||
            (ch >= 97 && ch <= 122))? true : false;
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
