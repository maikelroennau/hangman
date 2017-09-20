/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author 110453310
 */
public class Server {

    static ArrayList<String> words = new ArrayList<>();
    static HashMap<String, String> tips = new HashMap<>();

    static JSONObject rank = new JSONObject();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        loadWordList();
        loadRank();

        ServerSocket server;
        int port = 8765;

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

    public static void loadWordList() {
        System.out.println("Loading word list...");

        try {
            String filePath = "ListaPalavras.txt";
            List<String> file = Files.readAllLines(Paths.get(filePath));
            Collections.shuffle(file);

            for (String word : file) {
                String readWord = word.split("\\|")[0].toLowerCase();
                String tip = word.split("\\|")[1];

                words.add(readWord);
                tips.put(readWord, tip);
            }

            System.out.println("Word list sucessfull loaded.");
        } catch (IOException ex) {
            System.out.println("Failed loading word list. Shutting down...");
            System.exit(0);
        }
    }

    public static void loadRank() {
        System.out.println("Reading rank file...");
        try {
            String rankFile = "rank.json";

            JSONObject rankRead = new JSONObject(new Scanner(new File(rankFile)).useDelimiter("\\Z").next());
            rank = rankRead;

            System.out.println("Rank file sucessfull loaded.");
        } catch (FileNotFoundException | JSONException ex) {
            System.out.println("Failed loading rank file.");
        }
    }
}
