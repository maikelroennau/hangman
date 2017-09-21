package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class Client {

    private static String userName;
    private static String userKey;
    private static User user;

    private static String ipServer;
    private static int portServer;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the Server IP address: ");
        ipServer = scanner.nextLine();

        System.out.print("Enter the Server port: ");
        portServer = scanner.nextInt();
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
        System.out.println("");

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

                socket = Utils.getSocketConnection(ipServer, portServer);
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
                        System.out.println("Ending session...");
                        socket.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("\nInvalid option.\n");
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

            printSlots(letters, corrects);

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
                hits += Collections.frequency(letters, guess);
            } else {
                wrongs.add(guess);
                errors++;
            }

            if (hits == wordSize) {
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
        try {
            Socket socket = Utils.getSocketConnection(ipServer, portServer);
            Scanner quickReceiver = new Scanner(socket.getInputStream());
            PrintWriter quickSender = new PrintWriter(socket.getOutputStream());

            pushResults(quickReceiver, quickSender, user, true);
        } catch (IOException ex) {
            System.out.println("\nFailed pushing user data to the server.");
            System.out.println("The results will not be the most recent ones.");
        }

        System.out.println("Requesting rank information...");
        sender.println("BUSCARRANKING");
        sender.flush();

        JSONObject response = null;

        try {
            response = new JSONObject(receiver.nextLine());

            JSONArray usersRank = response.getJSONArray("ranking");

            System.out.println("\nRanking:\n");
            for (int i = 0; i < usersRank.length(); i++) {
                System.out.println("User...........: " + usersRank.getJSONObject(i).getString("usuario"));
                System.out.println("Victories......: " + usersRank.getJSONObject(i).getInt("vitorias"));
                System.out.println("Defeats........: " + usersRank.getJSONObject(i).getInt("derrotas"));
                System.out.println("Win percentage.: " + usersRank.getJSONObject(i).getDouble("percentual") + "\n");
                usersRank.getJSONObject(i).getString("usuario");
            }
        } catch (JSONException ex) {
            System.out.println("\nFailed to parse rank.");
        }
    }

    public static void pushResults(Scanner receiver, PrintWriter sender, User user) {
        System.out.println("\nUpdating scores to server...");

        String command = "ENCERRARJOGO";
        command += " " + user.getUserName();
        command += " " + user.getUserKey();
        command += " " + user.getWins();
        command += " " + user.getDefeats();

        sender.println(command);
        sender.flush();

        System.out.println("User score updated.");

        JSONObject response = null;

        try {
            response = new JSONObject(receiver.nextLine());

            System.out.println("\nUser data:");
            System.out.println("Username.......: " + response.getString("usuario"));
            System.out.println("Victories......: " + response.getInt("vitorias"));
            System.out.println("Defeats........: " + response.getInt("derrotas"));
            System.out.println("Win percentage.: " + response.getDouble("percentual") + "\n");
        } catch (JSONException ex) {
            System.out.println("\nFailed to parse user data back.");
        }
    }

    public static void pushResults(Scanner receiver, PrintWriter sender, User user, boolean silent) {
        System.out.println("\nUpdating scores to server...");

        String command = "ENCERRARJOGO";
        command += " " + user.getUserName();
        command += " " + user.getUserKey();
        command += " " + user.getWins();
        command += " " + user.getDefeats();

        sender.println(command);
        sender.flush();

        user.resetScores();
        receiver.nextLine();
    }
}
