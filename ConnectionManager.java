import java.net.Socket;
import java.util.ArrayList;
import java.io.IOException;
import java.net.ServerSocket;
import static java.lang.System.out;

public class ConnectionManager extends Thread {

    private int port;
    private int maximumConnections;
    public ServerSocket serverSocket;
    public ArrayList<Connection> connections = new ArrayList<>();

    public ConnectionManager(int port, int maximumConnections) {
        this.port = port;
        this.maximumConnections = maximumConnections;
        this.setName("Connection manager thread");
    }

    @Override
    public void run() {
        out.println(this + " : just started");

        try {
            serverSocket = new ServerSocket(port, maximumConnections);
        } catch (IOException e) {
            out.println(this + " : can't create new ServerSocket on port " + port); System.exit(-1);
        }

        out.println(this + " : created new ServerSocket on port " + port);

        Socket client;
        Connection clientConnection;

        while (!serverSocket.isClosed())
        {
            try {
                out.println(this + " : waiting for connection...");
                client = serverSocket.accept();

                out.println(this + " : new connection " + client.getInetAddress().getHostAddress());
                clientConnection = new Connection(client, connections);
                connections.add(clientConnection);
                clientConnection.start();

                out.println(this + " : new connection thread just started");
            } catch (IOException e) {
                out.println(this + " : accept error");
                out.println(e);
            } catch (Connection.ConnectionConstructorException e) {
                out.println(this + " : new Connection constructor error");
                out.println(e);
            }
        }
    }

    public String toString() {
        return "[Connection manager]";
    }
}