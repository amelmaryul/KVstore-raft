public class Main {






    public static void main(String[] args){
        new Thread(() -> ClientServer.main(args)).start();
        new Thread(() -> RaftNode.main(args)).start();
   
    }
    
}
