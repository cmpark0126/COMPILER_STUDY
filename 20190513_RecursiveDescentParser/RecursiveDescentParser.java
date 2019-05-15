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

abstract class EBNF {
    public abstract boolean match(String token);

    @Override
    public boolean equals(Object o){

        if(o == null) return false;
        String token = (String)o;
        if(match(token)) return true;
        return false;
    }
}

abstract class NonTerminal extends EBNF{
}

abstract class Terminal extends EBNF{
    public static final int DELIMITER = -1;
    public static final int ERROR = -2;

    public abstract int nextState(char ch, int current_state);

    public boolean dfa(String input){
        int current_state = 0;
        int lengthOfStr = input.length();

    	for(int i = 0; i < lengthOfStr; i++){ // independent function
            char ch = input.charAt(i);

            current_state = nextState(ch, current_state);
            if(current_state == ERROR) return false;
        }

        current_state = nextState(' ', current_state);
    	return (current_state == DELIMITER)? true : false;
    }

    @Override
    public boolean match(String token){
        return dfa(token);
    }
}

public class RecursiveDescentParser {
    public static Terminal number = new Terminal(){
        public boolean IsDigit(char ch){
            return (ch >= 48 && ch <= 57)? true : false;
        }

   		@Override
   		public int nextState(char ch, int current_state){
            switch (current_state) {
                case 0: if(ch == '-') return 1;
                        else if(IsDigit(ch)) return 1;
                        else return ERROR;
                case 1: if(IsDigit(ch)) return 1;
                        else if(ch == ' ') return DELIMITER;
                        else return ERROR;
                default: return ERROR;
            }
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
                System.out.println(info.m_token + " : " + number.equals((Object)info.m_token));
                System.out.println(scanner.GetCurLineNum() + " : " + scanner.GetCurLine());
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        kb.close();
    }
}
