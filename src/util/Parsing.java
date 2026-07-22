package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;


/*
This class is very nasty and its hard for me to justify why it even exists. 
The idea is to use resp strings for handling requests, even though i genuinly dont know why i should use it
Redis uses it, so its for sure the best option i just dont understand why and therefore its hard for me to feel motivated enough to do it

Right now the methods feel very hacky. Intuitively, i think the idea is, read the string as bytes
Right now i just split strings and essentially bypass the whole resp format. 

Essentially there are 2 parts, 
Part 1 - Generating RESP String
Takes a command string like 'set salem good person' and then genertes a RESP string like *3\r\n$3\r\nGET\r\n and so on. 
This parts feels alright. I think the onus of building the string might be on the client side so its fine to build like this. 

Part 2 - Converting String to command
This parts feels the most tacky and the one that needs to be modified the most. 
I believe some form of byte reading is needed to generate the array. I think bcs the onus is on the server side for this part, the idea is to be fast and efficient.
As of now theres no byte reading and just a bunch of string.spliting and some tacky logic.

Do better


Update:
ParseRespString method has been developed to readlines. this is a slight better update. 
its not ideal but i think its a good base that can be used to build on later.


*/

public class Parsing {




    //////////////////////////////////////// client side //////////////////////////////////////////////////////////////////////////               //////////////////////////////////

    /*
    Central manager of client side communication.
    Checks if string are valid
    valid strings turn into an array
    aforementioned array turns into a respString
    */
    public static String parseCommandString(String message){
        boolean valid = isValidCommand(message);
        if (valid){
            String[] commandArray = buildArray(message);
            String respString = buildRespString(commandArray);
            return respString;
        }
        return "-ERR unknown command";
    }



    /*
    Takes a command like 'set salem is a good person' and converts it to an array like 
    [set, salem, is a good person]
    Also uses isValidCommand to check if string is valid
    If String is not valid it throws a RuntimeException which can be handled wherever
    */
   /*
   update while reading this. i believe this never expects the string to be get? get malek return an array of [get, malek, malek]
   */
    private static String[] buildArray(String message){

        // resp string should either start as set get or delete so i only have to consider these 3

        String[] roughParse = message.split(" ");
        if (roughParse[0].equals("get")) return new String[]{roughParse[0], roughParse[1]}; // case if its a get call
        if (roughParse[0].equals("delete")) return new String[]{roughParse[0], roughParse[1]}; // case if its a delete call

        // this part of code handles set calls. this assumes key set can only be one word
        StringBuilder sb = new StringBuilder();

        for (int i = 2; i < roughParse.length -1; i++){
            sb.append(roughParse[i] + " ");
        }
        sb.append(roughParse[roughParse.length -1]);

        String[] res = {roughParse[0], roughParse[1], sb.toString()};
        return res;
    }


    /*
    Takes a command string and checks if its valid. 
    
    This method guarentees command strings start with either set, get or delete
    it ensures get and sets commands consist of exactly 2 words

    it also ensures that set commands are more then 2 words so 3 or more
    */
    private static boolean isValidCommand(String message){
        if (message == null) return false;
        String[] roughString = message.split(" ");
        if (roughString.length<2) return false;


        if (!roughString[0].equals("set") && !roughString[0].equals("get") && !roughString[0].equals("delete")){
            return false;
        }

        if (roughString[0].equals("get") && roughString.length != 2) return false;
        
        if (roughString[0].equals("delete") && roughString.length != 2) return false;

        if (roughString[0].equals("set") && roughString.length < 3) return false;

        return true;

    }


    /*
    Takes a command in the form of an array
    builds a resp string from the array
    Note: this only builds respstrings that start with * and assumes every value is a string
    */
    public static String buildRespString(String[] message){
        StringBuilder sb = new StringBuilder();
        sb.append("*" + String.valueOf(message.length) + "\r\n");

        for (String word : message){
            sb.append("$" + String.valueOf(word.length()) + "\r\n");
            sb.append(word + "\r\n");
        }

        return sb.toString();
    }



/////////////////////////////////////////////////////// Server Side ///////////////////////////////////////////////

    /*
    Central manager for handling incoming requests
    Reads from input stream and generates respString
    checks if valid respString 
    builds command array from respString

    Note: handles error slightly. if any error happens it just sends back a ["set", "turn" "off"]. this triggers to server to terminate connection with client
    */
    public static String[] parseRequest(BufferedReader reader){
        
        String respString = getRespString(reader); // blocking I/O so this should wait until it gets all packages from the respstring or throw an error

        boolean valid = checkValidRespString(respString);
        if (valid){
            String[] res = parseRespString(respString);
            return res;
        }
        return new String[]{"set", "turn", "off"};

    }

    /*
    This is an I/O blocker. 
    It reads message from clients
    And generates the respString
    */
    public static String getRespString(BufferedReader reader){

        try {
            String len = reader.readLine();
            if (len.equals("-ERR unknown command")) return len; // error command server side

            int length = Integer.parseInt(len.substring(1));
            String res = len;
            res += "\r\n";
            for (int i = 0; i < length*2; i++){ // plus one for the extra \r\n
                res += reader.readLine() + "\r\n";
            }
            reader.readLine();
            return res;
            

        } catch (IOException e){
            e.printStackTrace();
            return "-ERR unknown command";
        }
    }


    /*
    
    */
    public static boolean checkValidRespString(String message){ 
        if (message == null) return false;
        if (message.equals("-ERR unknown command")) return false;
        char c = message.charAt(0);
        if (c != '*' && c != '+' && c != '-') return false;

        if (c == '*'){ // case for array respStrings
            String[] m = message.split("\r\n");

            int length = Integer.valueOf(m[0].substring(1));
            if (length*2 != m.length-1) return false; 
            String command = m[2];
            if (!command.equals("get") && !command.equals("set") && !command.equals("delete")) return false;

        }
        return true;
    }

    /*
    this is meant to convert a resp string to a string array
    right now its hacky. you split the array to create a temporary String array and then from there you can apply some logic to that
    i believe the ideal way to is to do byte reading to create the array. 
    i cba to do that though. 
    */
    public static String[] parseRespString(String message){
        String[] roughParse = message.split("\r\n");
        String[] ans = new String[Integer.parseInt(roughParse[0].substring(1))];

        int j = 0;
        for (int i = 2; j < Integer.parseInt(roughParse[0].substring(1)); i=i+2){
            ans[j] = roughParse[i];
            j++;
        }
        return ans;
    }

    public static String[] parseRespString(BufferedReader reader){
        try{
            String len = reader.readLine();
            if (len.equals("-ERR unknown command")) return new String[]{"set", "turn", "off"}; // if client side sends err command just shut off connection
            int length = Integer.parseInt(len.substring(1));
            String[] res = new String[length];
            for (int i = 0; i < length; i++){
            // tacky way to just add create the String array 
            // this does not check for $ or any of that lol

            String wordSize = reader.readLine();
            int size = Integer.parseInt(wordSize.substring(1)); // this should be used to read the amount of bytes. right now i just do simple line reads
            String word = reader.readLine();
            res[i] = word;
            }
            reader.readLine();
            return res;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }









// main for debugging only right now
    public static void main(String[] args){
        Parsing parsing = new Parsing();
        Scanner scanner = new Scanner(System.in);
        while (true){
            String idc = scanner.nextLine();
            if (idc.equals("turn off")) break;
            BufferedReader reader = new BufferedReader(new StringReader(parseCommandString(idc)));
            System.out.println(Arrays.toString(parseRequest(reader)));
            
        }
        scanner.close();
    }
    
}
