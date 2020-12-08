import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/*
@author: Cai Songge
@date: 3/12/2020
 */

public class Server extends JFrame implements ActionListener {

    private JButton sendButton, cleanButton, closeButton, startButton, showCommandButton;
    private TextArea record, input;
    private TextField textfield;
    private List list;
    private ClientList clientList;
    private ServerSocket serversocket;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ArrayList<ObjectOutputStream> allOut;
    private String message;
    private InetAddress ip;
    private int port;
    private java.util.List<String> comments;
    private static boolean isStop;
    private static Server server;

    public static void main(String[] args) {
        server = new Server();
    }

    /*
    server constructor will set the graphics UI directly
     */
    public Server() {
        //set all buttons
        this.allOut = new ArrayList<>();
        sendButton = new JButton("send");
        cleanButton = new JButton("clean");
        closeButton = new JButton("stop");
        startButton = new JButton("start");
        showCommandButton = new JButton("show commands");
        closeButton.setEnabled(false);
        sendButton.setEnabled(false);
        cleanButton.setEnabled(false);
        showCommandButton.setEnabled(false);

        record = new TextArea("",14,50);
        record.setBackground(Color.white);
        input = new TextArea("",4,50);
        input.setBackground(Color.white);
        textfield = new TextField();
        textfield.setBackground(Color.white);
        record.setEditable(false);
        textfield.setEditable(false);

        list = new List();

        /*
        setting left panel of chat room
         */
        Panel p1 = new Panel();
        p1.setLayout(new BorderLayout());
        p1.add(new Label("online list"), BorderLayout.NORTH);
        p1.add(textfield, BorderLayout.CENTER);

        Panel lPanel = new Panel();
        lPanel.setLayout(new BorderLayout());
        lPanel.add(p1,BorderLayout.NORTH);
        lPanel.add(list,BorderLayout.CENTER);
        lPanel.add(showCommandButton,BorderLayout.SOUTH);

        /*
        setting right panel of chat room
         */
        Panel p3 = new Panel();
        p3.setLayout(new GridLayout(1,2,90,4));
        p3.add(startButton);
        p3.add(closeButton);

        Panel history = new Panel();
        history.setLayout(new BorderLayout());
        history.add(new Label("chat history"), BorderLayout.NORTH);
        history.add(record, BorderLayout.CENTER);

        Panel p9 = new Panel();
        p9.setLayout(new BorderLayout());
        p9.add(p3,BorderLayout.NORTH);
        p9.add(history,BorderLayout.CENTER);

        Panel pInput = new Panel();
        pInput.setLayout(new BorderLayout(5,9));
        pInput.add(p9,BorderLayout.CENTER);
        pInput.add(input, BorderLayout.SOUTH);

        Panel controlBtn2 = new Panel();
        controlBtn2.setLayout(new GridLayout(1,2,90,50));
        controlBtn2.add(sendButton);
        controlBtn2.add(cleanButton);

        Panel rPanel = new Panel();
        rPanel.setLayout(new BorderLayout());
        rPanel.add(pInput,BorderLayout.CENTER);
        rPanel.add(controlBtn2,BorderLayout.SOUTH);

        Panel chatApp = new Panel();
        chatApp.setLayout(new BorderLayout());
        chatApp.setBackground(new Color(120,179,255));
        chatApp.add(lPanel,BorderLayout.WEST);
        chatApp.add(rPanel,BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(chatApp,BorderLayout.CENTER);

        setSize(500,500);
        setTitle("chatRoom");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setVisible(true);
        sendButton.addActionListener(this);
        cleanButton.addActionListener(this);
        closeButton.addActionListener(this);
        startButton.addActionListener(this);
        showCommandButton.addActionListener(this);
    }

    /*
    this method uses to listen the different server event buttons: "start server", "stop server", "send messages"
    and "clean input field"
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startButton)
            startServer();
        else if(e.getSource() == closeButton) {
            stopServer();
            System.exit(0);
        }
        else if(e.getSource() == sendButton) {
            if(input.getText().equalsIgnoreCase("") || input.getText() == null) {
                JOptionPane.showMessageDialog(this, "You have not enter the chat information!","Warning" , JOptionPane.INFORMATION_MESSAGE);
            }
            sendSystemMessage();
        }
        else if(e.getSource() == cleanButton) {
            input.setText("");
        }
        else if(e.getSource() == showCommandButton) {
            String comment = comments.toString();
            record.append(comment);
        }
    }

    /*
    method to start server working
     */
    public void startServer() {
        try {
            serversocket=new ServerSocket(1234);
            record.append("waiting for connecting..."+"\n");
            startButton.setEnabled(false);
            closeButton.setEnabled(true);
            sendButton.setEnabled(true);
            cleanButton.setEnabled(true);
            showCommandButton.setEnabled(true);
            this.isStop = false;
            clientList = new ClientList();
            ServerListenThread serverlistenthread = new ServerListenThread(serversocket, record, textfield, list,clientList);
            serverlistenthread.start();
        }
        catch(Exception e) {
            record.append("start server error");
        }
    }

    /*
    method to stop server working
     */
    public void stopServer() {
        try {
            this.isStop = true;
            serversocket.close();
            socket.close();
            list.removeAll();
        }
        catch(Exception e) {
            record.append("server has closed now");
        }
    }

    /*
    this method will send a system message to all connecting clients
     */
    public void sendSystemMessage() {
        String message = input.getText();
        record.append("System announce: " + input.getText()+"\n");
        input.setText("");

        try {
            Iterator<ObjectOutputStream> it = this.allOut.iterator();
            while(it.hasNext()){
                ObjectOutputStream tout = it.next();
                tout.writeObject("system message");
                tout.flush();
                tout.writeObject(message);
                tout.flush();
            }
        }
        catch(Exception e) {
            record.append("system customize error: " + e.toString());
        }
    }

    /*
    set server listener thread about clients log in & connecting clients number
     */
    public class ServerListenThread extends Thread{
        ServerSocket serversocket;
        TextArea record;
        List list;
        TextField textfield;
        ClientList clientList;
        Node node;
        ServerReceiveThread serverreceivethread;

        public ServerListenThread(ServerSocket serversocket,TextArea taRecord,TextField textfield,List list,
                                  ClientList clientList) {
            this.serversocket = serversocket;
            this.record = taRecord;
            this.textfield = textfield;
            this.list = list;
            this.clientList = clientList;

        }

        /*
        this thread will listen the client message and receive it
         */
        public void run() {
            while(!isStop && !serversocket.isClosed()) {
                try {
                    //等待接收client连接，设置用户信息
                    socket = serversocket.accept();
                    ip = socket.getInetAddress().getByName(socket.getInetAddress().getHostAddress());
                    out = new ObjectOutputStream(socket.getOutputStream());
                    allOut.add(out);

                    //input中只包含username & port，此时server输出当前client连接成功的提示
                    in = new ObjectInputStream(socket.getInputStream());
                    node = new Node((String)in.readObject(), ip, in.readInt());
                    record.append("Congratulations! " + node.getUsername() + " connects successfully!" + "\n" +
                            "The client " + node.getUsername() + "'s address is " + ip + ":" + node.getPort() + "\n");
                    record.append("User " + node.getUsername() + " has logged in\n");

                    //left online user info set & update online user number
                    list.add(node.getUsername());
                    clientList.add(node);
                    message = "online user num: " + clientList.getCount() + "\n";
                    textfield.setText(message);

                    server.setOut(out);

                    //set a new receive thread of server after connection
                    serverreceivethread = new ServerReceiveThread(socket, record, textfield, list, node, clientList, in,
                            out, Server.this.allOut, message);
                    serverreceivethread.start();
                }
                catch(Exception e) {
                    //stop listening
                    record.append("server socket error, please check your server connection" + e.toString());
                }
            }
        }
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }
    public ObjectOutputStream getOut() {
        return out;
    }

}
