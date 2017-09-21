package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class Utils {

    public static boolean testConnection(String ipServer, int portServer) {
        try {
            Socket socket = new Socket(ipServer, portServer);

            Scanner receiver = new Scanner(socket.getInputStream());
            PrintWriter sender = new PrintWriter(socket.getOutputStream());

            sender.println("TESTARCONECCAO");
            sender.flush();

            return receiver.nextLine().equals(("CONECCAOOK"));
        } catch (IOException ex) {
            return false;
        }
    }

    public static Socket getSocketConnection(String ipServer, int portServer) {
        try {
            if (testConnection(ipServer, portServer)) {
                Socket socket = new Socket(ipServer, portServer);
                return socket;
            }
        } catch (IOException ex) {
            System.out.println("Failed connecting to the server.");
        }

        return null;
    }
}
