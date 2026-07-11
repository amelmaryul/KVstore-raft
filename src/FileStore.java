import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileStore {
    int nodeId;
    String logFile;
    String stateFile;
    PrintWriter pw;
    RandomAccessFile raf;
    List<Long> byteLine = new ArrayList<>();

    Object stateLock = new Object();
    Object logLock = new Object();
    

    public FileStore(int nodeId){
        this.nodeId = nodeId;
        logFile = "../" + String.valueOf(nodeId) + "-logs.txt";
        stateFile = "../" + String.valueOf(nodeId) + "-state.txt";
        byteLine.add(0L);
        try{
            pw = new PrintWriter(new FileWriter(logFile, true));
            raf = new RandomAccessFile(logFile, "rw");
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    public int[] initializeRaftState(){
        synchronized (stateLock){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(stateFile));
                String line = reader.readLine();
                int term = Integer.valueOf(line.split(",")[1]);
                line = reader.readLine();
                int votedFor = Integer.valueOf(line.split(",")[1]);
                reader.close();
                return new int[] {term, votedFor};

            } catch (Exception e) {
                return null;
            }
        }
    }


    public boolean updateRaftState(int term, Integer votedFor){
        synchronized (stateLock){
            try {
                votedFor = (votedFor == null) ? -1 : votedFor;
                PrintWriter printWriter = new PrintWriter(new FileWriter(stateFile));
                printWriter.println("term," + String.valueOf(term));
                printWriter.println("votedFor," + String.valueOf(votedFor));
                printWriter.flush();
                printWriter.close();
                return true;

            } catch (Exception e) {
                return false;
            }
        }
   }







    public boolean appendLog(LogEntry logEntry){
        synchronized (logLock){
            try {
                String line = serialize(logEntry);
                pw.println(line);
                pw.flush();
                Long b = (long) (line + "\r\n").getBytes(StandardCharsets.UTF_8).length;
                byteLine.add(byteLine.getLast() + b);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }



    public List<LogEntry> initializeLogs(){
        synchronized (logLock){
            try {
                List<LogEntry> res = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null){
                    byte[] bytes = (line + "\r\n").getBytes(StandardCharsets.UTF_8);
                    byteLine.add(byteLine.getLast() + bytes.length);
                    LogEntry logEntry = parse(line);
                    res.add(logEntry);
                }
                reader.close();

                return res;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private LogEntry parse(String line){
        String[] arr = line.split(",", 3);
        int term = Integer.valueOf(arr[0]);
        int index = Integer.valueOf(arr[1]);
        String commandString = arr[2];
        String[] command = commandString.split(";"); 
        return new LogEntry(command, term, index);
    }


    private String serialize(LogEntry logEntry){
        String res = String.valueOf(logEntry.term) + "," + String.valueOf(logEntry.index) + ",";
        for (String s : logEntry.command){
            res += s + ";";
        }
        
        return res;
    }


    public void delBytes(int index){
        synchronized (logLock){
            while (byteLine.size() > index){
                byteLine.removeLast();
            }
        }
       }

    public void trunacate(int index){
        synchronized (logLock){
            try {
                delBytes(index);
                raf.setLength(byteLine.getLast());
                pw.close();
                pw = new PrintWriter(new FileWriter(logFile, true));
            } catch (Exception e) {
                return;
            }
 
        }
   }

}