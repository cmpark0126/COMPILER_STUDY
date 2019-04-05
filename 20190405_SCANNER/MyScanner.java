import java.util.Scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MyScanner {
private boolean isScript = false;
private static final int DELIMETER = -1;
private static final int ERROR = -2;

public static void main(String[] args) {
    System.out.println("main:void");
    try {
        MyScanner scanner = new MyScanner();
        // Need to open java script file
        File file = null; // dummy
        scanner.Scan(file);
    } catch(Exception e) {
        System.out.println("Usage : main fault");
    }
}

public MyScanner(){
    System.out.println("MyScanner()");
    try {

    } catch(Exception e) {
        System.out.println("Usage : Constructor fault");
    }
}

public boolean Scan(File file){
    System.out.println("Scan(file:File):boolean");
    try {
        Scanner kb = new Scanner(System.in); // dummy
        // read nextLine from file
        while(true){
            // if there is no nextline : break;
            String line = kb.nextLine(); // dummy
            if(line.charAt(0) == 'e') break; // dummy
            this.Scan(line);
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
    System.out.println("Scan(line:String):boolean");
    int sizeOfLine = line.length();
    int startIdx = 0;
    int endIdx = 0;
    int curState = 0;
    try {
        while(true){
            for(int i = startIdx; i < sizeOfLine; i++){
                endIdx++;
                curState = this.nextState(line.charAt(i));
                if(curState == DELIMETER) break;
            }
            String token = line.substring(startIdx, endIdx);
            // analyze token
            this.AnalyzeToken(token);

            if(endIdx >= sizeOfLine) break;
            curState = 0;
            startIdx = endIdx;
        }

    } catch(Exception e) {
        System.out.println("Usage : Scan(line:String):boolean fault");
    }
    return true;

}

public int nextState(char ch){
    System.out.println("nextState(ch:char):int");
    try {
        return DELIMETER;
    } catch(Exception e) {
        System.out.println("Usage : nextState(ch:char):int fault");
    }

    return ERROR;
}

public void AnalyzeToken(String token){
    System.out.println("AnalyzeToken(token:String):void");
    try {
        System.out.println(token);
    } catch(Exception e) {
        System.out.println("Usage : AnalyzeToken(token:String):void fault");
    }

    return;
}

}
