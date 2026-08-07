package util;

public class RespBuffer {
    byte[] buffer;
    int offset = 0;
    int size;
    int messageLen = 0;
    String respType;

    public RespBuffer(int size){
        this.size = size;
        buffer = new byte[this.size];
    }


    public void setRespType(String respType){
        this.respType = respType;
    }

    
}