package util;

public class State {
    byte[] buffer;
    int offset = 0;
    int size;
    int messageLen = 0;

    public State(int size){
        this.size = size;
        buffer = new byte[this.size];
    }



    
}