package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

public class SunHalo {
    /**
     * This function creates a halo around a given object that represents the sun.
     * The halo will be tied to the given sun, and will always move with it.
     * @param gameObjects The collection of all participating game objects.
     * @param layer The number of the layer to which the created halo should be added.
     * @param sun A game object representing the sun
     *            (it will be followed by the created game object).
     * @param color The color of the halo.
     * @return A new game object representing the sun's halo.
     */
    public static GameObject create(
            GameObjectCollection gameObjects,
            int layer,
            GameObject sun,
            Color color ) {
        GameObject sunHalo = new GameObject(
                Vector2.ZERO,
                new Vector2(100, 100),
                new OvalRenderable(color));

        sunHalo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sunHalo.setTag("sunHalo");
        gameObjects.addGameObject(sunHalo, layer);

        sunHalo.addComponent((deltaTime) -> {
            sunHalo.setCenter(sun.getCenter());
        });

        return sunHalo;
    }

    @FunctionalInterface
    private interface Component {
        void update(float deltaTime);
    }

    private void addComponent(Component component) {}
}
