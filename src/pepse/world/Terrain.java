package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;

public class Terrain {

    private static final Color BASE_GROUND_COLOR = new Color(212, 123,74);
    private static final String GROUND_TAG = "ground";

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

        return (float) (noise.noise(x)+1)*groundHeightAtX0;  // +1 since it returns neg values

    }

    public void createInRange(int minX, int maxX){

        Renderable r = new RectangleRenderable(BASE_GROUND_COLOR);
        GameObject block = new GameObject(Vector2.ZERO, Vector2.ONES.mult(200), r);
        block.setTag(GROUND_TAG);
        gameObjects.addGameObject(block);

    }

}