package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;

public class Avatar extends GameObject {

    private static final int VEL_X = 300;
    private static final int VEL_Y = 300;
    private static final int GRAVITY = 300;
    private static final Vector2 DIMENSIONS = new Vector2(200,200);
    private final UserInputListener inputListener;


    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param renderable    The renderable representing the object. Can be null, in which case
     */
    public Avatar(Vector2 topLeftCorner, Renderable renderable, UserInputListener inputListener) { //todo inputListener in ctr??
        super(topLeftCorner, DIMENSIONS, renderable);

        this.inputListener = inputListener;
    }

    public static Avatar create(GameObjectCollection gameObject,
                                int layer,
                                Vector2 topLeftCorner,
                                UserInputListener inputListener,
                                ImageReader imageReader){


//        Renderable avatarImage = imageReader.readImage() // todo

        Renderable avatarImage = new RectangleRenderable(Color.BLACK);

        Avatar avatar = new Avatar(topLeftCorner, avatarImage, inputListener);
        gameObject.addGameObject(avatar, Layer.DEFAULT);
        return avatar;

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        float xVel = 0;

        //input listener stuff

    }
}
