package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.Buffer;
import java.util.*;
import java.util.Arrays;
import java.util.Scanner;



// *2\r\n*1\r\n$5\r\nhello\r\n$5\r\nworld\r\n
 
public class Parsing {

    // turn input into a resp string
    public String bulkStringToResp(String s){
        int len = s.length();
        StringBuilder sb = new StringBuilder();
        sb.append("$");
        sb.append(String.valueOf(len));
        sb.append("\r\n");
        sb.append(s+"\r\n");

        return sb.toString();

    }


    public String arrayToRespArray(String[] command){
        StringBuilder sb = new StringBuilder();
        sb.append("*"+String.valueOf(command.length)+"\r\n");

        for (int i = 0; i < command.length; i++){
            sb.append(bulkStringToResp(command[i]));
        }

        return sb.toString();
    }

    public String[] inputToArray(String s){
        List<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean insideQuotes = false;


        for (char c : s.toCharArray()){
            if (c == '"'){
                insideQuotes = !insideQuotes;
                continue;
            }
            if (c == ' ' && !insideQuotes){
                res.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            sb.append(c);
        }
        res.add(sb.toString());

        return res.toArray(new String[0]);
    }


    public String generateRespString(String s){
        String[] command = inputToArray(s);
        String resp = arrayToRespArray(command);

        return resp;
    }




    // parsing resp string into a command logic below

    // currently impure bcs it edits RespBuffer resp. 
    // not only is this stupidi fuckign idiot impure it also cant parse simple strings you silly silly silly baka ! 
    public byte[] trimBuffer(RespBuffer buffer, boolean isBulkString){
        int len = buffer.buffer.length;
        byte[] newBuffer = new byte[len];

        if (!isBulkString){
            String s = new String(buffer.buffer, 0, buffer.offset);
            int idx = s.indexOf("\r\n");
            s = s.substring(idx+2);
            byte[] b = s.getBytes();

            for (int i = 0; i < b.length; i++){
                newBuffer[i] = b[i];
            }
            buffer.offset = b.length; // impurity here
            return newBuffer;
        }

        String s = new String(buffer.buffer, 0, buffer.offset);
        s = s.substring(s.indexOf("\r\n")+2);
        s = s.substring(s.indexOf("\r\n")+2);
        byte[] b = s.getBytes();
        for (int i = 0; i < b.length; i++){
            newBuffer[i] = b[i];
        }
        buffer.offset = b.length; // impurity here


        return newBuffer;
    }

    public String[] handleRespArray(BufferedInputStream in, RespBuffer buffer) throws Exception{
        buffer.stack++;
        int messageLen = buffer.messageLen;
        String[] res = new String[messageLen];
        buffer.buffer = trimBuffer(buffer, false);
        

        for (int i = 0; i < messageLen; i++){ // yeah its kille
            res[i] = (String) parseRespValue(in, buffer); // might be broken for nested arrays lol
        }


        buffer.stack--;
        return res;
    }


    public Object parseRespValue(BufferedInputStream in, RespBuffer buffer) throws Exception {
        getRespType(in, buffer);
        return handleRespTypeCase(in, buffer, buffer.respType);
    }

    public void getRespType(BufferedInputStream in, RespBuffer buffer) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(new String(buffer.buffer, 0, buffer.offset));
        int bytesRead = 0;

        boolean foundNurse = false;

        if (sb.toString().contains("\r\n")) foundNurse = true;

        while (!foundNurse){
            bytesRead = in.read(buffer.buffer, buffer.offset, buffer.size - buffer.offset);
            if (bytesRead == -1) {
                if (sb.toString().contains("\r\n")) {
                    foundNurse = true;
                }
                else throw new Exception("Empty input stream before valid resp string");

            }
            else {
                String temp = new String(buffer.buffer, buffer.offset, bytesRead);
                sb.append(temp);
                buffer.offset += bytesRead;
                if (sb.toString().contains("\r\n")) foundNurse = true;

            }
            }

        String s = sb.toString();
        int idx = s.indexOf("\r\n");
        String respType = s.substring(0, idx);


        if (s.charAt(0) != '+' && s.charAt(0) != '-') {
            buffer.messageLen = Integer.valueOf(s.substring(1, idx));
        }
        buffer.setRespType(respType);
        
        return;
    }


    public boolean isRespTypeValid(String respType){
        char c = respType.charAt(0);

        return (c == ':' || c == '+' || c == '-' || c == '*' || c == '$');

    }

