import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.util.Vector2;
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

    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader, UserInputListener inputListener, WindowController windowController) {
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
        pepse.world.Sky.create(gameObjects(), windowController.getWindowDimensions(), Layer.BACKGROUND);

        // create terrain
        Terrain terrain = new Terrain(gameObjects(), Layer.STATIC_OBJECTS,
                windowController.getWindowDimensions(), 19); // todo use real seed

        terrain.createInRange(0,1920); // todo infinite world

        // create night/day
        Night.create(this.gameObjects(), Layer.FOREGROUND, this.windowDimensions, 30.0F);

        GameObject sun = Sun.create(this.gameObjects(), Layer.BACKGROUND+2, this.windowDimensions, 30.0F);
        GameObject sunHalo = SunHalo.create(this.gameObjects(), Layer.BACKGROUND+1, sun, new Color(255, 255, 0, 40));
        sunHalo.addComponent((deltaTime) -> {
            sunHalo.setCenter(sun.getCenter());
        });

        Tree tree = new Tree(this.gameObjects(), this.windowDimensions, terrain);
        tree.createInRange(0,1920);
    }

    public static void main(String[] args) {

    new PepseGameManager().run();

    }
}
