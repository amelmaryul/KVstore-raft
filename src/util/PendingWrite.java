package util;

import java.util.concurrent.CompletableFuture;

public class PendingWrite {
    public String[] command;
    public CompletableFuture<Void> applied = new CompletableFuture<>();


    public PendingWrite(String[] command){
        this.command = command;
    }
}
