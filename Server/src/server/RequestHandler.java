/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
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

        System.out.println("Request received: " + request);
        System.out.println("Atending request...");

        switch (request) {
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

            case "-":
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
}
