import java.util.Scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MyScanner {
private File file = null;
private boolean isScript = false;
private static final int DELIMITER = -1;
private static final int ERROR = -2;

public static void main(String[] args) {
    System.out.println("main:void");
    try {
        File file = null; // dummy
        MyScanner scanner = new MyScanner(file);
        // Need to open java script file
        scanner.Scan();
    } catch(Exception e) {
        System.out.println("Usage : main fault");
    }
}

public MyScanner(File file){
    System.out.println("MyScanner()");
    try {
        this.file = file;
    } catch(Exception e) {
        System.out.println("Usage : Constructor fault");
    }
}

public boolean Scan(){
    System.out.println("Scan(file:File):boolean");
    try {
        Scanner kb = new Scanner(System.in); // dummy
        // read nextLine from file
        while(true){
            // if there is no nextline : break;
            String line = kb.nextLine(); // dummy
            if(line.charAt(0) == 'e') break; // dummy
            Scan(line);
        }

        kb.close(); // dummy
    } catch(Exception e) {
        System.out.println("Usage : Scan(file:File):boolean fault");
    }

    return true;
}

// we need to fix to follow under condition
public boolean Scan(String line){
    // if "var m = 0, h = 1;", we need to finish all of the setting
    // we need to consider '\n' character when we use scanner
    // until we meet the symbol ';', we need to think all of the token on one line and on circumstance
    // token can be splited by sub token
    // we need to consider annotation
    // we need to consider whether the parenthesis is behind the function identifier or not
    // If the parenthesis is behind the reserved id like while, for, if, we need to analyze the sentence between the parenthesis
    System.out.println("Scan(line:String):boolean");
    int sizeOfLine = line.length();
    int startIdx = 0;
    int endIdx = 0;
    int curState = 0;
    try {
        while(true){
            for(int i = startIdx; i < sizeOfLine; i++){
                curState = nextState(line.charAt(i), curState);
                if(curState == DELIMITER) {
                    break;
                }
                else if(curState == ERROR) {
                    System.out.println(line.substring(startIdx, endIdx + 1) + " is Rejected!");
                    System.out.println("Please reprograming!");
                    System.exit(-1);
                }
                endIdx++;
            }

            if (endIdx == sizeOfLine){
                if(nextState(' ', curState) == DELIMITER) {
                    String token = line.substring(startIdx, endIdx);
                    AnalyzeToken(token);
                } else {
                    System.out.println(line.substring(startIdx, endIdx) + " is Rejected!");
                    System.out.println("Please reprograming!");
                    System.exit(-1);
                }
                break;
            }else if(startIdx != endIdx){ // analyze token
                String token = line.substring(startIdx, endIdx);
                AnalyzeToken(token);
            } else { // blank .etc
                endIdx++;
            }

            if(endIdx == sizeOfLine) break;
            curState = 0;
            startIdx = endIdx;
        }
    } catch(Exception e) {
        System.out.println("Usage : Scan(line:String):boolean fault");
    }
    return true;
}

public static int nextState(char ch, int curState){
    // we need to simplify the algorithm, and increase extencability
    // do not consider grammer issue
    System.out.println("nextState(ch:char, curState:int):int");
    int nextState = 0;
    try {
        switch (curState) { // Need to reduce redundancy
            case 0: if(isLetter(ch)) nextState = 1;
                    else if(ch == ' ') nextState = DELIMITER;
                    else if(ch == ';' || ch == ':' || ch == ',') nextState = 5;
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
    System.out.println("AnalyzeToken(token:String):void");
    try {
        System.out.println("analysis: " + token);
    } catch(Exception e) {
        System.out.println("Usage : AnalyzeToken(token:String):void fault");
    }

    return;
}

}
