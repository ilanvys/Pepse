package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.Transition;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Responsible for the creation of Leaves.
 */
public class Leaf extends GameObject {
    private final String upperTerrainTag;
    private Transition<Float> verticalTransition;

    /**
     * Constructs a new leaf instance.
     *
     * @param topLeftCorner   Position of the object, in window coordinates (pixels).
     *                        Note that (0,0) is the top-left corner of the window.
     * @param dimensions      Width and height in window coordinates.
     * @param renderable      The renderable representing the object. Can be null.
     * @param leafBlockTag    Tag representing all leaves.
     * @param upperTerrainTag Tag representing the upper terrain blocks.
     */
    public Leaf(Vector2 topLeftCorner,
                Vector2 dimensions,
                Renderable renderable,
                String leafBlockTag,
                String upperTerrainTag) {
        super(topLeftCorner, dimensions, renderable);
        this.upperTerrainTag = upperTerrainTag;
        this.setTag(leafBlockTag);
    }

    /**
     * Makes the leaf stop moving when colliding with the terrain.
     *
     * @param other The game object the ball collided into.
     * @param collision  an instance of the collision
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);

        if (other.getTag().equals(upperTerrainTag)) {
            this.transform().setVelocity(0,0);
            this.removeComponent(verticalTransition);
        }
    }

    /**
     * This method creates a Transition that causes the leaf to move vertically
     * while falling, for a realistic feel.
     * @param leafBlock the leaf to append the Transition to.
     * @param transitionTime the amount of time the leaf goes to each direction.
     */
    public void initLeafVerticalFallTransition(GameObject leafBlock, int transitionTime) {
        this.verticalTransition = new Transition<>(
                leafBlock,
                (val) -> {
                    if (val < 2) {
                        leafBlock.transform().setVelocity(20, 25);
                    }
                    if (val > 7) {
                        leafBlock.transform().setVelocity(-20, 25);
                    }
                },
                0f,
                10f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                transitionTime + 1,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
    }
}
