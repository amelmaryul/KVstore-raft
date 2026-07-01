import java.io.*;
import java.util.stream.*;
import java.nio.file.*;
/*
this is me 10 days after buildign the first tier of this project
I currently dont know what the file does
Although i think i just made it to test some things out


*/
public class Files {
    private String name;
    public Files(String name){
        this.name = name;

    }


    



    public static void main(String[] args){
        String file = "idkmate.txt";
        
        try (FileWriter writer = new FileWriter(file)){
            writer.write("I love Clairo!!!!!!!!!!!");
            writer.write("\n");
            writer.write("This is the Second line. I love Charm!!!!!");
            System.out.println("FINIHSHED WRITING TO FILE 1111111111111111111111111111111111");

        }catch (IOException e){
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            System.out.println(reader.lines());
            
            Stream<String> stream = reader.lines();
            stream.forEach(s -> System.out.println(s));
            System.out.println("Finished reading lil bro");
            
            System.out.println(reader.readLine());
            reader.close();
        } catch (IOException exception){
            exception.printStackTrace();
        }



    }
}

