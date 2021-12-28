package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

public class Sun {
    private static GameObject sun;
    /**
     * This function creates a yellow circle that moves in
     * the sky in an elliptical path (in camera coordinates).
     * @param gameObjects  The collection of all participating game objects.
     * @param layer The number of the layer to which the created sun should be added.
     * @param windowDimensions The dimensions of the windows.
     * @param cycleLength The amount of seconds it should take the created game object
     *                    to complete a full cycle.
     * @return A new game object representing the sun.
     */
    public static GameObject create(
            GameObjectCollection gameObjects,
            int layer,
            Vector2 windowDimensions,
            float cycleLength) {

        sun = new GameObject(
                new Vector2(windowDimensions.x()/4, windowDimensions.y()/2),
                new Vector2(100, 100),
                new OvalRenderable(Color.yellow));

        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag("sun");
        createSunTransition(windowDimensions, cycleLength);

        gameObjects.addGameObject(sun, layer);

        return sun;
    }

    /**
     * This method initiates the Transition that makes the sun circle around
     * @param windowDimensions The dimensions of the windows.
     * @param cycleLength The amount of seconds it should take the created game
     *                    object to complete a full cycle.
     */
    private static void createSunTransition(Vector2 windowDimensions, float cycleLength) {
        float startPositionX = windowDimensions.x()/4;
        float intervalX = (3*windowDimensions.x()/4 - windowDimensions.x()/4) / 360;
        float intervalY = (windowDimensions.y()/3) / 360;

        // create sun movement transition
        new Transition<Float>(
                sun,
                (val) -> {
                    if(val <= 180) {
                        sun.setCenter(new Vector2(
                                startPositionX + intervalX*val,
                                sun.getCenter().y() - intervalY));
                    }
                    else {
                        sun.setCenter(new Vector2(
                                startPositionX + intervalX*val,
                                sun.getCenter().y() + intervalY));
                    }
                },
                0f,
                360f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
    }
}
