import danogl.GameManager;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.util.Vector2;
import pepse.world.NoiseGenerator;
import pepse.world.Terrain;

import java.util.Random;

public class PepseGameManager extends GameManager {



    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader, UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);

        // add sky
        pepse.world.Sky.create(gameObjects(), windowController.getWindowDimensions(), 0);

        Terrain t = new Terrain(gameObjects(), 1, windowController.getWindowDimensions(), 19);

        t.createInRange(0,1920);




    }

    public static void main(String[] args) {

    new PepseGameManager().run();

    }
}
