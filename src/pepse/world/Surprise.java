package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.ImageReader;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents surprise class.
 * Method can createInRange, and it has a 'turn off' flag (as the surprised is one-time per game)
 * @author Yonatan Chocron
 */
public class Surprise {

    private static final int BUFFER = 100*Block.SIZE; // multiplication of Block.SIZE
    private static final Vector2 SIZE = new Vector2(100, 100);
    private static final String SURPRISE_IMAGE_PATH = "src/pepse/assets/miri.jpeg";
    private static boolean doneSurprise = false;

    private final GameObjectCollection gameObjects;
    private final int layer;
    private final Terrain terrain;
    private final String surpriseTag;
    private final Renderable surpriseRenderable;

    /**
     * constructor
     * @param gameObjects gameObjects
     * @param surpriseLayer Layer to put surprise in
     * @param surpriseTag surprise tag
     * @param imageReader imageReader instance
     * @param terrain current terrain instance
     */
    public Surprise (GameObjectCollection gameObjects,
                     int surpriseLayer,
                     String surpriseTag,
                     ImageReader imageReader,
                     Terrain terrain){

        this.layer = surpriseLayer;
        this.gameObjects = gameObjects;
        this.terrain = terrain;
        this.surpriseTag = surpriseTag;

        this.surpriseRenderable = imageReader.readImage(SURPRISE_IMAGE_PATH, true);


    }

    /**
     * Method builds surprises from minX till maxX
     * @param minX start X coord
     * @param maxX end X coord
     */
    public void createInRange(int minX, int maxX){
        minX = normalizeToBlockSize(minX);
        maxX = normalizeToBlockSize(maxX);

        // swap values if min > max
        if (minX > maxX){
            int temp = maxX;
            maxX = minX;
            minX = temp;
        }


        for (int x = minX; x < maxX; x += Block.SIZE) {
            if (x % BUFFER == 0){
                createSurprise(x);

            }
        }
    }

    /**
     * Setter should run after surprised had been used. It'll prevent building anymore surprises.
     */
    public static void setDoneSurprise(){
        doneSurprise = true;
    }


    /**
     * creates surprise at given X.
     * @param x
     */
    private void createSurprise(int x){

        if (doneSurprise) {
            return;
        }

        int height = normalizeToBlockSize(terrain.groundHeightAt(x)) - (int) SIZE.y();
        Vector2 surpriseTopLeftCorner = new Vector2(x, height);

        GameObject surprise = new GameObject(surpriseTopLeftCorner, SIZE, surpriseRenderable);
        surprise.setTag(surpriseTag);
        gameObjects.addGameObject(surprise);
    }

    /**
     * Method normalizes given int to be divisible by Block.SIZE (rounds int down)
     * @param x
     * @return normalized int
     */
    private int normalizeToBlockSize(float x) {
        return (int) (Math.floor(x / Block.SIZE) * Block.SIZE);
    }


}
