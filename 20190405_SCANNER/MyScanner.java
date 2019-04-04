import java.util.Scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MyScanner {
private boolean isScript = false;
private int currentState = 0;

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

public boolean Scan(String line){
    System.out.println("Scan(line:String):boolean");
    int sizeOfLine = line.length();
    int startIdx = 0;
    int endIdx = 0;
    try {
        while(true){
            endIdx = this.GetTokenIdx(line, startIdx);
            // match token symbol from hash table and print information of that symbol

            startIdx = endIdx + 2; // over delimeter
            if(sizeOfLine <= startIdx) break;
        }
    } catch(Exception e) {
        System.out.println("Usage : Scan(line:String):boolean fault");
    }
    return true;

}

// we need to fix to follow under condition
public int GetTokenIdx(String line, int startIdx){
    // if "var m = 0, h = 1;", we need to finish all of the setting
    // we need to consider '\n' character when we use scanner
    System.out.println("GetTokenIdx(line:String, startIdx:int):int");
    int endIdx = startIdx;
    try {
        currentState = NextState('e');
    } catch(Exception e) {
        System.out.println("Usage : GetTokenIdx(line:String, startIdx:int):int fault");
    }

    return endIdx;
}

public int NextState(char ch){
    System.out.println("NextState(ch:char):int");
    try {

    } catch(Exception e) {
        System.out.println("Usage : NextState(ch:char):int fault");
    }

    return -1;
}

}
