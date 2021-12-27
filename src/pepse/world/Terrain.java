package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.awt.*;

public class Terrain {

    private static final Color BASE_GROUND_COLOR = new Color(212, 123,74);
    private static final String GROUND_TAG = "ground";
    private static final int TERRAIN_DEPTH = 20;

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

                createBlock(x, roundedHeight);
                roundedHeight += Block.SIZE;

            }
        }
    }

    private void createBlock(int x, int y) {
        Renderable renderable = new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));
        GameObject block = new Block(new Vector2(x, y), renderable);
        block.setTag(GROUND_TAG);
        gameObjects.addGameObject(block);
    }

    /**
     * Calculates the closest smaller num that divides by Block.SIZE
     * @param x
     * @return Closest num
     */
    private int getClosestSmallerNumDividesByBlockSize(int x){

        int remainder = x % Block.SIZE;

        if (remainder < 0){
            remainder += Block.SIZE;
        }

        return x - remainder;
    }

}