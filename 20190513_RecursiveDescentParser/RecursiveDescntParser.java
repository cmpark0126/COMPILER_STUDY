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

public class RecursiveDescentParser {
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
                scanner.AnalyzeToken(info.m_token, info.m_typeOfDelimiter);
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.exit(-1);
        }

        kb.close();
    }
}
