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
import java.util.LinkedList;

public class MyRecursiveDescentParser {
    private MyScanner m_scanner = null;
    private LinkedList<InfoOfToken> m_queue = null;

    private static final int MATCH = 1;
    private static final int NO_MATCH = -1;
    private static final int NO_TOKEN = -2;
    private static final boolean COMPARE_WITH_TOKEN = true;
    private static final boolean COMPARE_WITH_SYMBOL = false;

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        String filename = "";
        File file = null;
        MyScanner scanner = null;
        MyRecursiveDescentParser parser = null;
        InfoOfToken info = null;

        try {
            System.out.print("Enter the filename: ");
            filename = kb.nextLine();
            file = new File(filename);

            scanner = new MyScanner(file);
            parser = new MyRecursiveDescentParser(scanner);
            parser.Parse();

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        kb.close();
    }

    public MyRecursiveDescentParser(MyScanner scanner){
        try {
            m_scanner = scanner;
            m_queue = new LinkedList<InfoOfToken>();
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }
    }

    public boolean Parse(){
        int isMatch = 0;

        try {
            while(true){
                jsscode();
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        return true;
    }

    public void Match(String expectedValue, boolean option, String errorMessage){
        InfoOfToken info = null;
        String givenValue = null;
        info = GetInfoOfToken();
        RemoveInfoOfToken(); // if matched we need to delete this element from queue
        if(option == COMPARE_WITH_TOKEN) givenValue = info.m_token;
        else if(option == COMPARE_WITH_SYMBOL) givenValue = info.m_symbolInfo; // or compare with symbol

        System.out.println("m_token: "+ info.m_token + ", m_symbolInfo: " + info.m_symbolInfo);

        try {
            if(givenValue == null || expectedValue == null) throw new Exception("There is a no Token!: " + errorMessage);
            if(!givenValue.equals(expectedValue)) throw new Exception("Token is wrong value: " + errorMessage);
        } catch(Exception e) {
            System.out.println("line num: " + info.m_curLinenum);
            System.out.println(info.m_curLine);
            for(int i = 0; i < info.m_startIdx; i++) System.out.print(" ");
            for(int i = info.m_startIdx; i < info.m_endIdx; i++) System.out.print("-");
            System.out.println();
            for(int i = 0; i < info.m_endIdx - 1; i++) System.out.print(" ");
            System.out.println("^");
            System.out.println(e);
            System.exit(-1);
        }

        return;
    }

    public boolean CheckNext(String expectedValue, boolean option){
        InfoOfToken info = GetInfoOfToken();
        String givenValue = null;
        if(option == COMPARE_WITH_TOKEN) givenValue = info.m_token;
        else if(option == COMPARE_WITH_SYMBOL) givenValue = info.m_symbolInfo;

        return givenValue.equals(expectedValue);
    }

    public void number(){
        Match("number", COMPARE_WITH_SYMBOL, "We need number"); // match is clear buffer automatically, so please use carefully
        return;
    }

    public void comment(){
        Match("comment", COMPARE_WITH_SYMBOL, "We need comment");
        return;
    }

    public void varDeclare() {
        Match("var", COMPARE_WITH_TOKEN, "We need \"var\" token");
        Match("user-defined id", COMPARE_WITH_SYMBOL, "We need user-defined id");

        while(true){
            if(CheckNext("=", COMPARE_WITH_TOKEN)) {
                Match("=", COMPARE_WITH_TOKEN, "We need \"=\" token");
                number();
            } else if(CheckNext(",", COMPARE_WITH_TOKEN)) {
                Match(",", COMPARE_WITH_TOKEN, "We need \",\" token");
                while(CheckNext("comment", COMPARE_WITH_SYMBOL)) comment();
                Match("user-defined id", COMPARE_WITH_SYMBOL, "We need user-defined id");
            } else break;
        }

        Match(";", COMPARE_WITH_TOKEN, "We need \",\" token");

        return;
    }

    public void stmt(){
        InfoOfToken info = GetInfoOfToken();
        String token = info.m_token;
        String symbol = info.m_symbolInfo;
        boolean checkSymbol = false;
        boolean needNestStmt = false;

        if(needNestStmt = CheckNext("var", COMPARE_WITH_TOKEN)) varDeclare();
        else if(needNestStmt = CheckNext("comment", COMPARE_WITH_SYMBOL)) comment();

        if(needNestStmt) stmt();

        return;
    }

    public void jsscode(){
        Match("<script_start>", COMPARE_WITH_TOKEN, "We need \"<script_start>\" token");
        stmt();
        Match("<script_end>", COMPARE_WITH_TOKEN, "We need \"<script_end>\" token");
        return;
    }

    public InfoOfToken GetInfoOfToken(){
        if(m_queue.size() == 0) m_queue.add(m_scanner.Scan());
        InfoOfToken temp = m_queue.peek();
        if(temp == null) temp = new InfoOfToken();
        return temp;
    }

    public void RemoveInfoOfToken(){
        try {
            m_queue.remove();
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }
    }

}
