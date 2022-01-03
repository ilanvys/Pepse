package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Terrain;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

/**
 * Responsible for the creation and management of trees.
 */
public class Tree {
    // CONSTANTS
    private final Color ROOT_COLOR = new Color(100, 50, 20);
    private final Color LEAF_COLOR = new Color(50, 200, 30);
    private final int FADEOUT_TIME = 10;
    private final int BLOCK = Block.SIZE;
    private final float TREE_ODD = 0.08f;
    private final int MIN_TREE_HEIGHT = 4;
    private final int MAX_TREE_HEIGHT = 12;
    private final int COLOR_DELTA = 10;
    private final int MAX_LEAF_FALL_INTERVAL = 50;
    private final int MAX_LEAF_ANIMATION_INTERVAL = 6;
    private final float MIN_ANGLE_TRANSITION = 0;
    private final float MAX_ANGLE_TRANSITION = 5;
    private final float MIN_DIMENSIONS_TRANSITION = -1;
    private final float MAX_DIMENSIONS_TRANSITION = 4;



    // FIELDS
    private final GameObjectCollection gameObjects;
    private final Terrain terrain;
    private Random rand;
    private final int rootLayer;
    private final int leavesLayer;
    private final int seed;
    private final String rootTag;
    private final String leafBlockTag;
    private final String upperTerrainTag;

    /**
     * This function initiates the class with all the params necessary
     * for creating the trees in the game.
     * @param gameObjects The collection of all participating game objects.
     * @param terrain the Terrain GameObject
     * @param seed A seed for a random number generator.
     * @param rootLayer int representing the layer of tree roots in the game
     * @param leavesLayer int representing the layer of tree leaves in the game
     * @param rootTag Tag representing the tree roots.
     * @param leafBlockTag Tag representing all leaves.
     * @param upperTerrainTag Tag representing the upper terrain blocks.
     */
    public Tree(GameObjectCollection gameObjects,
                Terrain terrain,
                int seed,
                int rootLayer,
                int leavesLayer,
                String rootTag,
                String leafBlockTag,
                String upperTerrainTag) {
        this.gameObjects = gameObjects;
        this.terrain = terrain;
        this.rootLayer = rootLayer;
        this.leavesLayer = leavesLayer;
        this.seed = seed;
        this.rootTag = rootTag;
        this.leafBlockTag = leafBlockTag;
        this.upperTerrainTag = upperTerrainTag;
    }

    /**
     * This method creates trees in a given range of x-values.
     * @param minX  The lower bound of the given range
     *              (will be rounded to a multiple of Block.SIZE).
     * @param maxX The upper bound of the given range
     *             (will be rounded to a multiple of Block.SIZE).
     */
    public void createInRange(int minX, int maxX){
        // normalize
        minX = normalizeToBlockSize(minX);
        maxX = normalizeToBlockSize(maxX);

        // swap values if min > max
        if (minX > maxX){
            int temp = maxX;
            maxX = minX;
            minX = temp;
        }

        for (int x = minX; x < maxX; x += Block.SIZE){
            rand = new Random(Objects.hash(x, seed));
            if (rand.nextFloat() < TREE_ODD) {

                int height = rand.nextInt(MAX_TREE_HEIGHT - MIN_TREE_HEIGHT) + MIN_TREE_HEIGHT;
                createTree(x, height);
            }
        }
    }

    /**
     * Method normalizes given int to be divisible by Block.SIZE (rounds int down)
     * @param x the num to normalize
     * @return normalized integer
     */
    private int normalizeToBlockSize(float x) {
        return (int) (Math.floor(x / Block.SIZE) * Block.SIZE);
    }

    /**
     * This method receives location for a tree and creates it.
     * @param treeLocation X-coordinate for the tree
     * @param rootHeight num of blocks in the tree root
     */
    private void createTree(int treeLocation, int rootHeight) {
        int groundHeight = calcHeightAt(treeLocation);

        // add root
        createRoot(groundHeight, treeLocation, rootHeight);
        // add leaves
        int treeTopSize = (2*rootHeight/3)*BLOCK;
        int leavesCol = treeLocation-treeTopSize/2;
        int leavesRow = rootHeight*BLOCK-treeTopSize/2;

        for (int i = leavesCol; i <= (leavesCol + treeTopSize); i+=BLOCK) {
            for (int j = leavesRow; j <= leavesRow + treeTopSize; j+=BLOCK) {
                Vector2 originalLeafLocation = new Vector2(i, groundHeight - j);
                Leaf leafBlock = createLeaf(originalLeafLocation);

                int location = (int) Math.abs(leafBlock.getTopLeftCorner().x()) +
                        (int) Math.abs(leafBlock.getTopLeftCorner().y()
                                - terrain.groundHeightAt(leafBlock.getTopLeftCorner().y()));

                Random rand = new Random(Objects.hash(location, seed));

                createLeafAnimation(leafBlock, rand);
                createLeafFallTask(leafBlock,
                        originalLeafLocation,
                        rand.nextInt(MAX_LEAF_FALL_INTERVAL)+1,
                        rand.nextInt(MAX_LEAF_ANIMATION_INTERVAL)+1);

                gameObjects.addGameObject(leafBlock, leavesLayer);
            }
        }
    }

    /**
     * @param location x-coordinate
     * @return The terrain height at that location
     *         rounded to a multiple of Block.SIZE
     */
    private int calcHeightAt(int location) {
        int height = (int) terrain.groundHeightAt(location);
        int roundedHeight = height - (height % Block.SIZE) - Block.SIZE;
        return roundedHeight;
    }

