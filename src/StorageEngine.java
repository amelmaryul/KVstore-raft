
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
/*
An idea for this file is i think i should have a default constructor that just returns the one instance of storageEngine
*/
import java.util.concurrent.LinkedBlockingQueue;

public class StorageEngine {
   private ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>(); 
   public final LinkedBlockingQueue<String[]> queue = new LinkedBlockingQueue<>(); // what is this for again i forget lol


   public static final StorageEngine instance = new StorageEngine();

   private StorageEngine(){

   }

   public static StorageEngine getInstance(){
    return instance;
   }


   
    public synchronized void set(String key, String value){
        store.put(key, value);
        return;
    }

    public synchronized String get(String key){
        return store.get(key);
    }

    public String delete(String key){
        return store.remove(key);
    }

    public void writeToFile(String fileName, String key, String value){
        try {
        FileWriter writer = new FileWriter(fileName, true);
        writer.write(key + "=" + value + "\n");
        writer.close();

        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public void writeToFile(String fileName, String key, String value, FileWriter writer){
        try {
        writer.write(key + "=" + value + "\n");

        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public void clearFile(String fileName){
        try{
            FileWriter w = new FileWriter(fileName);
            w.write("");
            w.close();
        } catch (IOException exception){
            exception.printStackTrace();
        }
    }

    public void dumpToFile(String fileName){
        clearFile(fileName);
        try{
            FileWriter w = new FileWriter(fileName, true);
            store.forEach((key, value) -> {
                writeToFile(fileName, key, value, w);
            });
            w.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void readFile(String filename){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null){
                String[] values = line.split("=");
                store.put(values[0], values[1]);
            }
            reader.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
