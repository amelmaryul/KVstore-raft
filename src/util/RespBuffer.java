package util;

public class RespBuffer {
    byte[] buffer;
    int offset = 0;
    int size;
    int messageLen = 0;
    String respType;
    int stack; // quick fix for recursive arrays. 

    public RespBuffer(int size){
        this.size = size;
        buffer = new byte[this.size];
        this.stack = 0; 
    }


    public void setRespType(String respType){
        this.respType = respType;
    }

    
}