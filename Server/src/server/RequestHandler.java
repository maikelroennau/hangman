package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Maikel Maciel Rönnau
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

            System.out.println("Request received: " + command);
            System.out.println("Atending request...");

            switch (command) {
                case "TESTARCONECCAO":
                    getConnectionStatus();
                    break;

                case "BUSCARPALAVRA":
                    try {
                        getWord();
                    } catch (JSONException ex) {
                        System.out.println("Failed creating JSON of word-tip.");
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
                JSONObject rank = new JSONObject(Server.rank.toString());
                JSONArray rankList = new JSONArray(rank.getJSONArray("ranking").toString());
                rank.remove("ranking");
                
                for (int i = 0; i < rankList.length(); i++) {
                    rankList.getJSONObject(i).remove("chave");
                    rank.append("ranking", rankList.getJSONObject(i));
                }
                
                String ordered = sortRank(rank);
                
                this.sender.println(ordered);
                this.sender.flush();
            }
        } catch (JSONException ex) {
            System.out.println("Failed getting rank.");
        }
    }
    
    public String sortRank(JSONObject ranking) {
        try {
            JSONArray usersRank = ranking.getJSONArray("ranking");
            ranking.remove("ranking");

            List<JSONObject> rankData = new ArrayList<>();

            for (int i = 0; i < usersRank.length(); i++) {
                rankData.add(usersRank.getJSONObject(i));
            }

            Collections.sort(rankData, new Comparator<JSONObject>() {
                private static final String KEY_NAME = "percentual";

                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String scoreA = new String();
                    String scoreB = new String();

                    try {
                        scoreA = String.valueOf(a.get(KEY_NAME));
                        scoreB = String.valueOf(b.get(KEY_NAME));
                    } catch (JSONException e) {
                        System.out.println("Failed sorting rank.");
                    }

                    return -scoreA.compareTo(scoreB);
                }
            });

            for (int i = 0; i < usersRank.length(); i++) {
                ranking.append("ranking", rankData.get(i));
            }
            
            return ranking.toString();
        } catch (JSONException ex) {
            System.out.println("No ranking information.\n");
        }
        
        return "{}";
    }

    public void updateRankData(String request) {
        JSONObject backup = new JSONObject(Server.rank);

        try {
            String[] data = request.split(" ");

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
                userData = Server.rank.getJSONArray("ranking").getJSONObject(userIndex);

                userData.put("vitorias", userData.getInt("vitorias") + Integer.parseInt(data[3]));
                userData.put("derrotas", userData.getInt("derrotas") + Integer.parseInt(data[4]));
                userData.put("percentual", User.calculateWinPercentage(userData.getInt("vitorias"), userData.getInt("derrotas")));
            }

            JSONObject user = new JSONObject(userData.toString());
            userData = null;
            user.remove("chave");

            saveToDisk();

            this.sender.println(user.toString());
            user = null;
            this.sender.flush();
        } catch (JSONException ex) {
            System.out.println("Failed building user data JSON.");
            System.out.println("Restoring backup...");
            Server.rank = backup;
            saveToDisk();
            System.out.println("Backup restored. Last entry is lost.");
        }
    }

    public void saveToDisk() {
        try {
            PrintWriter writer = new PrintWriter("rank.json", "UTF-8");
            writer.println(Server.rank.toString());
            writer.flush();
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            System.out.println("Failed updating rank file on disk.");
        }
    }

    public int getUserHistory(String user, String key) {
        try {
            JSONArray ranking = Server.rank.getJSONArray("ranking");
            JSONObject userData;

            for (int i = 0; i < ranking.length(); i++) {
                userData = new JSONObject(ranking.get(i).toString());

                try {
                    if (userData.getString("usuario").equals(user) && userData.getString("chave").equals(key)) {
                        return i;
                    }
                } catch (JSONException e) {
                    System.out.println("User not found. Creating new entry.");
                    userData = null;
                    return -1;
                }
            }

            return -1;
        } catch (JSONException ex) {
            System.out.println("The rank does not contain any entry.");
            return -1;
        }
    }
}
