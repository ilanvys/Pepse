package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.awt.*;

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

        int roundedX = getClosestSmallerNumDividesByBlockSize((int) x) / Block.SIZE;

        return 10 * Block.SIZE * ( (float) noise.noise(roundedX)) + groundHeightAtX0;  // todo keep 10?
    }

    public void createInRange(int minX, int maxX){

        minX = getClosestSmallerNumDividesByBlockSize(minX);
        maxX = getClosestSmallerNumDividesByBlockSize(maxX);

        for (int x = minX; x <= maxX; x += Block.SIZE) {

            int roundedHeight = (int) (Math.floor(groundHeightAt(x) / Block.SIZE) * Block.SIZE);
            for (int i = 0; i < TERRAIN_DEPTH; i++) {
                Block block = createBlock(x, roundedHeight);
                if (i < 2){
                    gameObjects.addGameObject(block, groundLayer);
                    block.renderer().setRenderable(new RectangleRenderable(Color.BLUE));
                    block.setTag(UPPER_TERRAIN_TAG);
                } else {
                    gameObjects.addGameObject(block, groundLayer + NON_COLLISABLE_LAYER_DIFF);
                    block.setTag(LOWER_TERRAIN_TAG);
                }

                roundedHeight += Block.SIZE;

            }
        }
    }

    private static Block createBlock(int x, int y) {
        Renderable renderable = new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));
        Block block = new Block(new Vector2(x, y), renderable);

        return block;

    }


    /**
     * Calculates the closest smaller num that divides by Block.SIZE
     * @param x
     * @return Closest num
     */
    private int getClosestSmallerNumDividesByBlockSize(int x){  // todo make sure not duplicate

        int remainder = x % Block.SIZE;

        if (remainder < 0){
            remainder += Block.SIZE;
        }

        return x - remainder;
    }

}