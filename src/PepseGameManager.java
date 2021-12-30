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
import java.util.Objects;
import java.util.Random;

public class PepseGameManager extends GameManager {

    // CONSTANTS
    private static final int OPTIONAL_SEEDS = 100;
    private static final float DAY_CYCLE_LENGTH = 30f;
    private static final Color SUN_COLOR = new Color(255, 255, 0, 20);

    // LAYERS
    private static final int SKY_LAYER = Layer.BACKGROUND;
    private static final int SUN_HALO_LAYER = Layer.BACKGROUND + 5;
    private static final int SUN_LAYER = Layer.BACKGROUND + 10;
    private static final int UPPER_TERRAIN_LAYER = Layer.DEFAULT;
    private static final int LOWER_TERRAIN_LAYER = Layer.DEFAULT - 10; // todo how can we pass it to Terrain?
    private static final int AVATAR_LAYER = Layer.DEFAULT;
    private static final int NIGHT_LAYER = Layer.FOREGROUND;
    public int LEAVES_LAYER = Layer.DEFAULT - 4;
    public int ROOT_LAYER = Layer.DEFAULT - 5;

    // TAGS  todo erase what were not using. notice its only a name consists with tag name given in class
    private static final String SKY_TAG = "sky";
    private static final String SUN_HALO_TAG = "sun halo";
    private static final String SUN_TAG = "sun";
    private static final String UPPER_TERRAIN_TAG = "upper terrain";
    private static final String LOWER_TERRAIN_TAG = "lower terrain"; // todo how can we pass it to Terrain?
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
    private int worldBuiltPointer = 0;  // points which part (x axis) of the wold is currently built

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
        pepse.world.Sky.create(gameObjects(), windowController.getWindowDimensions(), SKY_LAYER);

        // create terrain
        this.terrain = new Terrain(gameObjects(), UPPER_TERRAIN_LAYER,
                windowController.getWindowDimensions(), 20); // todo use real seed

        // create night/day
        Night.create(this.gameObjects(), NIGHT_LAYER, this.windowDimensions, DAY_CYCLE_LENGTH);

        // crate sun
        GameObject sun = Sun.create(this.gameObjects(), SUN_LAYER, this.windowDimensions, DAY_CYCLE_LENGTH);

        // create sun halo
        GameObject sunHalo = SunHalo.create(this.gameObjects(), SUN_HALO_LAYER, sun, SUN_COLOR);

        // create trees
        this.tree = new Tree(this.gameObjects(), this.windowDimensions, terrain, seed, ROOT_LAYER, LEAVES_LAYER);

        // create avatar & set camera
        Vector2 initPos = windowDimensions.mult(0.5f); // middle of screen
        this.avatar = Avatar.create(gameObjects(), AVATAR_LAYER, initPos, inputListener, imageReader);
        setCamera(new Camera(avatar, Vector2.ZERO, windowDimensions, windowDimensions));

        // build world
        int windowDimX = (int) windowDimensions.x();
        terrain.createInRange(-windowDimX, 2*windowDimX);
        tree.createInRange(0, windowDimX);
        worldBuiltPointer = (int) avatar.getCenter().x();


        // Differentiating layers
        gameObjects().layers().shouldLayersCollide(LEAVES_LAYER, UPPER_TERRAIN_LAYER, true);

    }


    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        int avatarXPos = normalizeToBlockSize(avatar.getCenter().x());
        int windowXDim = normalizeToBlockSize(windowDimensions.x());



        if (worldBuiltPointer + windowXDim / 2 < avatarXPos) {  // avatar had walked right enough
            extendWorldToRight(avatarXPos, windowXDim);
        }
        if (avatarXPos < worldBuiltPointer - windowXDim / 2) {  // avatar had walked left enough
            extendWorldToLeft(avatarXPos, windowXDim);
        }

    }

    private void removeObjectFromItsLayer(GameObject obj){
        switch (obj.getTag()) {
            case UPPER_TERRAIN_TAG -> gameObjects().removeGameObject(obj, UPPER_TERRAIN_LAYER);
            case LOWER_TERRAIN_TAG -> gameObjects().removeGameObject(obj, LOWER_TERRAIN_LAYER);
            case ROOT_TAG -> gameObjects().removeGameObject(obj, ROOT_LAYER);
            case LEAF_BLOCK_TAG -> gameObjects().removeGameObject(obj, LEAVES_LAYER);
            }
        }

    private void extendWorldToRight(int avatarXPos, int windowXDim) {

        // adding world to right
        int minX = worldBuiltPointer + windowXDim;
        int maxX = worldBuiltPointer + (int) (1.5 * windowXDim);
        terrain.createInRange(minX, maxX);
        tree.createInRange(minX, maxX);

        // removing world from left
        for (GameObject obj : gameObjects()) {
            if  (obj.getCenter().x() < worldBuiltPointer - windowXDim / 2) {
                removeObjectFromItsLayer(obj);
            }
        }
        worldBuiltPointer = avatarXPos;
    }

    private void extendWorldToLeft(int avatarXPos, int windowXDim) {

        // adding world to left
        int minX = worldBuiltPointer - (int) (1.5 * windowXDim);
        int maxX = worldBuiltPointer - windowXDim;
        terrain.createInRange(minX, maxX);
        tree.createInRange(minX, maxX);


        // removing world from right
        for (GameObject obj : gameObjects()) {
            if (obj.getCenter().x() > worldBuiltPointer + windowXDim / 2) {
                removeObjectFromItsLayer(obj);
            }
        }
        worldBuiltPointer = avatarXPos;  // updating world pointer
    }

    private int normalizeToBlockSize(float x){
        return (int) (Math.floor(x / Block.SIZE) * Block.SIZE);
    }



    public static void main(String[] args) {

        new PepseGameManager().run();

    }
}
