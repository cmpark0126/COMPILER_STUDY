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

abstract class EBNF {
    public abstract int Match();
}
abstract class NonTerminal extends EBNF{}
abstract class Terminal extends EBNF{}

public class MyRecursiveDescentParser {
    private MyScanner m_scanner = null;
    private LinkedList<InfoOfToken> m_queue = null;

    private static final int MATCH = 1;
    private static final int NO_MATCH = -1;
    private static final int NO_TOKEN = -2;

    public Terminal number = new Terminal(){
        @Override
        public int Match(){
            InfoOfToken info = GetInfoOfToken();
            if(info == null) return NO_TOKEN;

            if(!info.m_symbolInfo.equals("number")) return NO_MATCH;
            RemoveInfoOfToken(); // if matched we need to delete this element from queue
            return MATCH;
        }
   	};

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

    public int Match(String expectedToken){
        InfoOfToken info = GetInfoOfToken();
        if(info == null) return NO_TOKEN;

        if (!info.m_token.equals(expectedToken)) return NO_MATCH;
        RemoveInfoOfToken();
        return MATCH;
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
                isMatch = number.Match();
                System.out.println(CheckMatchingStatus(isMatch));
                RemoveInfoOfToken(); // for test
                if(isMatch == NO_TOKEN) break;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        return true;
    }

    public InfoOfToken GetInfoOfToken(){
        if(m_queue.size() == 0) m_queue.add(m_scanner.Scan());
        return m_queue.peek();
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



    public String CheckMatchingStatus(int matchingStatus){
        if(matchingStatus == MATCH) return "MATCH";
        else if(matchingStatus == NO_MATCH) return "NO_MATCH";
        if (matchingStatus == NO_TOKEN) return "NO_TOKEN";
        return "Wrong Status";
    }
}
