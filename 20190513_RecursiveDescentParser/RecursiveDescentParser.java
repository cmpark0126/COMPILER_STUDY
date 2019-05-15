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

abstract class NonTerminal extends EBNF{
}

abstract class Terminal extends EBNF{

}

public class RecursiveDescentParser {
    public static Terminal number = new Terminal(){
        public boolean IsDigit(char ch){
            return (ch >= 48 && ch <= 57)? true : false;
        }

        @Override
        public boolean match(InfoOfToken info){
            String token = info.m_token;
            String symbolInfo = info.m_symbolInfo;

            for(char ch: token.toCharArray()) {
                if(!IsDigit(ch)) return false;
            }
            return true;
        }
   	};

    public static void main(String[] args) {
        Scanner kb = new Scanner(System.in);
        String filename = "";
        File file = null;
        MyScanner scanner = null;
        InfoOfToken info = null;

        try {
            System.out.print("Enter the filename: ");
            filename = kb.nextLine();
            file = new File(filename);

            scanner = new MyScanner(file);
            // Need to open java script file
            while(true) {
                info = scanner.Scan();
                if(info == null) break;
                System.out.println(info.m_token + " : "
                                    + number.equals((Object)info) + " : "
                                    + info.m_symbolInfo);
                // System.out.println(scanner.GetCurLineNum() + " : " + scanner.GetCurLine());
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        kb.close();
    }
}
