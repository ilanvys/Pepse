package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

public class SunHalo {
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

        return sunHalo;
    }

    @FunctionalInterface
    public interface Component {
        void update(float deltaTime);
    }

    public void addComponent(Component component) {}
}
