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
public class Client {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String userName;
        String userKey;
        int games = 0;
        int wins = 0;
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter the Server IP address: ");
        String ipServer = scanner.nextLine();
        
        System.out.print("Enter the Server port: ");
        int portServer = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Testing connection to server...");
        if (Utils.testConnection(ipServer, portServer)) {
            System.out.println("Successfully contacted the server.");
        } else {
            System.out.println("Failed contacting the server.");
            System.out.println("Ending session...");
            System.exit(0);
        }
        
        System.out.print("Enter a username: ");
        userName = scanner.nextLine();
        
        System.out.print("Enter a user key: ");
        userKey = scanner.nextLine();
        
        Socket socket;
        Scanner receiver;
        PrintWriter sender;
        
        try {
            
            int option;
            
            do {
                
                System.out.println("Choose an option:");
                System.out.println("1 - Play");
                System.out.println("2 - Get ranking");
                System.out.println("3 - Exit");
                option = scanner.nextInt();

                socket = new Socket(ipServer, portServer);
                receiver = new Scanner(socket.getInputStream());
                sender = new PrintWriter(socket.getOutputStream());
                
                switch(option) {
                    case 1:
                        play(receiver, sender);
                        break;
                        
                    case 2:
                        showRank(receiver, sender);
                        break;
                        
                    case 3:
                        System.out.println("Ending session...");
                        System.exit(0);
                        break;
                        
                    default:
                        System.out.println("Invalid option.");
                    break;
                }
                
                if ((option == 1) || (option == 2)) {
                    socket = new Socket(ipServer, portServer);
                    System.out.println("Conectado...");

                    Scanner entrada = new Scanner(socket.getInputStream());
                    PrintWriter saida = new PrintWriter(socket.getOutputStream());

                    if (option == 1) {
                        System.out.print("Informe o caminho: ");
                        scanner.nextLine();
                        String path = scanner.nextLine();
                        saida.println("LISTAARQUIVOS " + path);
                    } else {
                        saida.println("GETFREESPACE");
                    }
                    saida.flush();
                    while (entrada.hasNextLine()) {
                        System.out.println(entrada.nextLine());
                    }

                    socket.close();
                    System.out.println("Conexao encerrada");
                }
            } while (option != 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void play(Scanner receiver, PrintWriter sender) {
        System.out.println("Requesting a word from the server...");
        sender.println("BUSCARPALAVRA");
        sender.flush();

        String response = receiver.nextLine();
        
        System.out.println(response);
        
        // Show the underscores referencing the word letters
        // Show the maximum erros alowed
        
        // If the user guess 4 letters, show a tip
        // If the user miss for letters, finish the game
        
        // If the user wins, update the score, aks if he wants to continue
        // If the user loses, update the score, aks if he wants to continue
        
        // If the user wants to continue, request a new word
        // If the user doesn't want to continue, push the results to the server
        // And show the ranking and then finish the session
    }
    
    public static void showRank(Scanner receiver, PrintWriter sender) {
        
    }
}
