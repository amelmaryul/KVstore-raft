import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Coordinator {

    ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    List<LinkedBlockingQueue<String[]>> queueList = new ArrayList<>();

    

    public void handleAck(String[] command){
        map.put(command[0], map.getOrDefault(command[0], 0) +1);



    }

    private void sendmessageidk(int ackId){
        // idk do somethign here like i forgot wait. something like hey all nodes update your maps!!!!!!!!!!!!!!!
        // ok so this means i should tell that one node that asked for the set that yeah you can actually set it has been successfull
        // i think behind the scenes this means we have a majority and therefore we need the rest of the nodes to update each other.
        for (LinkedBlockingQueue<String[]> q : queueList){
            //sq.add(command[0]);
        }

    }

    public void addCommandToQueue(String[] command){
        for (LinkedBlockingQueue<String[]> q : queueList){
            q.add(command);
        }
    }


    public void registerQueue(LinkedBlockingQueue<String[]> queue){
        queueList.add(queue);
    }
}
