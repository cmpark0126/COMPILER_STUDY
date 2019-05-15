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
    public abstract boolean match();
}
abstract class NonTerminal extends EBNF{}
abstract class Terminal extends EBNF{}

public class MyRecursiveDescentParser {
    private MyScanner m_scanner = null;
    private LinkedList<InfoOfToken> m_queue = null;

    public Terminal number = new Terminal(){
        @Override
        public boolean match(){
            InfoOfToken info = GetInfoOfToken();
            System.out.print(info.m_token+ ": ");
            System.out.print(info.m_symbolInfo+ ": ");
            System.out.print(m_queue.size() + ": ");
            RemoveInfoOfToken();
            System.out.print(m_queue.size() + ": ");
            if(info == null) {
                System.exit(-1);
                return false;
            }
            String symbolInfo = info.m_symbolInfo;
            if(symbolInfo != "number") return false;
            return true;
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
        boolean isT = false;

        try {
            while(true){
                isT = number.match();
                System.out.println(isT);
            }
            // if(isT) {
            //     System.out.println("Success!");
            // } else {
            //     System.out.println("Something Wrong!");
            // }
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
        m_queue.remove();
    }
}
