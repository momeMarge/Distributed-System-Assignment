import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/*
@author: Cai Songge
@date: 3/12/2020
this class defines a receive message thread of server through the client
 */
public class ServerReceiveThread extends Thread {
    private TextArea textAreaOut;
    private List list;
    private TextField textfield;
    private ClientList clientList;
    private Node node;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ArrayList<ObjectOutputStream> allOut;
    private boolean isStop;
    private String message;

    //constructor
    public ServerReceiveThread(Socket socket,TextArea taRecord,TextField textfield,List list,Node node,
                               ClientList clientList, ObjectInputStream in, ObjectOutputStream out,
                               ArrayList<ObjectOutputStream> allOut, String message)
    {
        this.socket = socket;
        this.textAreaOut = taRecord;
        this.textfield = textfield;
        this.list = list;
        this.node = node;
        this.clientList = clientList;
        this.in = in;
        this.out = out;
        this.isStop = false;
        this.allOut = allOut;
        this.message = message;
    }

    /*
    details about the server receives thread
     */
    public void run() {
        //update user list
        sendUserList();
        while(true) {
            try {
                String request = (String)in.readObject();
                textAreaOut.append(request + ": ");
                //listen the logout request of clients
                if(request.equalsIgnoreCase("logout")) {
                    Node client = clientList.find(node.getUsername());
                    String msg = "User " + node.getUsername() + " log out\n";
                    textAreaOut.append(" message: ");
                    textAreaOut.append(msg + "\n");

                    //update user list
                    int j = searchIndex(clientList, node.getUsername());
                    clientList.remove(client);
                    this.allOut.remove(j);
                    list.removeAll();
                    int count = clientList.getCount();
                    for (int i = 0; i < count; i++){
                        client = clientList.find(i);
                        if(client != null)
                            list.add(client.getUsername());
                    }
                    this.message = "online user num: " + clientList.getCount() + "\n";
                    textfield.setText(this.message);

                    //update user list
                    sendUserList();

                    //broad cast to announce logout
                    logout(msg);
                    break;
                }
                //listen the kick message of clients with error
//                else if(request.equalsIgnoreCase("kick message")){
//                    String name = (String)in.readObject();
//                    Node client = clientList.find(name);
//                    String msg = "User " + client.getUsername() + " is kicked out\n";
//                    textAreaOut.append(" message: ");
//                    textAreaOut.append(msg + "\n");
//
//                    //update user list
//                    int j = searchIndex(clientList, name);
//                    clientList.remove(client);
//                    this.allOut.remove(j);
//                    list.removeAll();
//                    int count = clientList.getCount();
//                    for (int i = 0; i < count; i++){
//                        client = clientList.find(i);
//                        if(client != null)
//                            list.add(client.getUsername());
//                    }
//                    this.message = "online user num: " + clientList.getCount() + "\n";
//                    textfield.setText(this.message);
//
//                    break;
//                }

            }catch(Exception e) {
                textAreaOut.append("server receive thread error: " + e.toString() + "\n");
            }
        }
    }

    /*
    server sends a board cast logout message to all connected client
    type: logout message
     */
    public void logout(String msg) {
        try {
            Iterator<ObjectOutputStream> it = this.allOut.iterator();
            while(it.hasNext()) {
                ObjectOutputStream tout = it.next();
                //在输出流中写入全局输出信息
                tout.writeObject("logout message");
                tout.flush();
                tout.writeObject(msg);
                tout.flush();
                //socket.close();
            }
        }
        catch(Exception e) {
            textAreaOut.append("logout boardCast message error: " + e.toString());
        }
    }

    /*
    server will send a broad cast message to all client in the user list
    type: list message
     */
    public void sendUserList() {
        //获取当前在线用户的列表
        String userlist = "";
        int count = clientList.getCount();
        for (int i = 0; i < count; i++){
            Node client = clientList.find(i);
            if (client != null){
                userlist += client.getUsername();
                userlist += "@@";
            }
        }
        try {
            //设置所有输出流的信息
            Iterator<ObjectOutputStream> it = this.allOut.iterator();
            while(it.hasNext()){
                //按迭代器顺序更新单个用户的信息
                ObjectOutputStream itout = it.next();
                    itout.writeObject("list message");
                    itout.flush();
                    //以@@分隔开的所有的user名单
                    itout.writeObject(userlist);
                    itout.flush();
                    itout.writeObject(this.message);
                    itout.flush();

                    //检查当前的用户是否在out的list名单中，写入当前itme的client list
                    if(itout != out){
                        itout.writeObject(clientList.find(clientList.getCount()-1));}
                    else
                        itout.writeObject(clientList);
                    itout.flush();
            }

            //在线用户数量不为0
            if(clientList.getCount() != 0) {
                textAreaOut.append("Now the online user list: ");
                for(int r = 0; r < clientList.getCount(); r++) {
                    textAreaOut.append(clientList.find(r).getUsername() + " ");
                }
                textAreaOut.append("\n");
            }
        }
        catch(Exception e) {
            textAreaOut.append("server user list error:" + e.toString() + "\n");
        }
    }

    /*
    this method will search the index of a client by client name, used to delete log out user by name
     */
    public int searchIndex(ClientList clientList, String name) {
        int count = clientList.getCount();
        int i = 0;
        while(i < count) {
            Node client = clientList.find(i);
            if(!name.equalsIgnoreCase(client.getUsername()))
                i++;
            else
                return i;
        }
        return i;
    }
}
