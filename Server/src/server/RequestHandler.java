/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author 110453310
 */
public class RequestHandler extends Thread {

    private Socket socket;
    private Scanner receiver;
    private PrintWriter sender;

    RequestHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.receiver = new Scanner(this.socket.getInputStream());
        this.sender = new PrintWriter(this.socket.getOutputStream());
    }

    public void run() {

        String request = this.receiver.nextLine();
        String command = request.split(" ")[0];

        System.out.println("Request received: " + request);
        System.out.println("Atending request...");

        switch (command) {
            case "TESTARCONECCAO":
                getConnectionStatus();
                break;

            case "BUSCARPALAVRA": {
                try {
                    getWord();
                } catch (JSONException ex) {
                    System.out.println("Failed creating JSON of word-tip.");
                }
            }
            break;
            
            case "BUSCARRANKING":
                
                break;

            case "ENCERRARJOGO":
                updateRankFile(request);
                break;

            default:
                this.sender.println("Invalid Comand.");
                this.sender.flush();
                break;
        }

        System.out.println("Finished attending request.");
        System.out.println("Closing connection...");
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Connection closed.");

    }

    public void getConnectionStatus() {
        this.sender.println("CONECCAOOK");
        this.sender.flush();
    }

    public void getWord() throws JSONException {
        System.out.println("Selecting random word...");
        int randon = ThreadLocalRandom.current().nextInt(0, Server.words.size());

        String word = Server.words.get(randon);
        String tip = Server.tips.get(word);

        JSONObject json = new JSONObject();
        json.put("palavra", word);
        json.put("dica", tip);

        this.sender.println(json);
        this.sender.flush();
    }
    
    public void getRank() {
        // Reads the raking file
        // Converts it to JSON
        // Send back to the client
    }
    
    public void updateRankFile(String request) {
        try {
            // ENCERRARJOGO tales chavetales 10 2

            String[] data = request.split(" ");

//            System.out.println(request);
//            System.out.println(Arrays.toString(data));

            JSONObject userData = new JSONObject();
            userData.put("usuario", data[1]);
            
            getUserHistory(data[1], data[2]);
        } catch (JSONException ex) {
            System.out.println("Failed building user data JSON.");
        }
    }
    
    public JSONObject getUserHistory(String user, String key) {
        try {
            System.out.println(Server.rank.toString());
            System.out.println(Server.rank.getJSONObject("ranking").toString());
            
            
        } catch (JSONException ex) {
            System.out.println("Failed checking user data.");
        }
        
        return new JSONObject();
    }
}
