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

public class MyLL1Parser {
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
        MyLL1Parser parser = null;
        InfoOfToken info = null;

        try {
            System.out.print("Enter the filename: ");
            filename = kb.nextLine();
            file = new File(filename);

            scanner = new MyScanner(file);
            parser = new MyLL1Parser(scanner);
            parser.Parse();
            System.out.println("Complete Parse!");

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        kb.close();
    }

    public MyLL1Parser(MyScanner scanner){
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
        try {
            jsscode();
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
        RemoveInfoOfToken(); // if matched May We need to delete this element from queue
        if(option == COMPARE_WITH_TOKEN) givenValue = info.m_token;
        else if(option == COMPARE_WITH_SYMBOL) givenValue = info.m_symbolInfo; // or compare with symbol

        // System.out.println("m_token: "+ info.m_token + ", m_symbolInfo: " + info.m_symbolInfo);

        try {
            if(givenValue == null || expectedValue == null) throw new Exception("There is a no Token!: " + errorMessage);
            if(!givenValue.equals(expectedValue)) throw new Exception("Token is wrong value: " + errorMessage);
        } catch(Exception e) {
            System.out.println("Parsing Error!");
            if(givenValue != null){
                System.out.println("line num: " + info.m_curLinenum);
                System.out.println(info.m_curLine);
                for(int i = 0; i < info.m_startIdx; i++) System.out.print(" ");
                for(int i = info.m_startIdx; i < info.m_endIdx; i++) System.out.print("-");
                System.out.println();
                for(int i = 0; i < info.m_endIdx - 1; i++) System.out.print(" ");
                System.out.println("^");
            }
            // e.printStackTrace();
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
        Match("number", COMPARE_WITH_SYMBOL, "May We need number"); // match is clear buffer automatically, so please use carefully
        return;
    }

    public void id(){
        Match("user-defined id", COMPARE_WITH_SYMBOL, "May We need id");
        // if(CheckNext("(", COMPARE_WITH_TOKEN)){
        //     Match("(", COMPARE_WITH_TOKEN, "May We need \"(\" token");
        //     functionParameter();
        //     Match(")", COMPARE_WITH_TOKEN, "May We need \")\" token");
        // }
        return;
    }

    public void literal(){
        Match("literal", COMPARE_WITH_SYMBOL, "May We need literal");
        return;
    }

    public void factor(){
        if(CheckNext("(", COMPARE_WITH_TOKEN)) {
            Match("(", COMPARE_WITH_TOKEN, null);
            exp();
            Match(")", COMPARE_WITH_TOKEN, "May We need )");
        }
        if(CheckNext("number", COMPARE_WITH_SYMBOL)) number();
        else if(CheckNext("user-defined id", COMPARE_WITH_SYMBOL)) id();
        else if(CheckNext("literal", COMPARE_WITH_SYMBOL)) literal();
        return;
    }

    public void mulop(){
        if(CheckNext("*", COMPARE_WITH_TOKEN)) Match("*", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("/", COMPARE_WITH_TOKEN)) Match("/", COMPARE_WITH_TOKEN, null);
        return;
    }

    public void term(){
        factor();
        if(CheckNext("*", COMPARE_WITH_TOKEN) ||
           CheckNext("/", COMPARE_WITH_TOKEN)){
            mulop();
            factor();
        }
    }

    public void addop(){
        if(CheckNext("+", COMPARE_WITH_TOKEN)) Match("+", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("-", COMPARE_WITH_TOKEN)) Match("-", COMPARE_WITH_TOKEN, null);
        return;
    }

    public void exp(){
        term();
        if(CheckNext("+", COMPARE_WITH_TOKEN) ||
           CheckNext("-", COMPARE_WITH_TOKEN)){
            addop();
            term();
        }
    }

    public void varDeclaereId(){
        id();
        return;
    }

    public void varDeclaereIdAssign(){
        varDeclaereId();
        if(CheckNext("=", COMPARE_WITH_TOKEN)){
            Match("=", COMPARE_WITH_TOKEN, null);
            exp();
        }

        return;
    }

    public void varDeclaereIdAssignSequence(){
        if(CheckNext(",", COMPARE_WITH_TOKEN)) {
            Match(",", COMPARE_WITH_TOKEN, null);
            varDeclaereIdAssign();
            varDeclaereIdAssignSequence();
        }

        return;
    }

    public void varDeclare(){
        Match("var", COMPARE_WITH_TOKEN, null);
        varDeclaereIdAssign();
        varDeclaereIdAssignSequence();

        return;
    }

    public void unaryop(){
        if(CheckNext("++", COMPARE_WITH_TOKEN)) Match("++", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("--", COMPARE_WITH_TOKEN)) Match("--", COMPARE_WITH_TOKEN, null);
        return;
    }

    public void idAssign(){
        id();
        if(CheckNext("=", COMPARE_WITH_TOKEN)){
            Match("=", COMPARE_WITH_TOKEN, null);
            exp();
        } else if (CheckNext("++", COMPARE_WITH_TOKEN)||
                   CheckNext("--", COMPARE_WITH_TOKEN)){
            unaryop();
        }

        return;
    }

    public void logicalop(){
        if(CheckNext("==", COMPARE_WITH_TOKEN))
            Match("==", COMPARE_WITH_TOKEN, null);
        else if(CheckNext(">", COMPARE_WITH_TOKEN))
            Match(">", COMPARE_WITH_TOKEN, null);
        else if(CheckNext(">=", COMPARE_WITH_TOKEN))
            Match(">=", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("<", COMPARE_WITH_TOKEN))
            Match("<", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("<=", COMPARE_WITH_TOKEN))
            Match("<=", COMPARE_WITH_TOKEN, null);
        return;
    }

    public void booleanVal(){
        if(CheckNext("true", COMPARE_WITH_TOKEN))
            Match("true", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("false", COMPARE_WITH_TOKEN))
            Match("false", COMPARE_WITH_TOKEN, null);
        return;
    }

    public void logicalFactor(){
        if(CheckNext("(", COMPARE_WITH_TOKEN)) {
            Match("(", COMPARE_WITH_TOKEN, null);
            logicalExp();
            Match(")", COMPARE_WITH_TOKEN, "May We need )");
        }
        if(CheckNext("number", COMPARE_WITH_SYMBOL)) number();
        else if(CheckNext("user-defined id", COMPARE_WITH_SYMBOL)) id();
        else if(CheckNext("literal", COMPARE_WITH_SYMBOL)) literal();
        else if(CheckNext("true", COMPARE_WITH_TOKEN) || CheckNext("false", COMPARE_WITH_TOKEN)) booleanVal();

        return;
    }

    public void logicalExp(){
        logicalFactor();
        if(CheckNext("==", COMPARE_WITH_TOKEN) ||
           CheckNext(">", COMPARE_WITH_TOKEN) ||
           CheckNext(">=", COMPARE_WITH_TOKEN) ||
           CheckNext("<", COMPARE_WITH_TOKEN) ||
           CheckNext("<=", COMPARE_WITH_TOKEN)){
            logicalop();
            logicalFactor();
        }
    }

    public void elsePart(){
        if(CheckNext("else", COMPARE_WITH_TOKEN)){
            Match("else", COMPARE_WITH_TOKEN, null);
            Match("{", COMPARE_WITH_TOKEN, "May We need {");
            stmtSquence();
            Match("}", COMPARE_WITH_TOKEN, "May We need }");
        }
    }

    public void ifStmt(){
        Match("if", COMPARE_WITH_TOKEN, null);
        Match("(", COMPARE_WITH_TOKEN, "May We need (");
        logicalExp();
        Match(")", COMPARE_WITH_TOKEN, "May We need )");
        Match("{", COMPARE_WITH_TOKEN, "May We need {");
        stmtSquence();
        Match("}", COMPARE_WITH_TOKEN, "May We need }");
        elsePart();
        return;
    }

    public void stmt(){
        if(CheckNext("var", COMPARE_WITH_TOKEN)) {
            varDeclare();
            Match(";", COMPARE_WITH_TOKEN, "May We need \";\" token");
        }
        else if (CheckNext("user-defined id", COMPARE_WITH_SYMBOL)) {
            idAssign();
            Match(";", COMPARE_WITH_TOKEN, "May We need \";\" token");
        }
        else if (CheckNext("if", COMPARE_WITH_TOKEN)) {
            ifStmt();
        }

        return;
    }

    public void stmtSquence(){
        if(CheckNext("var", COMPARE_WITH_TOKEN) ||
           CheckNext("user-defined id", COMPARE_WITH_SYMBOL) ||
           CheckNext("if", COMPARE_WITH_TOKEN)) {
               stmt();
               stmtSquence();
           }
        return;
    }

    public void comment(){
        Match("comment", COMPARE_WITH_SYMBOL, "May We need comment");
        return;
    }

    public void initialComment(){
        boolean needNextStmt = false;
        if(needNextStmt = CheckNext("comment", COMPARE_WITH_SYMBOL)) {
            comment();
        }
        if(needNextStmt) initialComment();
        return;
    }

    public void jsscode(){
        initialComment();
        Match("<script_start>", COMPARE_WITH_TOKEN, "May We need \"<script_start>\" token");
        stmtSquence();
        Match("<script_end>", COMPARE_WITH_TOKEN, "May We need \"<script_end>\" token");
        return;
    }

    public InfoOfToken GetInfoOfToken(){
        InfoOfToken temp = null;
        while(true){
            if(m_queue.size() == 0) m_queue.add(m_scanner.Scan());
            temp = m_queue.peek();
            if(temp.m_symbolInfo.equals("comment")) m_queue.remove();
            else break;
        }
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
