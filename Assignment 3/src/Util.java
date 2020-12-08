import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

/*
@author: Cai Songge
@date: 3/12/2020
 */

public class Util {
    public static int getAvailablePort(){
        Random rand = new Random();
        while(true){
            try{
                int port = rand.nextInt(65535);
                ServerSocket socket = new ServerSocket(port);
                socket.close();
                return port;
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }
    }
}