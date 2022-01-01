package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.world.Avatar;
import pepse.world.Block;
import pepse.world.Terrain;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Tree;

import java.awt.*;
import java.util.Random;

/**
 * Represents PepseGameManeger instance.
 * @author Ilan Vys, Yonatan Chocron
 */
public class PepseGameManager extends GameManager {
    // CONSTANTS
    private static final int OPTIONAL_SEEDS = 1000;
    private static final float DAY_CYCLE_LENGTH = 30f;
    private static final Color SUN_COLOR = new Color(255, 255, 0, 20);
    private static final int WORLD_BUFFER = 5*Block.SIZE;

    // LAYERS
    private static final int SKY_LAYER = Layer.BACKGROUND;
    private static final int SUN_HALO_LAYER = Layer.BACKGROUND + 5;
    private static final int SUN_LAYER = Layer.BACKGROUND + 10;
    private static final int UPPER_TERRAIN_LAYER = Layer.DEFAULT;
    private static final int LOWER_TERRAIN_LAYER = Layer.DEFAULT - 10;
    private static final int AVATAR_LAYER = Layer.DEFAULT + 10;
    private static final int NIGHT_LAYER = Layer.FOREGROUND;
    private static final int LEAVES_LAYER = Layer.DEFAULT + 5;
    private static final int ROOT_LAYER = Layer.DEFAULT + 4;

    // TAGS  todo erase what were not using. notice its only a name consists with tag name given in class
    private static final String SKY_TAG = "sky";
    private static final String SUN_HALO_TAG = "sun halo";
    private static final String SUN_TAG = "sun";
    private static final String UPPER_TERRAIN_TAG = "upper terrain";
    private static final String LOWER_TERRAIN_TAG = "lower terrain";
    private static final String AVATAR_TAG = "avatar";
    private static final String NIGHT_TAG = "night";
    private static final String LEAF_BLOCK_TAG = "leafBlock";
    private static final String ROOT_TAG = "rootBlock"; // tree root

    // FIELDS
    private Vector2 windowDimensions;
    private ImageReader imageReader;
    private WindowController windowController;
    private UserInputListener inputListener;
    private SoundReader soundReader;
    private Avatar avatar;
    private Terrain terrain;
    private Tree tree;
    private Camera camera;
    private int rightWorldPointer;
    private int leftWorldPointer;

    @Override
    public void initializeGame(ImageReader imageReader,
                               SoundReader soundReader,
                               UserInputListener inputListener,
                               WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        this.windowDimensions = windowController.getWindowDimensions();
        this.imageReader = imageReader;
        this.windowController = windowController;
        this.inputListener = inputListener;
        this.soundReader = soundReader;

        // seed
        Random random = new Random();
        int seed = random.nextInt(OPTIONAL_SEEDS);

        // create sky
        pepse.world.Sky.create(gameObjects(),
                windowController.getWindowDimensions(),
                SKY_LAYER);

        // create terrain
        this.terrain = new Terrain(gameObjects(),
                UPPER_TERRAIN_LAYER,
                windowController.getWindowDimensions(),
                seed);

        // create night/day
        Night.create(
                this.gameObjects(),
                NIGHT_LAYER,
                this.windowDimensions,
                DAY_CYCLE_LENGTH);

        // crate sun
        GameObject sun = Sun.create(
                this.gameObjects(),
                SUN_LAYER,
                this.windowDimensions,
                DAY_CYCLE_LENGTH);

        // create sun halo
        GameObject sunHalo = SunHalo.create(
                this.gameObjects(),
                SUN_HALO_LAYER,
                sun,
                SUN_COLOR);

        // create trees
        this.tree = new Tree(
                this.gameObjects(),
                terrain,
                seed,
                ROOT_LAYER,
                LEAVES_LAYER,
                ROOT_TAG,
                LEAF_BLOCK_TAG,
                UPPER_TERRAIN_TAG);

        // create avatar & camera
        Vector2 initPos = windowDimensions.mult(0.5f); // middle of screen
        this.avatar = Avatar.create(
                gameObjects(),
                AVATAR_LAYER,
                initPos,
                inputListener,
                imageReader);
        this.camera = new Camera(
                avatar,
                Vector2.ZERO,
                windowDimensions,
                windowDimensions);
        setCamera(camera);

        // build initial world
        buildInitialWorld();

        // Differentiating layers
        gameObjects().layers().shouldLayersCollide(
                LEAVES_LAYER,
                UPPER_TERRAIN_LAYER,
                true);
        gameObjects().layers().shouldLayersCollide(
                AVATAR_LAYER,
                UPPER_TERRAIN_LAYER,
                true);


    }

