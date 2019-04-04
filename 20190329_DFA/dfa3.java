import java.util.Scanner;

public class dfa3 {
  private static final int ERROR = 9;

  public static void main(String[] args) {
      Scanner kb = new Scanner(System.in); // Get a hexadecimal

      while(true){
        System.out.println("Type E or e to exit.");
        System.out.print("Input: ");
        String input = kb.nextLine();
        boolean result = true;

        if(input.length() != 0 && input.charAt(0) == 'e') break;
        else result = dfa3.IsAccepted(input);

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

      current_state = dfa3.nextState(ch, current_state);
      if(current_state == ERROR) return false;
    }

		return (current_state == 3 || current_state == 6)? true : false;
	}

  public static int nextState(char ch, int current_state){
    switch (current_state) {
            case 0: if(ch == 'a') return 1;
                    else if(ch == 'x') return 4;
                    else return ERROR;
            case 1: if(ch == 'b') return 2;
                    else if(ch == 'c') return 3;
                    else return ERROR;
            case 2: if(ch == 'b') return 2;
                    else if(ch == 'c') return 3;
                    else return ERROR;
            case 3: return ERROR;
            case 4: if(ch == 'y') return 5;
                    else if(ch == 'z') return 6;
                    else return ERROR;
            case 5: if(ch == 'a') return 1;
                    else if(ch == 'x') return 7;
                    else return ERROR;
            case 6: return ERROR;
            case 7: if(ch == 'y') return 8;
                    else if(ch == 'z') return 6;
                    else return ERROR;
            case 8: if(ch == 'x') return 7;
                    else return ERROR;
            default: return ERROR;
        }
  }
}