    public String parseBulkString(BufferedInputStream in, RespBuffer buffer) throws Exception {
        int n = 0;
        StringBuilder sb = new StringBuilder();
        int expectedSize = buffer.messageLen + 2; // what if the buffer alr has values inside!!! 
        int diff = buffer.offset - (buffer.respType.length() + 2); // wtf was i trying to do here!
        expectedSize -= diff;


        while (expectedSize > 0) {
            n = in.read(buffer.buffer, buffer.offset, buffer.size - buffer.offset);
            if (n == -1) throw new Exception("Error reading from input stream");
            expectedSize -= n;
            String temp = new String(buffer.buffer, buffer.offset, n);
            sb.append(temp);
            buffer.offset += n;
        }
        if (!isValidBulkString(buffer)) throw new Exception("Invalid Bulk String");


        String bulkString = new String(buffer.buffer).substring(buffer.respType.length() +2, buffer.respType.length() + 2 + buffer.messageLen);
        //String bulkString = sb.toString().split("\r\n")[0];
        buffer.buffer = trimBuffer(buffer, true);
        return bulkString;


    }

    public boolean isValidBulkString(RespBuffer buffer){
        String s = new String(buffer.buffer, 0, buffer.offset);

        int idx = s.indexOf("\r\n");
        if (idx == -1){
            System.out.println("No registered nurse"); 
            return false;
        }
        int size = Integer.valueOf(s.substring(1, idx));
        String ss = s.substring(idx, size + 2 + idx); // i enforce the size to be true here. if it was bigger i wouldnt catch it. 

        if (!s.substring(size + 2 + idx, size + 4 + idx).equals("\r\n")) {
            System.out.println("Size mismatch");
            return false;
        }

        return true;
    }


    public Object handleRespTypeCase(BufferedInputStream in, RespBuffer buffer, String respType) throws Exception {
        if (!isRespTypeValid(respType)) throw new Exception("Invalid RespType");
        char c = respType.charAt(0);
        switch (c){
            case '$':
                return parseBulkString(in, buffer);
            case ':':
                return String.valueOf(parseRespInteger(in, buffer));
                // call parse integers
            case '+':
                buffer.buffer = trimBuffer(buffer, false);
                return buffer.respType;
                // parse simple message
            case '*':
                // call parse arrays
                return handleRespArray(in, buffer);
            case '-':
                // idk
                buffer.buffer = trimBuffer(buffer, false);
                return buffer.respType;
            
            default:
                return "";

        }

    }


    public long parseRespInteger(BufferedInputStream in, RespBuffer buffer) throws Exception {
        char sign = buffer.respType.charAt(1);
        System.out.printf("///////////////////////////////////////////////// Sign: %c\n", sign);

        if (sign == '+'){
            long res = Long.valueOf(buffer.respType.substring(1));
            buffer.buffer = trimBuffer(buffer, false);
            return res;
        } 

        if (sign == '-'){
            long res = Long.valueOf(buffer.respType.substring(1));
            buffer.buffer = trimBuffer(buffer, false);
            return res;
        } 

        throw new Exception("Invalid Resp integer");
    }
    


    // i just want to return a command array ok. 
    public String readBulkString(BufferedInputStream in, RespBuffer buf) throws Exception{
        int bufferSize = 1024;
        int bytesRead = 0;

        StringBuilder sb = new StringBuilder();

        while (true) {
            bytesRead = in.read(buf.buffer, buf.offset, buf.size);
            if (bytesRead == -1){
                break;
            }
            sb.append(new String(buf.buffer, buf.offset, buf.size - buf.offset));
            buf.offset += bytesRead;
            if (sb.toString().contains("\r\n")); break;

        }

        int idx = sb.toString().indexOf("\r\n");
        String s = sb.toString().substring(0, idx);
        return s;

        //return handleBuildString(in, buf, sb);
    }

    public void typeOfString(String s){
        if (s.charAt(0) == '$'){ // bulk string
            // ohhh set state as bulk strign me thinks
        }
    }

    public String handleBuildString(BufferedInputStream in, RespBuffer buffer, StringBuilder sb) throws Exception {
        String s = sb.toString();
        int idx = s.indexOf("\r\n");
        if (idx == -1) throw new Error("No registered nurse"); // error here maybe throw something idk
        int respLen = Integer.valueOf(s.substring(1, idx));
        int currLen = s.substring(idx).length();
        int bytesRread = 0;

        while (currLen < respLen){ // or until it hits a registered nurse
            bytesRread = in.read(buffer.buffer, buffer.offset, buffer.size);
            if (bytesRread == -1) {
                throw new Error("Some error while reading bytes");
            }
            sb.append(new String(buffer.buffer, buffer.offset, buffer.size - buffer.offset));
            buffer.offset += bytesRread;

        }

        String res = sb.substring(1 + respLen + 2); // this should include the new line i think? not sure
        in.close();
        return res;
    }



    public static String parseCommandString(String message){
        boolean valid = isValidCommand(message);
        if (valid){
            String[] commandArray = buildArray(message);
            String respString = buildRespString(commandArray);
            return respString;
        }
        return "-ERR unknown command";
    }



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
        
        String respString = getRespString(reader); 

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
