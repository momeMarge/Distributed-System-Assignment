import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/*
@author: Cai Songge
@date: 3/12/2020
@description: Client list stores the all nodes, which is easy for the server to find a client
 */

public class ClientList implements Serializable {
    private ArrayList<Node> clientNodeList;

    public ClientList() {
        this.clientNodeList = new ArrayList<>();
    }

    public void add(Node n) {
        this.clientNodeList.add(n);
    }

    public void remove(Node n)
    {
        this.clientNodeList.remove(n);
    }

    public int getCount()
    {
        return this.clientNodeList.size();
    }

    //find the client by user name
    public Node find(String username)
    {
        Iterator<Node> iter = this.clientNodeList.iterator();
        while(iter.hasNext()){
            Node n = iter.next();
            if(n.getUsername().equals(username)){
                return n;
            }
        }
        return null;
    }

    //find the client by index
    public Node find(int index) {
        return this.clientNodeList.get(index);
    }
}
