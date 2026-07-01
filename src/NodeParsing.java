import java.io.BufferedReader;
import java.util.Arrays;
// erm this might be extra no?
public class NodeParsing {
    


    public static String parseSomethingIdkYet(String[] command){
        // maybe check if command valid or something idk 

        String res = "";
        for (int i = 0; i < command.length-1; i++){
            res += command[i] + " ";
        }
        res += command[command.length-1];
        return res;
    }

// yeah this is so fucking ugly but lets see if it works!!!1
    public static String[] parseToStringArray(BufferedReader reader){
        // do you want to do some error handlign? i should do some error handling!!!
        try{
            String temp = reader.readLine(); // i/o blocker do i pass this?????
            System.out.println("[NodeParsing] you have passed the i/o blocker");
            String[] temp2 = temp.split(" ");
            if (temp2.length < 3) return new String[] {"set", "turn", "off"};
            String[] res = new String[3];
            res[0] = temp2[0];
            res[1] = temp2[1];
            res[2] = "";

            for (int i = 2; i < temp2.length-1; i++){
                res[2] += temp2[i] + " ";
            }
            res[2]+= temp2[temp2.length-1];
            System.out.println("[NodeListenterThread] this is how i converted the message: " + Arrays.toString(res));
            return res;
        } catch (Exception e){
            e.printStackTrace();
            return new String[]{"set", "turn", "off"}; // idk what im doing lol. 
        }



    }
}
