import static java.lang.System.out;

import java.io.*;
import java.util.ArrayList;
import java.net.Socket;
import java.util.TreeSet;

public class Connection extends Thread {

    public Socket client;
    public String userName = "USER_NAME";

    private DataOutputStream clientOutputStream;
    private DataInputStream clientInputStream;
    private ArrayList<Connection> connections;

    private ArrayList<String> chatCommands;
    private ArrayList<String> menuCommands;

    public Connection(Socket client, ArrayList<Connection> connections) throws ConnectionConstructorException {
        this.client = client;
        this.connections = connections;
        try {
            clientOutputStream = new DataOutputStream(client.getOutputStream());
            clientInputStream = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            throw new ConnectionConstructorException(this + " : can't get I/O stream");
        }

        createChatCommands();
        createMenuCommands();

        out.println(this + " : successfully created");
    }

    private void createChatCommands() {
        chatCommands = new ArrayList<>();

        chatCommands.add("ADDUSER");
        chatCommands.add("REMOVEUSER");
        chatCommands.add("STOPCHAT");
        chatCommands.add("LEAVECHAT");
    }

    private void createMenuCommands() {
        menuCommands = new ArrayList<>();

        menuCommands.add("LIST");
        menuCommands.add("STARTCHAT");
        menuCommands.add("EXIT");
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

        MenuAlgorithm();
    }

    private void disconnect() {
        try {
            clientOutputStream.close();
            clientInputStream.close();
            client.close();
            connections.remove(this);
        } catch (IOException e) {
            out.println(this + "disconnection error");
        }
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

    private void MenuAlgorithm() {

        out.println(this + " : menu algorithm started");

        String message;
        String menuCommands = "\nHELP : get all commands" +
                "\nLIST : see users online" +
                "\nCHAT : start chat" +
                "\nEXIT : disconnect";

        connection_life_cycle :
        while (!client.isClosed()) {
            try {
                sendMessageToClient("Write \"HELP\" to get commands...");
                message = getMessageFromClient();

                switch (message) {
                    case "LIST" : sendMessageToClient(connections.toString()); break;
                    case "HELP" : sendMessageToClient("Commands :" + menuCommands); break;
                    case "CHAT" : sendMessageToClient("START CHAT"); ChatAlgorithm(); break;
                    case "EXIT" : sendMessageToClient("Disconnecting..."); disconnect(); break connection_life_cycle;
                    default : sendMessageToClient("Unknown command \"" + message + "\"");
                }
            } catch (IOException e) {
                disconnect();
            }
        }
        out.println(this + " : successfully disconnected");
    }

    private void ChatAlgorithm() throws IOException {

//        ArrayList<Connection> chatUsers = new ArrayList<>();
//        chatUsers.add(this);
//
//        String message;
//
//        while (true) {
//            for (Connection user : chatUsers)
//                if (user.clientInputStream.ready())
//                {
//                    message = user.getMessageFromClient();
//                    if (!isChatCommand(message))
//                        for (Connection anotherUser : chatUsers)
//                            anotherUser.sendMessageToClient("[" + user.userName + "] : " +  message);
//                    else {
//
//                        switch (chatCommands.indexOf(message)) {
//                            case 0 : addUserInChat(message.substring(chatCommands.get(0).length()), chatUsers); break;
//                            case 1 : removeUserFromChat(message.substring(chatCommands.get(1).length()), chatUsers); break;
//                        }
//
//                    }
//                }
//        }

    }

    private boolean isChatCommand(String string) {
        for (String command : chatCommands) {
            if (string.startsWith(command) || string.equals(command))
                return true;
        }

        return false;
    }

    private void addUserInChat(String name, ArrayList<Connection> users) {

        for (Connection connection : connections) {
            if (connection.userName.equals(name))
            {
                users.add(connection);
                break;
            }
        }
    }

    private void removeUserFromChat(String name, ArrayList<Connection> users) {

        for (Connection connection : connections) {
            if (connection.userName.equals(name))
            {
                users.remove(connection);
                break;
            }
        }
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
