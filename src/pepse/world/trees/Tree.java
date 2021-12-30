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

public class Tree {

    // CONSTANTS
    private final Color ROOT_COLOR = new Color(100, 50, 20);
    private final Color LEAF_COLOR = new Color(50, 200, 30);
    private final int FADEOUT_TIME = 10;
    private final int BLOCK = Block.SIZE;
    private final int MIN_DIST_BETWEEN_TREES = 10 * Block.SIZE;
    private final float TREE_ODD = 0.08f;  // being checked every MIN_DIST_BETWEEN_TREES pixels
    private final int MIN_TREE_HEIGHT = 4;
    private final int MAX_TREE_HEIGHT = 12;
    private final String ROOT_TAG = "rootBlock";

    // TODO REFACTOR! all remaining magic nums ->> constants (there are plenty magic nums in transitions)

    // FIELDS
    private final GameObjectCollection gameObjects;
    private final Vector2 windowDimensions;
    private final Terrain terrain; //TODO: get callback maybe?
    private final Random rand;
    private final int rootLayer;
    private final int leavesLayer;
    private final int seed;

    /**
     * This function initiates the class with all the params necessary
     * for creating the trees in the game.
     * @param gameObjects The collection of all participating game objects.
     * @param windowDimensions The dimensions of the windows.
     * @param terrain the Terrain GameObject
     * @param seed A seed for a random number generator.
     */
    public Tree(GameObjectCollection gameObjects,
                Vector2 windowDimensions,
                Terrain terrain,
                int seed,
                int rootLayer,
                int leavesLayer) {
        this.gameObjects = gameObjects;
        this.windowDimensions = windowDimensions;
        this.terrain = terrain;
        this.rand = new Random(seed);
        this.rootLayer = rootLayer;
        this.leavesLayer = leavesLayer;
        this.seed = seed;
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
            Random r = new Random(Objects.hash(x, seed));
            if (r.nextFloat() < TREE_ODD) {

                int height = r.nextInt(MAX_TREE_HEIGHT - MIN_TREE_HEIGHT) + MIN_TREE_HEIGHT;
                createTree(x, height);
            }
        }
    }

    private int normalizeToBlockSize(float x){  // todo make sure not duplicate
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

                createLeafAnimation(leafBlock);
                createLeafFallTask(leafBlock, originalLeafLocation);

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
     * @return random location for the first tree,
     *         rounded to a multiple of Block.SIZE
     */
    private int calcInitialTreeLocation() {
        return (rand.nextInt(1000)/BLOCK)*BLOCK;
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
                        pepse.util.ColorSupplier.approximateColor(
                                LEAF_COLOR, 20)
                ));
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
            GameObject rootBlock = new GameObject(
                    new Vector2(treeLocation, groundHeight - (i*BLOCK)),
                    BlockSizeVector,
                    new RectangleRenderable(
                            pepse.util.ColorSupplier.approximateColor(
                                    ROOT_COLOR, 10)
                    ));

            rootBlock.setTag(ROOT_TAG);
            gameObjects.addGameObject(rootBlock, rootLayer);
        }
    }



    /**
     * This method creates two scheduled tasks for random leaf
     * transitions.
     * @param leafBlock the leaf to append movement to.
     */
    private void createLeafAnimation(GameObject leafBlock) {
        new ScheduledTask(
                leafBlock,
                rand.nextInt(19) + 1,
                true,
                () -> createAngleChangeTransition(leafBlock));
        new ScheduledTask(
                leafBlock,
                rand.nextInt(19) + 1,
                true,
                () -> createDimensionsChangeTransition(leafBlock));
    }

    /**
     * This method creates a Transition that plays with the leaf's
     * size, for a realistic feel.
     * @param leafBlock the leaf to append the Transition to.
     */
    private void createDimensionsChangeTransition(GameObject leafBlock) {
        new Transition<Float>(
                leafBlock,
                (size) -> leafBlock.setDimensions(
                        new Vector2(BLOCK+size, BLOCK+size)),
                -1f,
                4f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                rand.nextInt(7) + 3,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
    }

    /**
     * This method creates a TransitionTransition that plays with the leaf's
     * angle, for a realistic feel.
     * @param leafBlock the leaf to append the Transition to.
     */
    private void createAngleChangeTransition(GameObject leafBlock) {
        new Transition<Float>(
                leafBlock,
                (angle) -> leafBlock.renderer().setRenderableAngle(angle),
                0f,
                5f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                rand.nextInt(7) + 3,
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
     */
    private void createLeafFallTask(Leaf leafBlock, Vector2 originalLeafLocation) {
        leafBlock.renderer().setOpaqueness(1);
        leafBlock.setTopLeftCorner(originalLeafLocation);

        new ScheduledTask(
            leafBlock,
            rand.nextInt(60) + 5,
            false,
            () -> {
                // create transition for vertical movement
                leafBlock.initLeafVerticalFallTransition(leafBlock, rand.nextInt(5) + 2);

                leafBlock.renderer().fadeOut(FADEOUT_TIME, () -> {
                    // stop leaf horizontal and vertical movement when fadeOut ends
//                    leafBlock.transform().setVelocity(0, 0);
//                    leafBlock.removeComponent(verticalTransition);

                    initLeafAfterlifeWaitTask(
                            leafBlock,
                            originalLeafLocation,
                            rand.nextInt(5));
                });
            });
    }

    /**
     * This method creates a scheduled task that waits a random time
     * after the leaf has faded out, until it re-appears in its original location.
     * @param leafBlock the leaf to append the task to.
     * @param originalLeafLocation The location the leaf as originally
     *                             created at.
     * @param afterlifeTime The time needed to wait before the leaf reappears.
     */
    private void initLeafAfterlifeWaitTask(Leaf leafBlock,
                                           Vector2 originalLeafLocation,
                                           int afterlifeTime) {
        new ScheduledTask(
            leafBlock,
            afterlifeTime,
            false,
            () -> createLeafFallTask(leafBlock, originalLeafLocation));
    }

    /**
     * This method creates a Transition that causes the leaf to move vertically
     * while falling, for a realistic feel.
     * @param leafBlock the leaf to append the Transition to.
     */
    private Transition<Float> initLeafVerticalFallTransition(GameObject leafBlock) {
        return new Transition<Float>(
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
                rand.nextInt(5) + 2,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
    }
}
