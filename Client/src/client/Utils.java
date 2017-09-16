/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author 110453310
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
}
