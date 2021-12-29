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

    // FIELDS
    private Vector2 windowDimensions;
    private ImageReader imageReader;
    private WindowController windowController;
    private UserInputListener inputListener;
    private SoundReader soundReader;
    private Avatar avatar;
    private Terrain terrain;
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
        windowController.setTargetFramerate(30);


        // seed
        Random random = new Random();
        int seed = random.nextInt(OPTIONAL_SEEDS);

        // create sky
        pepse.world.Sky.create(gameObjects(), windowController.getWindowDimensions(), Layer.BACKGROUND);

        // create terrain
        this.terrain = new Terrain(gameObjects(), Layer.STATIC_OBJECTS,
                windowController.getWindowDimensions(), 20); // todo use real seed


        // create night/day
        Night.create(this.gameObjects(), Layer.FOREGROUND, this.windowDimensions, 30.0F);

        // crate sun
        GameObject sun = Sun.create(this.gameObjects(), Layer.BACKGROUND+2, this.windowDimensions, 30.0F);

        // create sun halo
        GameObject sunHalo = SunHalo.create(this.gameObjects(), Layer.BACKGROUND+1, sun, new Color(255, 255, 0, 20));

        // create trees
        Tree tree = new Tree(this.gameObjects(), this.windowDimensions, terrain, seed);
        tree.createInRange(0,1920);

        // create avatar
        Vector2 initPos = windowDimensions.mult(0.5f); // middle of screen
        this.avatar = Avatar.create(gameObjects(), Layer.DEFAULT, initPos, inputListener, imageReader);

        // set camera
        setCamera(new Camera(avatar, Vector2.ZERO, windowDimensions, windowDimensions));

        int windowDimX = (int) windowDimensions.x();
        terrain.createInRange(-windowDimX, 2*windowDimX); // todo infinite
        // world
        worldBuiltPointer = (int) avatar.getCenter().x();

        gameObjects().layers().shouldLayersCollide(Layer.DEFAULT+1, Layer.DEFAULT-10, true);
        System.out.println(gameObjects().layers().doLayersCollide(Layer.DEFAULT+1, Layer.DEFAULT-10));
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        int avatarXPos = (int) avatar.getCenter().x();
        int windowXDim = (int) windowDimensions.x();

        if (worldBuiltPointer + windowXDim / 2 < avatarXPos) {
            extendWorldToRight(avatarXPos, windowXDim);
        }
        if (avatarXPos < worldBuiltPointer - windowXDim / 2) {
            extendWorldToLeft(avatarXPos, windowXDim);
        }


    }

    private void extendWorldToRight(int avatarXPos, int windowXDim) {

        // adding world to right
        terrain.createInRange(worldBuiltPointer + windowXDim, worldBuiltPointer + (int) (1.5 * windowXDim));

        // removing world from left
        for (GameObject obj : gameObjects()) {
            if  (obj.getCenter().x() < worldBuiltPointer - windowXDim / 2) {
                gameObjects().removeGameObject(obj, Layer.DEFAULT);
                gameObjects().removeGameObject(obj, Layer.DEFAULT - 10); // TODO CONST LAYER!!!
            }
        }
        worldBuiltPointer = avatarXPos;
    }

    private void extendWorldToLeft(int avatarXPos, int windowXDim) {

        // adding world to left
        terrain.createInRange(worldBuiltPointer - (int) (1.5 * windowXDim), worldBuiltPointer - windowXDim);

        // removing world from right
        for (GameObject obj : gameObjects()) {
            if (obj.getCenter().x() > worldBuiltPointer + windowXDim / 2) {
                gameObjects().removeGameObject(obj, Layer.DEFAULT);
                gameObjects().removeGameObject(obj, Layer.DEFAULT - 10); // TODO CONST LAYER!!!
            }
        }
        worldBuiltPointer = avatarXPos;  // updating world pointer
    }

    public static void main(String[] args) {

    new PepseGameManager().run();

    }
}
