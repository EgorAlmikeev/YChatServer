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
                "\nLIST                 : see users online" +
                "\nCONNECT              : start chat" +
                "\nWAIT                 : wait for connection by any other user" +
                "\nBLACKLIST            : add/remove user in/from blacklist" +
                "\nEXIT                 : close program";

        connectionHelp = "\n===CONNECTION MODE===" +
                "\nIn this mode you can try to start chat with any online user, which is in waiting mode." +
                "\n" +
                "\nCOMMANDS :" +
                "\nHELP                     : see mode commands and describe" +
                "\nLIST                     : see users online" +
                "\nCONNECT TO [USERNAME]    : try to start chat with user with name USERNAME. Write it without brackets." +
                "\nBACK                     : return to main menu mode";

        waitForConnectionHelp = "\n===WAIT FOR CONNECTION MODE" +
                "In this mode you waiting for connections by another users," +
                "\nyou can accept or cancel their offers to chat." +
                "\n" +
                "\nCOMMANDS :" +
                "\nHELP         : see mode commands and describe" +
                "\nLIST         : " +
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

            out.println(this + " : in menu mode");

            String message;

            connection_life_cycle :
            while (!client.isClosed()) {

                sendMessageToClient("\n\n[MAIN MENU] set : ");
                message = getMessageFromClient();

                switch (message) {
                    case "HELP" : sendMessageToClient(menuHelp); break;
                    case "LIST" : listUsers(); break;
                    case "CONNECT" : connectionMode(); break;
                    case "WAIT" : waitForConnectionMode(); break;
                    case "BLACK LIST" : blacklistMode(); break;
                    case "EXIT" : disconnect(); break connection_life_cycle;
                    default : sendMessageToClient("[MAIN MENU] Unknown command \"" + message + "\"");
                }
            }
        } catch (IOException e) {
            disconnect();
        }


        out.println(this + " : menu method closed");
    }

    private void connectionMode() throws IOException {
        out.println(this + " : in connection mode");
        sendMessageToClient(connectionHelp);

        String message;
        Connection connection;

        while (true) {
            sendMessageToClient("\n[CONNECTION] set : ");
            message = getMessageFromClient();

            if (message.equals("LIST")) {
                listUsers();
            } else if (message.startsWith("CONNECT TO ")) {
                connection = getConnectionByName(message.substring("CONNECT TO ".length()));
                if (connection != null)
                    tryToStartChat(connection);
                else sendMessageToClient("no such user in waiting mode...");
            } else if (message.equals("BACK"))
                break;
            else if (message.equals("HELP"))
                sendMessageToClient(connectionHelp);
            else sendMessageToClient("[CONNECTION] Unknown command \"" + message + "\"");
        }

        out.println(this + " : connection mode closed");
    }

    private void waitForConnectionMode() {
        out.println(this + " : in waiting mode");
        isWaiting = !isWaiting;
    }

    private void tryToStartChat(Connection opponent) throws IOException {
        out.println(this + " : trying to start chat with " + opponent);

        opponent.isWaiting = false;
        opponent.sendMessageToClient(this.userName + " is trying to start chat with you\nStart chat? (y/n)");
        opponent.sendMessageToClient("\n[CONNECTION] set : ");

        String message;

        try {
            for (int i = 0; i < 10; i++) {
                this.sleep(1000);
                if (opponent.clientInputStream.available() != 0)
                {
                    if (opponent.getMessageFromClient().equalsIgnoreCase("y"))
                    {
                        chatMode(opponent);
                        break;
                    }
                    else {
                        sendMessageToClient(opponent + " don't want to chat with you...");
                        break;
                    }
                }
            }

            opponent.sendMessageToClient("\nRequest time out...");
            sendMessageToClient("\nRequest time out...");
            opponent.isWaiting = true;
        } catch (InterruptedException e) {
            disconnect();
        }
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

                connection = connections.get(i);

                if ((i + 1) % 2 == 0 && (i + 1) != connections.size()) {
                    usersTable += "\n\t" + (i + 1) + "\t\t" + connection.isWaiting + "\t" + connection.userName;
                    sendMessageToClient(usersTable + "\n\n");
                    sendMessageToClient("Printed " + (i + 1) + "/" + connections.size() + ". Print next? (y/n) : ");
                    if (getMessageFromClient().equalsIgnoreCase("y"))
                    {
                        usersTable = "";
                        continue;
                    }

                    break;
                }
                else if ((i + 1) == connections.size()) {
                    usersTable += "\n\t" + (i + 1) + "\t\t" + connection.isWaiting + "\t" + connection.userName;
                    sendMessageToClient(usersTable + "\n\n");
                }
                else usersTable += "\n\t" + (i + 1) + "\t\t" + connection.isWaiting + "\t" + connection.userName;
            }
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
