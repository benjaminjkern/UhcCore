package com.gmail.val59000mc.threads;

import java.io.PrintStream;
import java.util.Scanner;

import com.gmail.val59000mc.game.GameManager;

import org.bukkit.Bukkit;

public class ReceiveScoresThread implements Runnable {
    public void run() {
        try (Scanner in = new Scanner(GameManager.getGameManager().getLobbySocket().getInputStream())) {
            while (in.hasNextLine()) {
                String input = in.nextLine();
                if (input.startsWith("RATING:")) {
                    // update player rating
                    String[] inputBits = input.split(":");
                    if (inputBits.length == 3) {
                        try {
                            GameManager.getGameManager().getPlayersManager().getScoreKeeper().setScore(inputBits[1],
                                    Double.parseDouble(inputBits[2]));
                        } catch (Exception e) {
                            GameManager.getGameManager().getPlayersManager().getScoreKeeper().setScore(inputBits[1],
                                    50);
                        }
                        continue;
                    }
                }

                Bukkit.getLogger().info(input + " was not recognized!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
