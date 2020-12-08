import java.io.Serializable;
import java.net.InetAddress;

/*
@author: Cai Songge
@date: 3/12/2020
 */

public class Node implements Serializable {
    private String username;
    private InetAddress ip;
    private int port;

    public Node(String un, InetAddress ip, int port){
        this.username = un;
        this.ip = ip;
        this.port = port;
    }

    public String getUsername(){
        return username;
    }

    public InetAddress getIp(){
        return ip;
    }

    public int getPort(){
        return port;
    }

}
