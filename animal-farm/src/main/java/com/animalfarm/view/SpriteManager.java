package com.animalfarm.view;

import com.animalfarm.model.UnitType;
import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class SpriteManager {
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final Map<String, Image> playerCache = new HashMap<>();
    private static final int FRAMES_PER_ANIMATION = 4;

    // Load all sprites at startup
    public static void loadSprites() {
        // We iterate through all UnitTypes
        for (UnitType type : UnitType.values()) {
            String animalName = type.name().toLowerCase();
            
            // For both directions
            for (String direction : new String[]{"left", "right"}) {
                // For all 4 frames
                for (int i = 1; i <= FRAMES_PER_ANIMATION; i++) {
                    String path = String.format("/%s/%s_%d_%s_walk.png", animalName, direction, i, animalName);
                    try {
                        Image img = new Image(SpriteManager.class.getResourceAsStream(path));
                        imageCache.put(getCacheKey(animalName, direction, i), img);
                    } catch (Exception e) {
                        System.err.println("Failed to load sprite: " + path);
                    }
                }
            }
        }
        
        // Load player sprites
        // Player 1 (left side) uses folder player1
        // Player 2 (right side) uses folder player3
        int[] folderNums = {1, 3};
        String[] sides = {"left", "right"};
        
        for (int p = 0; p < 2; p++) {
            int folderNum = folderNums[p];
            String side = sides[p];
            int logicalPlayerNum = p + 1; // 1 or 2
            
            for (int i = 1; i <= 2; i++) {
                String path = String.format("/players/player%d/player%d-%s-%d.png", folderNum, folderNum, side, i);
                try {
                    Image img = new Image(SpriteManager.class.getResourceAsStream(path));
                    // Store them as player1_1, player1_2 and player2_1, player2_2
                    playerCache.put("player" + logicalPlayerNum + "_" + i, img);
                } catch (Exception e) {
                    System.err.println("Failed to load player sprite: " + path);
                }
            }
        }
    }

    public static Image getSprite(UnitType type, int direction, int frame) {
        String animalName = type.name().toLowerCase();
        String dirString = direction == 1 ? "left" : "right";
        return imageCache.get(getCacheKey(animalName, dirString, frame));
    }

    public static Image getPlayerSprite(int playerNum, int frame) {
        return playerCache.get("player" + playerNum + "_" + frame);
    }

    private static String getCacheKey(String animalName, String direction, int frame) {
        return animalName + "_" + direction + "_" + frame;
    }
}
