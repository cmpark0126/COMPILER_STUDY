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
            System.out.println("Complete Parse!");

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
        RemoveInfoOfToken(); // if matched we need to delete this element from queue
        if(option == COMPARE_WITH_TOKEN) givenValue = info.m_token;
        else if(option == COMPARE_WITH_SYMBOL) givenValue = info.m_symbolInfo; // or compare with symbol

        // System.out.println("m_token: "+ info.m_token + ", m_symbolInfo: " + info.m_symbolInfo);

        try {
            if(givenValue == null || expectedValue == null) throw new Exception("There is a no Token!: " + errorMessage);
            if(!givenValue.equals(expectedValue)) throw new Exception("Token is wrong value: " + errorMessage);
        } catch(Exception e) {
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
        Match("number", COMPARE_WITH_SYMBOL, "We need number"); // match is clear buffer automatically, so please use carefully
        return;
    }

    public void id(){
        Match("user-defined id", COMPARE_WITH_SYMBOL, "We need id");
        if(CheckNext("(", COMPARE_WITH_TOKEN)){
            Match("(", COMPARE_WITH_TOKEN, "We need \"(\" token");
            functionParameter();
            Match(")", COMPARE_WITH_TOKEN, "We need \")\" token");
        }
        return;
    }

    public void literal(){
        Match("literal", COMPARE_WITH_SYMBOL, "We need literal");
        return;
    }

    public boolean operand(){
        if(CheckNext("number", COMPARE_WITH_SYMBOL)) number();
        else if(CheckNext("user-defined id", COMPARE_WITH_SYMBOL)) id();
        else if(CheckNext("literal", COMPARE_WITH_SYMBOL)) literal();
        else return false;
        return true;
    }

    public void functionParameter(){
        if(operand() && CheckNext(",", COMPARE_WITH_TOKEN)) { // only when there is first parameter, we can check next one.
            Match(",", COMPARE_WITH_TOKEN, "We need \",\" token");
            functionParameter();
        }
        return;
    }

    public void comment(){
        Match("comment", COMPARE_WITH_SYMBOL, "We need comment");
        return;
    }

    public boolean factor(){
        if(CheckNext("number", COMPARE_WITH_SYMBOL)) number();
        else if(CheckNext("user-defined id", COMPARE_WITH_SYMBOL)) id();
        else return false;
        return true;
    }

    public boolean mulop(){
        if(CheckNext("*", COMPARE_WITH_TOKEN)) Match("*", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("/", COMPARE_WITH_TOKEN)) Match("/", COMPARE_WITH_TOKEN, null);
        else return false;
        return true;
    }

    public void term(){
        if(operand() && mulop()){
            operand();
        }
    }

    public boolean addop(){
        if(CheckNext("+", COMPARE_WITH_TOKEN)) Match("+", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("-", COMPARE_WITH_TOKEN)) Match("-", COMPARE_WITH_TOKEN, null);
        else return false;
        return true;
    }

    public boolean unaryop(){
        if(CheckNext("++", COMPARE_WITH_TOKEN)) Match("++", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("--", COMPARE_WITH_TOKEN)) Match("--", COMPARE_WITH_TOKEN, null);
        else return false;
        return true;
    }

    public void expression(){
        term();
        if(addop()){
            term();
        }
    }

    public void varDeclare() {
        Match("var", COMPARE_WITH_TOKEN, "We need \"var\" token");
        id();

        while(true){
            if(CheckNext("=", COMPARE_WITH_TOKEN)) {
                Match("=", COMPARE_WITH_TOKEN, "We need \"=\" token");
                if(!operand()) Match(null, COMPARE_WITH_SYMBOL, "We need number or id"); // for error
            } else if(CheckNext(",", COMPARE_WITH_TOKEN)) {
                Match(",", COMPARE_WITH_TOKEN, "We need \",\" token");
                while(CheckNext("comment", COMPARE_WITH_SYMBOL)) comment();
                id();
            } else break;
        }

        return;
    }

    public void functionDeclare() {
        Match("function", COMPARE_WITH_TOKEN, "We need \"function\" token");
        id();

        Match("{", COMPARE_WITH_TOKEN, "We need \"{\" token");
        stmt();
        Match("}", COMPARE_WITH_TOKEN, "We need \"}\" token");

        return;
    }

    public void whileLoop(){
        Match("while", COMPARE_WITH_TOKEN, "We need \"while\" token");
        Match("(", COMPARE_WITH_TOKEN, "We need \"(\" token");
        logicalExpression();
        Match(")", COMPARE_WITH_TOKEN, "We need \")\" token");
        if(CheckNext("{", COMPARE_WITH_TOKEN)){
            Match("{", COMPARE_WITH_TOKEN, "We need \"{\" token");
            stmt();
            Match("}", COMPARE_WITH_TOKEN, "We need \"}\" token");
        } else {
            stmt();
        }
        return;
    }

    public void doWhileLoop(){
        Match("do", COMPARE_WITH_TOKEN, "We need \"do\" token");
        Match("{", COMPARE_WITH_TOKEN, "We need \"{\" token");
        stmt();
        Match("}", COMPARE_WITH_TOKEN, "We need \"}\" token");
        Match("while", COMPARE_WITH_TOKEN, "We need \"while\" token");
        Match("(", COMPARE_WITH_TOKEN, "We need \"(\" token");
        logicalExpression();
        Match(")", COMPARE_WITH_TOKEN, "We need \")\" token");
        return;
    }

    public void forLoop(){
        Match("for", COMPARE_WITH_TOKEN, "We need \"for\" token");
        Match("(", COMPARE_WITH_TOKEN, "We need \"(\" token");

        if(CheckNext("user-defined id", COMPARE_WITH_SYMBOL)) useId();
        else if (CheckNext("var", COMPARE_WITH_TOKEN)) varDeclare();
        Match(";", COMPARE_WITH_TOKEN, "We need \";\" token");
        logicalExpression();
        Match(";", COMPARE_WITH_TOKEN, "We need \";\" token");
        if(CheckNext("user-defined id", COMPARE_WITH_SYMBOL)) useId();
        else if(CheckNext("++", COMPARE_WITH_TOKEN) || CheckNext("--", COMPARE_WITH_TOKEN)) {
            unaryop();
            id();
        }
        Match(")", COMPARE_WITH_TOKEN, "We need \")\" token");
        if(CheckNext("{", COMPARE_WITH_TOKEN)){
            Match("{", COMPARE_WITH_TOKEN, "We need \"{\" token");
            stmt();
            Match("}", COMPARE_WITH_TOKEN, "We need \"}\" token");
        } else {
            stmt();
        }
        return;
    }

    public void ifLoop(){
        Match("if", COMPARE_WITH_TOKEN, "We need \"if\" token");
        Match("(", COMPARE_WITH_TOKEN, "We need \"(\" token");
        logicalExpression();
        Match(")", COMPARE_WITH_TOKEN, "We need \")\" token");
        if(CheckNext("{", COMPARE_WITH_TOKEN)){
            Match("{", COMPARE_WITH_TOKEN, "We need \"{\" token");
            stmt();
            Match("}", COMPARE_WITH_TOKEN, "We need \"}\" token");
        } else {
            stmt();
        }

        if(CheckNext("else", COMPARE_WITH_TOKEN)){
            Match("else", COMPARE_WITH_TOKEN, null);
            if(CheckNext("{", COMPARE_WITH_TOKEN)){
                Match("{", COMPARE_WITH_TOKEN, "We need \"{\" token");
                stmt();
                Match("}", COMPARE_WITH_TOKEN, "We need \"}\" token");
            } else {
                stmt();
            }
        }
        return;
    }

    public void switchStmt(){
        Match("switch", COMPARE_WITH_TOKEN, "We need \"switch\" token");
        Match("(", COMPARE_WITH_TOKEN, "We need \"(\" token");
        expression();
        Match(")", COMPARE_WITH_TOKEN, "We need \")\" token");
        Match("{", COMPARE_WITH_TOKEN, "We need \"{\" token");
        caseStmt();
        Match("}", COMPARE_WITH_TOKEN, "We need \"}\" token");
        return;
    }

    public void caseStmt(){
        boolean needNextStmt = false;
        if(needNextStmt = CheckNext("case", COMPARE_WITH_TOKEN)){
            Match("case", COMPARE_WITH_TOKEN, null);
            operand();
            Match(":", COMPARE_WITH_TOKEN, "We need \":\" token");
            stmt();
        } else if(needNextStmt = CheckNext("default", COMPARE_WITH_TOKEN)) {
            Match("default", COMPARE_WITH_TOKEN, null);
            Match(":", COMPARE_WITH_TOKEN, "We need \":\" token");
            stmt();
        }

        if(needNextStmt) caseStmt();

        return;
    }

    public boolean logicalOperator(){
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
        else return false;

        return true;
    }

    public boolean booleanVal(){
        if(CheckNext("true", COMPARE_WITH_TOKEN))
            Match("true", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("false", COMPARE_WITH_TOKEN))
            Match("false", COMPARE_WITH_TOKEN, null);
        else return false;

        return true;
    }

    public boolean logicalExpression(){
        if(!booleanVal()){
            if(!operand()) return false; // for error
            if(logicalOperator()) if(!operand()) return false;

        }
        return true;
    }

    public void valueAssign(){
        Match("=", COMPARE_WITH_TOKEN, null);
        if(booleanVal()) return;
        expression();
    }

    public boolean assignmentOp(){
        if(CheckNext("+=", COMPARE_WITH_TOKEN))
            Match("+=", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("-=", COMPARE_WITH_TOKEN))
            Match("-=", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("*=", COMPARE_WITH_TOKEN))
            Match("*=", COMPARE_WITH_TOKEN, null);
        else if(CheckNext("*=", COMPARE_WITH_TOKEN))
            Match("*=", COMPARE_WITH_TOKEN, null);
        else return false;

        return true;
    }

    public void useId(){
        id();
        if(!unaryop()){
            if(CheckNext("=", COMPARE_WITH_TOKEN)) valueAssign();
            else if(assignmentOp()) expression();
        }
    }

    public void stmt(){
        InfoOfToken info = GetInfoOfToken();
        String token = info.m_token;
        String symbol = info.m_symbolInfo;
        boolean checkSymbol = false;
        boolean needNextStmt = false;

        if(needNextStmt = CheckNext("var", COMPARE_WITH_TOKEN)) {
            varDeclare();
            Match(";", COMPARE_WITH_TOKEN, "We need \";\" token");
        }
        else if(needNextStmt = CheckNext("function", COMPARE_WITH_TOKEN)) {
            functionDeclare();
        }
        else if(needNextStmt = CheckNext("while", COMPARE_WITH_TOKEN)) {
            whileLoop();
        }
        else if(needNextStmt = CheckNext("do", COMPARE_WITH_TOKEN)) {
            doWhileLoop();
            Match(";", COMPARE_WITH_TOKEN, "We need \";\" token");
        }
        else if(needNextStmt = CheckNext("for", COMPARE_WITH_TOKEN)) {
            forLoop();
        }
        else if(needNextStmt = CheckNext("if", COMPARE_WITH_TOKEN)) {
            ifLoop();
        }
        else if(needNextStmt = CheckNext("switch", COMPARE_WITH_TOKEN)) {
            switchStmt();
        }
        else if(needNextStmt = CheckNext("break", COMPARE_WITH_TOKEN)) {
            Match("break", COMPARE_WITH_TOKEN, null);
            Match(";", COMPARE_WITH_TOKEN, "We need \";\" token");
        }
        else if(needNextStmt = CheckNext("return", COMPARE_WITH_TOKEN)) {
            Match("return", COMPARE_WITH_TOKEN, null);
            expression();
            Match(";", COMPARE_WITH_TOKEN, "We need \";\" token");
        }
        else if(needNextStmt = unaryop()) {
            id();
            Match(";", COMPARE_WITH_TOKEN, "We need \";\" token");
        }
        else if(needNextStmt = CheckNext("user-defined id", COMPARE_WITH_SYMBOL)) {
            useId();
            Match(";", COMPARE_WITH_TOKEN, "We need \";\" token");
        }
        else if(needNextStmt = CheckNext("comment", COMPARE_WITH_SYMBOL)) {
            comment();
        }

        if(needNextStmt) stmt();

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
