package client;

/**
 *
 * @author Maikel Maciel Rönnau
 */
public class User {

    String userName;
    String userKey;
    int wins;
    int defeats;
    double winPercentage;

    public User(String userName, String userKey) {
        this.userName = userName;
        this.userKey = userKey;
        this.wins = 0;
        this.defeats = 0;
        this.winPercentage = 0;
    }

    public User(String userName, String userKey, int wins, int defeats) {
        this.userName = userName;
        this.userKey = userKey;
        this.wins = wins;
        this.defeats = defeats;
        this.winPercentage = calculateWinPercentage();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public int getWins() {
        return wins;
    }

    public void updateWins() {
        this.wins++;
        calculateWinPercentage();
    }

    public int getDefeats() {
        return defeats;
    }

    public void updateDefeats() {
        this.defeats++;
        calculateWinPercentage();
    }

    public double getWinPercentage() {
        return winPercentage;
    }

    public double calculateWinPercentage() {
        if (this.wins + defeats == 0) {
            return 0;
        } else if (this.defeats == 0) {
            return 100;
        } else {
            return Math.round(((float) this.wins * 100) / ((float) this.wins + (float) this.defeats) * 100.0) / 100.0;
        }
    }

    public static double calculateWinPercentage(int wins, int defeats) {
        if (wins + defeats == 0) {
            return 0;
        } else if (defeats == 0) {
            return 100;
        } else {
            return Math.round(((float) wins * 100) / ((float) wins + (float) defeats) * 100.0) / 100.0;
        }
    }

    public void resetScores() {
        this.wins = 0;
        this.defeats = 0;
    }
}
