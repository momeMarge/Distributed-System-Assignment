import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/*
@author: Cai Songge
@date: 3/12/2020
this class defines a receive thread of client, receiving a message from another client or server.
 */

public class ClientReceiveThread extends Thread {
    private Socket socket;
    private List list;
    private TextArea textAreaOut;
    private TextField textfield;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Node node;
    private InetAddress ip;
    private int port;
    private ServerSocket serversocket;
    private int selectedPort;
    private TextArea textAreaIn;

    /*
    constructor of client receiving messages thread
     */
    public ClientReceiveThread(Node node, Socket socket, ObjectInputStream in, ObjectOutputStream out, List list,
                               TextArea taRecord, TextArea taInput, TextField textfield, InetAddress ip, int port,
                               int selectedPort) {
        this.node = node;
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.list = list;
        this.textAreaOut = taRecord;
        this.textAreaIn = taInput;
        this.textfield = textfield;
        this.ip = ip;
        this.port = port;
        this.selectedPort = selectedPort;
    }

    /*
    this thread will listen the client receiving message
     */
    public void run() {
        try {
            serversocket = new ServerSocket(this.port);
            while (true) {
                //设置接收信息流(接收客户端发送的信息）
                Socket clientsocket = serversocket.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientsocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientsocket.getInputStream());
                String type = (String) in.readObject();
                if (type.equalsIgnoreCase("remote message")) {
                    String name = (String)in.readObject();
                    String msg = (String) in.readObject();
                    textAreaOut.append(name + ":" + "\n");
                    textAreaOut.append(" " + msg + "\n");
                    out.close();
                    in.close();
                    clientsocket.close();
                }
//                else if (type.equalsIgnoreCase("kick client")) {
//                    out.close();
//                    in.close();
//                    clientsocket.close();
//                    this.in.close();
//                    this.out.close();
//                    this.socket.close();
//                    System.exit(0);
//                }
            }
        } catch (Exception e1) {
            textAreaOut.append("client receive thread error: " + e1.toString());
        }
    }
}
