import java.util.Scanner;

public class dfa2 {
  private static final int ERROR = 6;

  public static void main(String[] args) {
      Scanner kb = new Scanner(System.in); // Get a hexadecimal

      while(true){
        System.out.println("Type E or e to exit.");
        System.out.print("Input: ");
        String input = kb.nextLine();
        boolean result = true;

        if(input.length() != 0 && input.charAt(0) == 'e') break;
        else result = dfa2.IsAccepted(input);

    		if(result) {
          if(input.length() == 0) System.out.println("Accepted (empty string)");
          else System.out.println("Accepted");
        }
        else System.out.println("Rejected");

      }
      kb.close();
  }

  public static boolean IsAccepted(String input){
    int current_state = 0;
    int lengthOfStr = input.length();

		for(int i = 0; i < lengthOfStr; i++){ // independent function
      char ch = input.charAt(i);

      current_state = dfa2.nextState(ch, current_state);
      if(current_state == ERROR) return false;
    }

		return (current_state == 5)? true : false;
	}

  public static int nextState(char ch, int current_state){
    switch (current_state) {
            case 0: if(ch == '#') return 1;
                    else if(ch == '$') return 3;
                    else return ERROR;
            case 1: if(ch == 'a' || ch == 'b') return 2;
                    else return ERROR;
            case 2: if(dfa2.isDigit(ch)) return 5;
                    else return ERROR;
            case 3: if(ch == 'x' || ch == 'y') return 4;
                    else return ERROR;
            case 4: if(dfa2.isDigit(ch)) return 5;
                    else return ERROR;
            case 5: if(dfa2.isDigit(ch)) return 5;
                    else return ERROR;
            default: return ERROR;
        }
  }

  public static boolean isDigit(char ch){
    return (ch == '0' ||
            ch == '1' ||
            ch == '2' ||
            ch == '3' ||
            ch == '4' ||
            ch == '5' ||
            ch == '6' ||
            ch == '7' ||
            ch == '8' ||
            ch == '9')? true : false;
  }
}
