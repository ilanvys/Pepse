package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

public class Leaf extends GameObject {
    //TODO: add documentation
    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     * @param dimensions    Width and height in window coordinates.
     * @param renderable    The renderable representing the object. Can be null, in which case
     */
    public Leaf(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable) {
        super(topLeftCorner, dimensions, renderable);
        this.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        this.setTag("leafBlock");
    }

//    @Override
//    public void onCollisionEnter(GameObject other, Collision collision) {
//        super.onCollisionEnter(other, collision);
//        if(this.shouldCollideWith(other)) {
//            this.transform().setVelocity(0,0);
//        }
//    }
}
