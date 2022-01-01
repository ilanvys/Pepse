package pepse.world;

import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import util.NoiseGenerator;
import util.ColorSupplier;

import java.awt.*;

/**
 * Represents Terrain object. Terrain can tell the wanted ground's height at certain X and can build Terrain.
 * @author Yonatan Chocron
 */
public class Terrain {

    private static final Color BASE_GROUND_COLOR = new Color(212, 123,74);
    private static final String UPPER_TERRAIN_TAG = "upper terrain";
    private static final String LOWER_TERRAIN_TAG = "lower terrain";
    private static final int TERRAIN_DEPTH = 20;
    private static final int NON_COLLISABLE_LAYER_DIFF = -10; // will add this diff to received layer in
    // constructor in order to differentiate blocks of top level (2 upper rows, can be collided with) and
    // other blocks (collision won't do a thing. Avatar won't collide with the as they are deep ( >2 ).

    private final GameObjectCollection gameObjects;
    private final int groundLayer;
    private final NoiseGenerator noise;

    private float groundHeightAtX0 = 2/3f; // of window dimensions

    public Terrain(GameObjectCollection gameObjects,
                   int groundLayer,
                   Vector2 windowDimensions,
                   int seed) {

        this.gameObjects = gameObjects;
        this.groundLayer = groundLayer;
        this.groundHeightAtX0 *= windowDimensions.y();
        this.noise = new NoiseGenerator(seed);
    }


    public float groundHeightAt(float x) {
        int roundedX = normalizeToBlockSize((int) x) / Block.SIZE;
        return 10 * Block.SIZE * ( (float) noise.noise(roundedX)) + groundHeightAtX0;
    }

    /**
     * Method builds terrain from minX till maxX at given order (it'll start from minX and build till maxX,
     * also if minX > maxx)
     * @param minX start X coord
     * @param maxX end X coord
     */
    public void createInRange(int minX, int maxX){

        minX = normalizeToBlockSize(minX);
        maxX = normalizeToBlockSize(maxX);

        // building according to avatar direction
        if (minX < maxX){
            for (int x = minX; x < maxX; x += Block.SIZE) {
                createAtX(x);
            }
        }
        if (minX > maxX){
            for (int x = minX; x >= maxX; x -= Block.SIZE){
                createAtX(x);
            }
        }
    }

    /**
     * Method builds terrain at proper height at given X.
     * @param x
     */
    private void createAtX(int x) {
        int roundedHeight = (int) (Math.floor(groundHeightAt(x) / Block.SIZE) * Block.SIZE);
        for (int i = 0; i < TERRAIN_DEPTH; i++) {
            Block block = createBlock(x, roundedHeight);
            if (i < 2){
                gameObjects.addGameObject(block, groundLayer);
                block.setTag(UPPER_TERRAIN_TAG);
            } else {
                gameObjects.addGameObject(block, groundLayer + NON_COLLISABLE_LAYER_DIFF);
                block.setTag(LOWER_TERRAIN_TAG);
            }

            roundedHeight += Block.SIZE;

        }
    }

    /**
     * Method creates block at a certain position (x,y)
     * @param x X coord
     * @param y Y coord
     * @return Block
     */
    private static Block createBlock(int x, int y) {
        Renderable renderable = new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));
        Block block = new Block(new Vector2(x, y), renderable);
        return block;
    }

    /**
     * Method normalizes given int to be divisible by Block.SIZE (rounds int down)
     * @param x
     * @return
     */
    private int normalizeToBlockSize(float x){  // todo make sure not duplicate
        return (int) (Math.floor(x / Block.SIZE) * Block.SIZE);
    }
}
