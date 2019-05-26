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


public class parser1 {
    public static void main(String[] args) {
        // Scanner kb = new Scanner(System.in);
        String filename = "";
        File file = null;
        MyScanner scanner = null;
        MyLL1Parser parser = null;
        InfoOfToken info = null;

        try {
            // System.out.print("Enter the filename: ");
            // filename = kb.nextLine();
            filename = args[0];
            file = new File(filename);

            scanner = new MyScanner(file);
            parser = new MyLL1Parser(scanner);
            parser.Parse();
            System.out.println("Complete Parse!");

        } catch(Exception e) {
            System.out.println("Usage : java parser1 [FILE_NAME]");
            System.exit(-1);
        }

    }

}
