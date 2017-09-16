/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author 110453310
 */
public class Server {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ServerSocket server = null;
        int port = 4321;
        
        try {
            System.out.println("Starting sever under the port " + port);
            server = new ServerSocket(port);

            while (true) {
                System.out.println("Waiting for connections...");
                Socket client = server.accept();

                System.out.println("Connection stablished.");
                System.out.println("Attending requisitions.");
                
                RequestHandler request = new RequestHandler(client);
                request.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
