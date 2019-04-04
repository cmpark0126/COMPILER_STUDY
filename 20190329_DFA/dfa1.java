import java.util.Scanner;

public class dfa1 {
  private static final int ERROR = 5;

  public static void main(String[] args) {
      Scanner kb = new Scanner(System.in); // Get a hexadecimal

      while(true){
        System.out.println("Type E or e to exit.");
        System.out.print("Input: ");
        String input = kb.nextLine();
        boolean result = true;

        if(input.length() != 0 && input.charAt(0) == 'e') break;
        else result = dfa1.IsAccepted(input);

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

      current_state = dfa1.nextState(ch, current_state);
      if(current_state == ERROR) return false;
    }

		return (current_state == 0 ||
            current_state == 1 ||
            current_state == 2 ||
            current_state == 4)? true : false;
	}

  public static int nextState(char ch, int current_state){
    switch (current_state) {
            case 0: if(ch == 'a') return 1;
                    else if(ch == 'b') return ERROR;
                    else return ERROR;
            case 1: if(ch == 'a') return 2;
                    else if(ch == 'b') return 3;
                    else return ERROR;
            case 2: if(ch == 'a') return 2;
                    else if(ch == 'b') return ERROR;
                    else return ERROR;
            case 3: if(ch == 'a') return 4;
                    else if(ch == 'b') return ERROR;
                    else return ERROR;
            case 4: if(ch == 'a') return ERROR;
                    else if(ch == 'b') return 3;
                    else return ERROR;
            default: return ERROR;
        }
  }
}