    /**
     * This method creates a leaf of a tree.
     * @param originalLeafLocation The location the leaf as originally
     *                             created at.
     * @return GameObject of a leaf
     */
    private Leaf createLeaf(Vector2 originalLeafLocation){
        return new Leaf(
                originalLeafLocation,
                new Vector2(BLOCK, BLOCK),
                new RectangleRenderable(
                        util.ColorSupplier.approximateColor(
                                LEAF_COLOR, COLOR_DELTA*2)),
                leafBlockTag,
                upperTerrainTag);
    }

    /**
     * This method creates a root of a tree, that is made up from blocks.
     * @param groundHeight The terrain height at the location.
     * @param treeLocation The x-coordinate for the root to be located at.
     * @param rootHeight The number of blocks in the root.
     */
    private void createRoot(int groundHeight, int treeLocation, int rootHeight) {
        Vector2 BlockSizeVector = new Vector2(BLOCK, BLOCK);

        for (int i = 0; i < rootHeight; i++) {
            GameObject rootBlock = new Block(
                    new Vector2(treeLocation, groundHeight - (i*BLOCK)),
                    new RectangleRenderable(
                            util.ColorSupplier.approximateColor(
                                    ROOT_COLOR, COLOR_DELTA)
            ));

            rootBlock.setTag(rootTag);
            gameObjects.addGameObject(rootBlock, rootLayer);
        }
    }

    /**
     * This method creates two scheduled tasks for random leaf
     * transitions.
     * @param leafBlock the leaf to append movement to.
     * @param leafRand Random to select times from.
     */
    private void createLeafAnimation(GameObject leafBlock, Random leafRand) {
        new ScheduledTask(
                leafBlock,
                leafRand.nextInt(MAX_LEAF_ANIMATION_INTERVAL)+1,
                false,
                () -> createAngleChangeTransition(leafBlock, leafRand.nextInt(MAX_LEAF_ANIMATION_INTERVAL)+1));
        new ScheduledTask(
                leafBlock,
                leafRand.nextInt(MAX_LEAF_ANIMATION_INTERVAL)+1,
                false,
                () -> createDimensionsChangeTransition(leafBlock, leafRand.nextInt(MAX_LEAF_ANIMATION_INTERVAL)+1));
    }

    /**
     * This method creates a Transition that plays with the leaf's
     * size, for a realistic feel.
     * @param leafBlock the leaf to append the Transition to.
     * @param leafRand transition time based on the leaf's randomness.
     */
    private void createDimensionsChangeTransition(GameObject leafBlock, int leafRand) {
        new Transition<>(
                leafBlock,
                (size) -> leafBlock.setDimensions(
                        new Vector2(BLOCK + size, BLOCK + size)),
                MIN_DIMENSIONS_TRANSITION,
                MAX_DIMENSIONS_TRANSITION,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                leafRand,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
    }

    /**
     * This method creates a TransitionTransition that plays with the leaf's
     * angle, for a realistic feel.
     * @param leafBlock the leaf to append the Transition to.
     * @param leafRand transition time based on the leaf's randomness.
     */
    private void createAngleChangeTransition(GameObject leafBlock, int leafRand) {
        new Transition<>(
                leafBlock,
                (angle) -> leafBlock.renderer().setRenderableAngle(angle),
                MIN_ANGLE_TRANSITION,
                MAX_ANGLE_TRANSITION,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                leafRand,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
    }

    /**
     * This method creates a scheduled task for the leaf's fade out after
     * a random time, and handles all that logic.
     * @param leafBlock the leaf to append the task to.
     * @param originalLeafLocation The location the leaf as originally
     *                             created at.
     * @param leafRand transition time based on the leaf's randomness.
     * @param verticalRand transition time based on the leaf's randomness.
     */
    private void createLeafFallTask(
            Leaf leafBlock,
            Vector2 originalLeafLocation,
            int leafRand,
            int verticalRand) {
        leafBlock.renderer().setOpaqueness(1);
        leafBlock.setTopLeftCorner(originalLeafLocation);

        new ScheduledTask(
            leafBlock,
            leafRand*MAX_LEAF_ANIMATION_INTERVAL,
            false,
            () -> {
                // create transition for vertical movement
                leafBlock.initLeafVerticalFallTransition(leafBlock, verticalRand);

                leafBlock.renderer().fadeOut(FADEOUT_TIME, () -> initLeafAfterlifeWaitTask(
                        leafBlock,
                        originalLeafLocation,
                        leafRand,
                        verticalRand));
            });
    }

    /**
     * This method creates a scheduled task that waits a random time
     * after the leaf has faded out, until it re-appears in its original location.
     * @param leafBlock the leaf to append the task to.
     * @param originalLeafLocation The location the leaf as originally
     *                             created at.
     * @param leafRand transition time based on the leaf's randomness.
     * @param verticalRand transition time based on the leaf's randomness.
     */
    private void initLeafAfterlifeWaitTask(Leaf leafBlock,
                                           Vector2 originalLeafLocation,
                                           int leafRand,
                                           int verticalRand) {
        new ScheduledTask(
            leafBlock,
            MAX_LEAF_FALL_INTERVAL/2,
            false,
            () -> createLeafFallTask(leafBlock, originalLeafLocation, leafRand, verticalRand));
    }
}
