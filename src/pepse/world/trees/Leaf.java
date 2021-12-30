package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.Transition;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

public class Leaf extends GameObject {
    private Transition<Float> verticalTransition;
//    private boolean transitionExist = false; //TODO: make flag
    private static final String LEAF_BLOCK_TAG = "leafBlock";

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
        this.setTag(LEAF_BLOCK_TAG);
    }

    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (other.getTag().equals("upper terrain")){
            this.transform().setVelocity(0,0);
            this.removeComponent(verticalTransition);
        }
    }

    /**
     * This method creates a Transition that causes the leaf to move vertically
     * while falling, for a realistic feel.
     * @param leafBlock the leaf to append the Transition to.
     * @return
     */
    public Transition<Float> initLeafVerticalFallTransition(GameObject leafBlock, int transitionTime) {
        this.verticalTransition = new Transition<Float>(
                leafBlock,
                (val) -> {
                    if(val < 2) {
                        leafBlock.transform().setVelocity(20, 25);
                    }
                    if(val > 7) {
                        leafBlock.transform().setVelocity(-20, 25);
                    }
                },
                0f,
                10f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                transitionTime,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
        return this.verticalTransition;
    }
}
