package com.animalfarm.view;

import com.animalfarm.model.UnitType;
import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class SpriteManager {
    private static final Map<String, Image> imageCache = new HashMap<>();
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
    }

    public static Image getSprite(UnitType type, int direction, int frame) {
        String animalName = type.name().toLowerCase();
        // Fix for sprites walking backwards: 
        // We swap the 'right' and 'left' strings here so units face the correct side.
        String dirString = direction == 1 ? "left" : "right";
        return imageCache.get(getCacheKey(animalName, dirString, frame));
    }

    private static String getCacheKey(String animalName, String direction, int frame) {
        return animalName + "_" + direction + "_" + frame;
    }
}
