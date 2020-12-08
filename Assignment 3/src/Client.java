import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
@author: Cai Songge
@date: 3/12/2020
 */

public class Client extends JFrame implements ActionListener {
    private JButton sendButton, cleanButton, logoutButton, loginButton, kickButton;
    private TextArea record, input;
    private TextField onlineUser, usernametext;
    private List list;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ClientList clientList;
    private Socket socket;
    private Node node;
    private int selectedPort;
    private int clientListenPort;
    private InetAddress ip;
    private int port;
    private ClientReceiveThread clientreceivethread;
    private String username;
    private static Client client;
    private SocketAddress socketAddress;

    public Client(){
        //set buttons
        sendButton = new JButton("send");
        cleanButton = new JButton("clean");
        logoutButton = new JButton("log out");
        loginButton = new JButton("log in");
        kickButton = new JButton("kick the user");
        logoutButton.setEnabled(false);
        sendButton.setEnabled(false);
        cleanButton.setEnabled(false);
        kickButton.setEnabled(false);

        //set record and input area
        record = new TextArea("",14,50);
        record.setBackground(Color.white);
        input = new TextArea("",4,50);
        input.setBackground(Color.white);
        onlineUser = new TextField();
        onlineUser.setBackground(Color.white);
        usernametext = new TextField();
        usernametext.setBackground(Color.white);
        record.setEditable(false);
        onlineUser.setEditable(false);

        //all user
        list = new List();
        list.add("all");

        /*
        left panel shows all online user
         */
        Panel p1 = new Panel();
        p1.setLayout(new BorderLayout());
        p1.add(new Label("online list"),BorderLayout.NORTH);
        p1.add(onlineUser,BorderLayout.CENTER);

        Panel lPanel = new Panel();
        lPanel.setLayout(new BorderLayout());
        lPanel.add(p1,BorderLayout.NORTH);
        lPanel.add(list,BorderLayout.CENTER);
        lPanel.add(kickButton, BorderLayout.SOUTH);

        /*
        right panel shows the login user info, chat history and send message box
         */
        Panel user = new Panel();
        user.setLayout(new GridLayout(1,4));
        user.add(new Label("User name"));
        user.add(usernametext);
        user.add(loginButton);
        user.add(logoutButton);

        Panel pRecord = new Panel();
        pRecord.setLayout(new BorderLayout());
        pRecord.add(new Label("chat history"), BorderLayout.NORTH);
        pRecord.add(record,BorderLayout.CENTER);

        Panel p9 = new Panel();
        p9.setLayout(new BorderLayout());
        p9.add(user,BorderLayout.NORTH);
        p9.add(pRecord,BorderLayout.CENTER);

        Panel pSend = new Panel();
        pSend.setLayout(new BorderLayout(5,9));
        pSend.add(p9,BorderLayout.CENTER);
        pSend.add(input,BorderLayout.SOUTH);

        Panel sendBtn = new Panel();
        sendBtn.setLayout(new GridLayout(1,2,90,50));
        sendBtn.add(sendButton);
        sendBtn.add(cleanButton);

        Panel rPanel = new Panel();
        rPanel.setLayout(new BorderLayout());
        rPanel.add(pSend,BorderLayout.CENTER);
        rPanel.add(sendBtn,BorderLayout.SOUTH);

        Panel pAll = new Panel();
        pAll.setLayout(new BorderLayout(5,5));
        pAll.setBackground(new Color(120,179,255));
        pAll.add(lPanel,BorderLayout.WEST);
        pAll.add(rPanel,BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(pAll,BorderLayout.CENTER);

        /*
        set the server window
         */
        setSize(500,500);
        setTitle("Chat Room");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        sendButton.addActionListener(this);
        cleanButton.addActionListener(this);
        loginButton.addActionListener(this);
        logoutButton.addActionListener(this);
        kickButton.addActionListener(this);
        list.addActionListener(this);

    }

    //start a new client
    public static void main(String[] args) {
        client=new Client();
    }

    /*
    this method uses to listen the different server event buttons: "login server", "logout server", "send messages"
    and "clean input field"
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == loginButton)
            Login();
        else if(e.getSource() == logoutButton) {
            Logout();
            System.exit(0);
        }
        else if(e.getSource() == sendButton) {
            if(input.getText().equalsIgnoreCase("") || input.getText() == null) {
                JOptionPane.showMessageDialog(this, "You have not enter the chat information!",
                        "Warning" , JOptionPane.INFORMATION_MESSAGE);
            }
            sendMessage();
        }
        else if(e.getSource() == cleanButton) {
            input.setText("");
        }
        else if(e.getSource() == kickButton) {
            kickClient();
        }
    }

    /*
    method to login a client
     */
    public void Login() {
        new Thread(new ComWithServer()).start();
    }

    /*
    method to logout a client
     */
    public void Logout() {
        logoutButton.setEnabled(false);
        sendButton.setEnabled(false);
        cleanButton.setEnabled(false);
        loginButton.setEnabled(true);

        if(socket.isClosed())
            return;
        try {
            ObjectOutputStream out = client.getOut();
            out.writeObject("logout");
            out.flush();
            in.close();
            out.close();
        }
        catch(Exception e) {
            record.append("log out error: " + e.toString());
        }
    }

    /*
    this method will send messages to all/selected client & server
     */
    public void sendMessage() {
        try {
            String name = usernametext.getText();
            String msg = input.getText();

            //send message to all clients
            if(list.getSelectedIndex() == 0) {
                for(int j = 0; j < clientList.getCount(); j++) {
                    String remoteIp = clientList.find(j).getIp().getHostAddress();
                    int remotePort = clientList.find(j).getPort();
                    //与remote client建立联系, 输出流类型为remote message
                    Socket clientsocket = new Socket(remoteIp, remotePort);
                    ObjectOutputStream out = new ObjectOutputStream(clientsocket.getOutputStream());
                    out.writeObject("remote message");
                    out.flush();
                    out.writeObject(name);
                    out.flush();
                    out.writeObject(msg);
                    out.flush();
                    out.close();
                    clientsocket.close();
                }
            }

            //send message to special client
            else {
                String toSomebody = list.getSelectedItem();
                record.append(name + " said to " + toSomebody + ": "+"\n");
                record.append(" " + msg + "\n");

                //get the remote client ip & port
                String remoteIp = clientList.find(toSomebody).getIp().getHostAddress();
                int remotePort = clientList.find(toSomebody).getPort();
                client.setSelectedPort(remotePort);

                //与remote client建立联系, 输出流类型为remote message
                Socket clientsocket = new Socket(remoteIp,remotePort);
                ObjectOutputStream out = new ObjectOutputStream(clientsocket.getOutputStream());
                out.writeObject("remote message");
                out.flush();
                out.writeObject(name);
                out.flush();
                out.writeObject(msg);
                out.flush();
                out.close();
                clientsocket.close();
            }
            input.setText("");
        }
        catch(Exception ec) {
            record.append("send message exception!" + ec.toString());
        }
    }

    /*
    this method will close the connection between the specified client and server, and send broad cast message to all clients,
    but it still has unknown error
     */
    public void kickClient(){
        try {
            /*
            connect and inform the kicked client
             */
            String toSomebody = list.getSelectedItem();

            //get the remote client ip & port
            String remoteIp = clientList.find(toSomebody).getIp().getHostAddress();
            int remotePort = clientList.find(toSomebody).getPort();
            client.setSelectedPort(remotePort);

            //与remote client建立联系, 输出流类型为kick client
            Socket clientsocket = new Socket(remoteIp,remotePort);
            ObjectOutputStream out = new ObjectOutputStream(clientsocket.getOutputStream());
            out.writeObject("kick client");
            out.flush();
            out.close();

            /*
            send kick message to server
             */
            String kickItem = list.getSelectedItem();
            out = client.getOut();
            out.writeObject("kick message");
            out.flush();
            out.writeObject(kickItem);
            out.flush();
            out.close();
        } catch (IOException e) {
            record.append("kick client error: " + e.toString());
        }
    }

    /*
    thread operation of client that connect with server
     */
    public class ComWithServer implements Runnable{

        public void run() {
            try {
                socket = new Socket("127.0.0.1",1234);
                ip = socket.getLocalAddress();
                username = usernametext.getText();
                record.append("Congratulations! You have connected to server correctly with user name: " + username
                        + ", ip address:" + ip + ", port: " + port + "\n");
                clientListenPort = Util.getAvailablePort();

                //set output string USERNAME + PORT, message type: client to server
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(username);
                out.flush();
                out.writeInt(Client.this.clientListenPort);
                out.flush();
                client.setOut(out);

                in = new ObjectInputStream(socket.getInputStream());
                node = new Node(username, ip, Client.this.clientListenPort);

                //用户与服务器建立连接后设置客户端的接收线程
                int selectedPort = client.getSelectedPort();
                clientreceivethread = new ClientReceiveThread(node, socket, in, out, list, record, input, onlineUser,
                        ip, Client.this.clientListenPort, selectedPort);
                clientreceivethread.start();

                //change the btn state
                loginButton.setEnabled(false);
                logoutButton.setEnabled(true);
                sendButton.setEnabled(true);
                cleanButton.setEnabled(true);
                kickButton.setEnabled(true);

                //start to listen server transfer message
                while(true) {
                    try {
                        //receive input server message in list
                        String type = (String)in.readObject();
                        if(type.equalsIgnoreCase("list message")) {
                            //按顺序获取收到的所有信息，并重新设置左侧用户在线列表
                            String userlist = (String)in.readObject();
                            String username[] = userlist.split("@@");
                            list.removeAll();
                            list.add("all");
                            for(int i = 0; i < username.length; i++){
                                list.add(username[i]);
                            }
                            String onlineNum = (String)in.readObject();
                            onlineUser.setText(onlineNum);

                            //检查是否有新用户
                            Object o = in.readObject();
                            if(o instanceof ClientList)
                                clientList = (ClientList)o;
                            else
                                clientList.add((Node)o);
                        }
                        /*
                        log out message
                         */
                        else if(type.equalsIgnoreCase("logout message")) {
                            String logout = (String)in.readObject();
                            record.append("Announcement: " + logout + "\n");
                        }
                        /*
                        receive system message
                         */
                        else if(type.equalsIgnoreCase("system message")) {
                            String msg = (String)in.readObject();
                            record.append("System: " + msg + "\n");
                        }
                    }
                    catch(Exception e) {
                        record.append("client listen error: "+e.toString());
                    }
                }
            }
            catch(Exception e) {
                record.append("connect to server error: "+e.toString());
            }
        }

    }

    /*
    some set and get methods
     */
    public void setSelectedPort(int port) {
        this.selectedPort = port;
    }
    public int getSelectedPort() {
        return selectedPort;
    }
    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }
    public ObjectOutputStream getOut() {
        return out;
    }
}
