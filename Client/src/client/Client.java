/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author 110453310
 */
public class Client {

    private static String userName;
    private static String userKey;
    private static User user;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

//        String userName;
//        String userKey;
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the Server IP address: ");
//        String ipServer = scanner.nextLine();
        String ipServer = "localhost";

        System.out.print("Enter the Server port: ");
//        int portServer = scanner.nextInt();
//        scanner.nextLine();
        int portServer = 8765;

        System.out.println("Testing connection to server...");
        if (Utils.testConnection(ipServer, portServer)) {
            System.out.println("Successfully contacted the server.");
        } else {
            System.out.println("Failed contacting the server.");
            System.out.println("Ending session...");
            System.exit(0);
        }

        System.out.print("Enter a username: ");
//        userName = scanner.nextLine();
        userName = "maikel";

        System.out.print("Enter a user key: ");
//        userKey = scanner.nextLine();
        userKey = "ronnau";

        user = new User(userName, userKey);

        Socket socket;
        Scanner receiver;
        PrintWriter sender;

        try {

            int option;

            do {
                System.out.println("Choose an option:");
                System.out.println("1 - Play");
                System.out.println("2 - Show ranking");
                System.out.println("3 - Exit");
                System.out.print("\nOption: ");
                option = scanner.nextInt();

                socket = new Socket(ipServer, portServer);
                receiver = new Scanner(socket.getInputStream());
                sender = new PrintWriter(socket.getOutputStream());

                switch (option) {
                    case 1:
                        play(receiver, sender);
                        break;

                    case 2:
                        showRank(receiver, sender);
                        break;

                    case 3:
                        pushResults(receiver, sender, user);
                        System.out.println("\nEnding session...");
                        socket.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("\nInvalid option.");
                        break;
                }
            } while (option != 3);
        } catch (IOException e) {
            System.out.println("Failed connecting the server.");
        }
    }

    public static void play(Scanner receiver, PrintWriter sender) {
        System.out.println("\nRequesting a word from the server...");
        sender.println("BUSCARPALAVRA");
        sender.flush();

        JSONObject response = null;
        String word = "";
        String tip = "";

        try {
            response = new JSONObject(receiver.nextLine());
            word = response.getString("palavra");
            tip = response.getString("dica");
        } catch (JSONException ex) {
            System.out.println("Failed to parse word-tip.");
        }

        ArrayList<Character> letters = new ArrayList<>();
        for (Character letter : word.toCharArray()) {
            letters.add(letter);
        }

        int maxErrors = 4;
        int errors = 0;
        int hits = 0;
        int wordSize = letters.size();
        int total = 0;

        ArrayList<Character> corrects = new ArrayList<>();
        ArrayList<Character> wrongs = new ArrayList<>();

        Scanner scanner = new Scanner(System.in).useDelimiter("'");
        Character guess;

        boolean status = false;

        System.out.println("Starting game...");

        for (int i = 1; errors <= maxErrors; i++) {
            System.out.println("\n---------------------------------------------");
            System.out.print("Round " + i + " - Hits: " + hits + " | Errors: " + errors);
            System.out.println("\tCorrects: " + Arrays.toString(corrects.toArray()).replace("[", "").replace("]", ""));
            System.out.println("\t\t\t\tWrongs..: " + Arrays.deepToString(wrongs.toArray()).replace("[", "").replace("]", ""));

            total = printSlots(letters, corrects);

            if (hits >= (int) wordSize / 2) {
                System.out.println("\nTip: " + tip);
            }

            System.out.print("\nGuess: ");
            guess = scanner.nextLine().charAt(0);

            if (corrects.contains(guess) | wrongs.contains(guess)) {
                System.out.println("You already used this word.");
                continue;
            }

            if (letters.contains(guess)) {
                corrects.add(guess);
                hits++;
            } else {
                wrongs.add(guess);
                errors++;
            }

            if (total == wordSize) {
                status = true;
                break;
            }
        }

        if (status) {
            System.out.println("\nCongratulations!");
            System.out.println("The word was: " + word + "\n");
            user.updateWins();
        } else {
            System.out.println("\nGame over!");
            System.out.println("The word was: " + word + "\n");
            user.updateDefeats();
        }
    }

    public static int printSlots(ArrayList<Character> letters, ArrayList<Character> corrects) {
        String line = "";
        int total = 0;

        for (Character letter : letters) {
            if (corrects.contains(letter)) {
                line += letter + " ";
                total++;
            } else {
                line += "_ ";
            }
        }

        System.out.println("\nWord: " + line);

        return total + 1;
    }

    public static void showRank(Scanner receiver, PrintWriter sender) {
        System.out.println("\nRequesting rank information...");
        sender.println("BUSCARRANKING");
        sender.flush();

        JSONObject response = null;

        try {
            response = new JSONObject(receiver.nextLine());
            printOrderedRank(response);
        } catch (JSONException ex) {
            System.out.println("\nFailed to parse rank.");
        }
    }

    public static void printOrderedRank(JSONObject ranking) {
        try {
            JSONArray usersRank = ranking.getJSONArray("ranking");

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

            System.out.println("\nRanking:\n");
            for (int i = 0; i < usersRank.length(); i++) {
                System.out.println("User...........: " + rankData.get(i).getString("usuario"));
                System.out.println("Victories......: " + rankData.get(i).getInt("vitorias"));
                System.out.println("Defeats........: " + rankData.get(i).getInt("derrotas"));
                System.out.println("Win percentage.: " + rankData.get(i).getDouble("percentual") + "\n");
            }
        } catch (JSONException ex) {
            System.out.println("No ranking information.\n");
        }
    }

    public static void pushResults(Scanner receiver, PrintWriter sender, User user) {

        if (user.getWins() + user.getDefeats() != 0) {
            System.out.println("\nUpdating scores to server...");

            String command = "ENCERRARJOGO";
            command += " " + user.getUserName();
            command += " " + user.getUserKey();
            command += " " + user.getWins();
            command += " " + user.getDefeats();
            
            System.out.println(command);
            sender.println(command);
            sender.flush();

            System.out.println("User score updated.");
        }
    }
}
