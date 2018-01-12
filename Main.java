import java.io.IOException;
import java.util.Scanner;
import static java.lang.System.out;

public class Main {

    public static Scanner scanner = new Scanner(System.in);
    public static ConnectionManager connectionManager = new ConnectionManager(1111, 10);


    public static void main(String [] args) {

        connectionManager.start();

        String admin_message = "";

        try {
            while (true) {
                out.println("[Main] : scanner reads now...");
                admin_message = scanner.nextLine();
                out.println("[Main] : admin said " + admin_message);

                if (admin_message.equals("stop"))
                    break;
            }


                connectionManager.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}