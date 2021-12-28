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
                new Vector2(windowDimensions.x()/2, windowDimensions.y()/4),
                new Vector2(50,50),
                new OvalRenderable(Color.yellow));

        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag("sun");
        gameObjects.addGameObject(sun, layer);

        // create sun movement transition
        new Transition<Float>(
                sun,
                (a) -> {
//                  TODO:  Make it go round
                    sun.setCenter(sun.getCenter().add(Vector2.RIGHT));
                },
                0f,
                360f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength/2,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );

        return sun;
    }



//    private void calcSunPosition(Consumer<Float>  angleInSky) {
//        sun.setCenter(sun.getCenter().add(Vector2.RIGHT));
//    }
}