    /**
     * Builds initial world according to window dimensions
     */
    private void buildInitialWorld() {
        float rightScreenX = camera.screenToWorldCoords(windowDimensions).x();
        float leftScreenX = camera.screenToWorldCoords(
                windowDimensions).x() - windowDimensions.x();

        leftWorldPointer = normalizeToBlockSize(leftScreenX) - WORLD_BUFFER;
        rightWorldPointer = normalizeToBlockSize(rightScreenX) + WORLD_BUFFER;

        buildWorld(leftWorldPointer, rightWorldPointer);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        float rightScreenX = camera.screenToWorldCoords(windowDimensions).x();
        float leftScreenX = camera.screenToWorldCoords(
                windowDimensions).x() - windowDimensions.x();

        if (rightScreenX >= rightWorldPointer){
            extendWorldToRight(rightWorldPointer, rightScreenX + WORLD_BUFFER);
        }

        if (leftScreenX <= leftWorldPointer){
            extendWorldToLeft(leftScreenX, leftWorldPointer - WORLD_BUFFER);
        }
    }

    /**
     * Extends world to right AND removing redundant world from left
     * @param start X coord from which world will be built
     * @param end max X coord till which world will be built
     */
    private void extendWorldToRight(float start, float end){
        int normalizedStartX = normalizeToBlockSize(start);
        int normalizedEndX = normalizeToBlockSize(end);

        // build world to right
        buildWorld(normalizedStartX, normalizedEndX);

        // remove world from left
        for (GameObject obj : gameObjects()){
            if (obj.getCenter().x() < leftWorldPointer){
                removeObjectFromItsLayer(obj);
            }
        }

        // update both pointers
        rightWorldPointer = normalizedEndX;
        leftWorldPointer += (normalizedEndX - normalizedStartX);
    }

    /**
     * Extends world to left AND removing redundant world from right
     * @param start X coord from which world will be built
     * @param end X coord till which world will be built
     */

    private void extendWorldToLeft(float start, float end){
        int normalizedStart = normalizeToBlockSize(start);
        int normalizedEnd = normalizeToBlockSize(end);

        // build world to left (according to avatar walking direction)
        buildWorld(normalizedStart, normalizedEnd);

        // remove world from right
        for (GameObject obj : gameObjects()){
            if (obj.getCenter().x() > rightWorldPointer){
                removeObjectFromItsLayer(obj);
            }
        }

        // update pointers
        leftWorldPointer = normalizedEnd;
        rightWorldPointer -= (normalizedStart - normalizedEnd);
    }

    /**
     * Builds world
     * @param minX X coord from which world will be built
     * @param maxX X coord till which world will be built
     */
    private void buildWorld(int minX, int maxX){
        terrain.createInRange(minX, maxX);
        tree.createInRange(minX, maxX);
    }

    /**
     * Will remove redundant objects from their layer (only removes terrain blocks, trees and leaves)
     * @param obj GameObject to remove
     */
    private void removeObjectFromItsLayer(GameObject obj){
        switch (obj.getTag()) {
            case UPPER_TERRAIN_TAG -> gameObjects().removeGameObject(obj, UPPER_TERRAIN_LAYER);
            case LOWER_TERRAIN_TAG -> gameObjects().removeGameObject(obj, LOWER_TERRAIN_LAYER);
            case ROOT_TAG -> gameObjects().removeGameObject(obj, ROOT_LAYER);
            case LEAF_BLOCK_TAG -> gameObjects().removeGameObject(obj, LEAVES_LAYER);
            }
        }


    private int normalizeToBlockSize(float x){
        return (int) (Math.floor(x / Block.SIZE) * Block.SIZE);
    }

    public static void main(String[] args) {
        new PepseGameManager().run();
    }
}
