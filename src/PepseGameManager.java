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
import pepse.world.Terrain;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Tree;

import java.awt.*;
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
    public int LEAVES_LAYER = Layer.DEFAULT;
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
    private int worldBuiltPointer = 0;  // relative to avatar's pos!!!

    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader, UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        this.windowDimensions = windowController.getWindowDimensions();
        this.imageReader = imageReader;
        this.windowController = windowController;
        this.inputListener = inputListener;
        this.soundReader = soundReader;

        // todo erase
//        windowController.setTargetFramerate(30);

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
        GameObject sunHalo = SunHalo.create(this.gameObjects(), SUN_HALO_LAYER, sun, SUN_COLOR); // todo
        // const

        // create trees
        this.tree = new Tree(this.gameObjects(), this.windowDimensions, terrain, seed, ROOT_LAYER, LEAVES_LAYER);

        // create avatar
        Vector2 initPos = windowDimensions.mult(0.5f); // middle of screen
        this.avatar = Avatar.create(gameObjects(), AVATAR_LAYER, initPos, inputListener, imageReader);

        // set camera
        setCamera(new Camera(avatar, Vector2.ZERO, windowDimensions, windowDimensions));

        // build world
        int windowDimX = (int) windowDimensions.x();
        terrain.createInRange(-windowDimX, 2*windowDimX);
        tree.createInRange(-windowDimX, 2*windowDimX);
        worldBuiltPointer = (int) avatar.getCenter().x();


        // TODO REFACTOR THIS
        gameObjects().layers().shouldLayersCollide(LEAVES_LAYER, UPPER_TERRAIN_LAYER, true);
//        System.out.println(gameObjects().layers().doLayersCollide(Layer.DEFAULT+1, Layer.DEFAULT-10));


    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        int avatarXPos = (int) avatar.getCenter().x();
        int windowXDim = (int) windowDimensions.x()/2; // todo erase /2

        if (worldBuiltPointer + windowXDim / 2 < avatarXPos) {
            extendWorldToRight(avatarXPos, windowXDim);
        }
        if (avatarXPos < worldBuiltPointer - windowXDim / 2) {
            extendWorldToLeft(avatarXPos, windowXDim);
        }


    }

    private void removeObjectFromItsLayer(GameObject obj){
        switch (obj.getTag()) {
            case UPPER_TERRAIN_TAG:
                gameObjects().removeGameObject(obj, UPPER_TERRAIN_LAYER);
                break;
            case LOWER_TERRAIN_TAG:
                gameObjects().removeGameObject(obj, LOWER_TERRAIN_LAYER);
                break;
            case ROOT_TAG:
                gameObjects().removeGameObject(obj, ROOT_LAYER);
                break;
            case LEAF_BLOCK_TAG:
                gameObjects().removeGameObject(obj, LEAVES_LAYER);
                break;
            // TODO general obj?
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

    public static void main(String[] args) {

    new PepseGameManager().run();

    }
}
