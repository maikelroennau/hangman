package client;

import java.util.Comparator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Maikel Maciel Rönnau
 */
public class User implements Comparator<User> {

    String userName;
    String userKey;
    int wins;
    int defeats;
    float winPercentage;

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
    }

    public int getDefeats() {
        return defeats;
    }

    public void updateDefeats() {
        this.defeats++;
    }

    public float getWinPercentage() {
        return winPercentage;
    }

    public float calculateWinPercentage() {
        if (wins + defeats == 0) {
            return 0;
        } else if (this.defeats == 0) {
            return 100;
        } else {
            return (wins * 100) / (wins + defeats);
        }
    }

    @Override
    public int compare(User o1, User o2) {
        if (o1.getWinPercentage() < o2.getWinPercentage()) {
            return -1;
        } else if (o1.getWinPercentage() > o2.getWinPercentage()) {
            return 1;
        }

        return 0;
    }
}
