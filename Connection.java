import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import static java.lang.System.out;

public class Connection extends Thread {

    public Socket client;
    public String userName = "USER_NAME";

    private DataOutputStream clientOutputStream;
    private DataInputStream clientInputStream;
    private ArrayList<Connection> connections;

    private String menuHelp;
    private String connectionHelp;
    private String waitForConnectionHelp;
    private String chatHelp;
    private String blacklistHelp;

    public boolean isWaiting = false;

    private void createHelps() {
        menuHelp = "\n===MENU MODE===" +
                "\nThis menu is the main menu of this program. All you need to chatting you can find here." +
                "\n" +
                "\nCOMMANDS :" +
                "\nHELP                 : see mode commands and describe" +
                "\nLIST USERS           : see users online" +
                "\nCONNECT TO USER      : start chat" +
                "\nWAIT FOR CONNECTION  : wait for connection by any other user" +
                "\nBLACKLIST            : add/remove user in/from blacklist" +
                "\nEXIT                 : close program";

        connectionHelp = "\n===CONNECTION MODE===" +
                "\nIn this mode you can try to start chat with any online user, which is in waiting mode." +
                "\n" +
                "\nCOMMANDS :" +
                "\nHELP                     : see mode commands and describe" +
                "\nLIST USERS               : see users online" +
                "\nCONNECT TO [USERNAME]    : try to start chat with user with name USERNAME. Write it without brackets." +
                "\nBACK                     : return to main menu mode";

        waitForConnectionHelp = "\n===WAIT FOR CONNECTION MODE" +
                "In this mode you waiting for connections by another users," +
                "\nyou can accept or cancel their offers to chat." +
                "\n" +
                "\nCOMMANDS :" +
                "\nHELP         : see mode commands and describe" +
                "\nLIST USERS   : " +
                "\nBACK         : return to main menu mode";

        chatHelp = "\n===CHAT MODE===" +
                "\nIn this mode you can chat with your opponent." +
                "\nServer don't save your messages, so they can't be safe between two sessions." +
                "\n" +
                "\nCOMMANDS :" +
                "\nHELP  : see mode commands and describe" +
                "\nCLOSE : close chat session";

        blacklistHelp = "\n===BLACKLIST MODE===" +
                "\nThis mode allows you to add/remove user in/from blacklist." +
                "\nUsers in blacklist can't offer you to start chat." +
                "\nBlacklist can't be safe between two program sessions." +
                "\n" +
                "\nCOMMANDS :" +
                "\nHELP                 : see mode commands and describe" +
                "\nADD [USERNAME]       : add user with name USERNAME in blacklist" +
                "\nREMOVE [USERNAME]    : remove user with name USERNAME from blacklist" +
                "\nBACK                 : return to main menu mode";
    }

    public Connection(Socket client, ArrayList<Connection> connections) throws ConnectionConstructorException {
        this.client = client;
        this.connections = connections;
        try {
            clientOutputStream = new DataOutputStream(client.getOutputStream());
            clientInputStream = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            throw new ConnectionConstructorException(this + " : can't get I/O stream");
        }

        createHelps();
        out.println(this + " : successfully created");
    }

    private void menuMode() {
        try {
            sendMessageToClient(menuHelp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        out.println(this + " : in menu mode");

        String message;

        connection_life_cycle :
        while (!client.isClosed())
            try {
                sendMessageToClient("\n\nset : ");
                message = getMessageFromClient();

                switch (message) {
                    case "HELP" : sendMessageToClient(menuHelp); break;
                    case "LIST USERS" : listUsers(); break;
                    case "CONNECT TO USER" : connectionMode(); break;
                    case "WAIT FOR CONNECTION" : waitForConnectionMode(); break;
                    case "BLACK LIST" : blacklistMode(); break;
                    case "EXIT" : disconnect(); break connection_life_cycle;
                    default : sendMessageToClient("Unknown command \"" + message + "\"");
                }
            } catch (IOException e) {
                disconnect();
            }

        out.println(this + " : menu method closed");
    }

    private void connectionMode() {
        out.println(this + " : in connection mode");
        try {
            sendMessageToClient(connectionHelp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String message;
        Connection connection;

        while (true)
            try {
                sendMessageToClient("\n\nset : ");
                message = getMessageFromClient();

                if (message.equals("LIST USERS")) {
                    listUsers();
                }
                else if (message.startsWith("CONNECT TO ")) {
                    connection = getConnectionByName(message.substring("CONNECT TO ".length()));
                    if (connection != null)
                        chatMode(connection);
                    else sendMessageToClient("no such user in waiting mode...");
                }
                else if (message.equals("BACK"))
                    break;
                else if (message.equals("HELP"))
                    sendMessageToClient(connectionHelp);

            } catch (IOException e) {
                e.printStackTrace();
            }

            out.println(this + " : connection mode closed");
    }

    private void waitForConnectionMode() {
        out.println(this + " : in waiting mode");
//        isWaiting = true;
    }

    private void chatMode(Connection opponent) {
        out.println(this + " : in chat mode");

        try {
            sendMessageToClient("starting chat with opponent " + opponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void blacklistMode() {
        out.println(this + " : in blacklist mode");

    }

    private void listUsers() {
        try {
            String usersTable = "\n\n| INDEX | WAITING | NAME |";
            Connection connection;

            for (int i = 0; i < connections.size(); i++) {
                if (i % 50 == 0 && i != 0) {
                    sendMessageToClient("Printed " + i + "/" + connections.size() + ". Print next 50? (y/n) : ");
                    if (getMessageFromClient().equalsIgnoreCase("y"))
                        continue;
                    break;
                }

                connection = connections.get(i);
                usersTable += "\n\t" + (i + 1) + "\t\t" + connection.isWaiting + "\t" + connection.userName;
            }

            sendMessageToClient(usersTable + "\n\n");
        } catch (IOException e) {
            out.println(this + " : list users error");
        }
    }

    @Override
    public void run() {
        out.println(this + " : just started");

        try {
            userName = getMessageFromClient();
            out.println(this + " : logged in");
        } catch (IOException e) {
            disconnect();
        }

        menuMode();
        out.println(this + " : run method closed");
    }

    private void disconnect() {
        try {
            sendMessageToClient("DISCONNECTING");
            clientOutputStream.close();
            clientInputStream.close();
            client.close();
            connections.remove(this);
            this.interrupt();
        } catch (IOException e) {
            out.println(this + "disconnection error");
        }

        out.println(this + " : disconnect method closed");
    }

    public void sendMessageToClient(String message) throws IOException {
        clientOutputStream.writeUTF(message);
    }

    public String getMessageFromClient() throws IOException {
        return clientInputStream.readUTF();
    }

    public String toString() {
        return "[Connection " + userName + " " + this.client.getInetAddress().getHostAddress() + "]";
    }

    private Connection getConnectionByName(String name) {
        if (!name.equals(this.userName))
            for (Connection connection : connections) {
                if (name.equals(connection.userName) && connection.isWaiting)
                    return connection;
            }

        return null;
    }

    public class ConnectionConstructorException extends Exception {
        private String what;
        public ConnectionConstructorException(String what) {
            this.what = what;
        }
        public String toString() {
            return what;
        }
    }
}
