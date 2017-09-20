/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
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
        try {
            String request = this.receiver.nextLine();
            String command = "";

            if (request.split(" ").length > 0) {
                command = request.split(" ")[0];
            } else {
                command = request;
            }

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
                    getRank();
                    break;

                case "ENCERRARJOGO":
                    updateRankData(request);
                    break;

                default:
                    this.sender.println("Invalid Comand.");
                    this.sender.flush();
                    break;
            }

            System.out.println("Finished attending request.");
            System.out.println("Closing connection...");
        
            this.socket.close();
        } catch (IOException | NoSuchElementException e) {
            System.out.println("Client canceled before the server could attend.");
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
        try {
            if (Server.rank.isNull("ranking")) {
                this.sender.println(Server.rank.toString());
                this.sender.flush();
            } else {
                JSONObject rank = Server.rank;
                JSONArray rankList = new JSONArray(rank.get("ranking").toString());
                rank.remove("ranking");
                JSONObject record;
                
                for (int i = 0; i < rankList.length(); i++) {
                    record = rankList.getJSONObject(i);
                    record.remove("chave");
                }
                
                rank.put("ranking", rankList);
                
                this.sender.println(rank.toString());
                this.sender.flush();
            }
        } catch (JSONException ex) {
            System.out.println("Failed getting rank.");
        }
    }

    public void updateRankData(String request) {
        try {
            // ENCERRARJOGO maikel ronnau 0 1
            String[] data = request.split(" ");

            System.out.println(Server.rank.toString());

            int userIndex = getUserHistory(data[1], data[2]);
            JSONObject userData;

            if (userIndex == -1) {
                userData = new JSONObject();
                userData.put("usuario", data[1]);
                userData.put("chave", data[2]);
                userData.put("vitorias", Integer.parseInt(data[3]));
                userData.put("derrotas", Integer.parseInt(data[4]));
                userData.put("percentual", User.calculateWinPercentage(Integer.parseInt(data[3]), Integer.parseInt(data[4])));

                Server.rank.append("ranking", userData);
            } else {
                userData = new JSONObject(new JSONArray(Server.rank.getJSONArray("ranking").toString()).get(userIndex).toString());
                userData.put("vitorias", userData.getInt("vitorias") + Integer.parseInt(data[3]));
                userData.put("derrotas", userData.getInt("derrotas") + Integer.parseInt(data[4]));
                userData.put("percentual", User.calculateWinPercentage(userData.getInt("vitorias"), userData.getInt("derrotas")));

                Server.rank = new JSONObject(Server.rank.getJSONArray("ranking").remove(userIndex).toString());
                Server.rank.append("ranking", userData);
            }

            System.out.println(Server.rank.toString());
        } catch (JSONException ex) {
            System.out.println("Failed building user data JSON.");
            ex.printStackTrace();
        }
        
        
    }

    public int getUserHistory(String user, String key) {
        try {
            JSONArray ranking = new JSONArray(Server.rank.getJSONArray("ranking").toString());
            JSONObject userData = new JSONObject();

            for (int i = 0; i < ranking.length(); i++) {
                userData = new JSONObject(ranking.get(i).toString());

                try {
                    if (userData.getString("usuario").equals(user) && userData.getString("chave").equals(key)) {
                        return i;
                    }
                } catch (JSONException e) {
                    System.out.println("User not found. Creating new entry.");
                    userData = null;
                    i = -1;
                }
            }

            return -1;
        } catch (JSONException ex) {
            System.out.println("Failed checking user data.");
        }

        return -1;
    }
}
