package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Terrain;

import java.awt.*;
import java.util.Random;

public class Tree {
    private final Color ROOT_COLOR = new Color(100, 50, 20);
    private final Color LEAF_COLOR = new Color(50, 200, 30);
    private final int FADEOUT_TIME = 10;
    private final int BLOCK = Block.SIZE;

    private final GameObjectCollection gameObjects;
    private final Vector2 windowDimensions;
    private final Terrain terrain; //TODO: get callback maybe?
    private final Random rand;

    public Tree(GameObjectCollection gameObjects,
                Vector2 windowDimensions,
                Terrain terrain,
                int seed) {
        this.gameObjects = gameObjects;
        this.windowDimensions = windowDimensions;
        this.terrain = terrain;
        this.rand = new Random(seed);
    }

    public void createInRange(int minX, int maxX) {
        int initialTreeLocation = rand.nextInt(1000);
        for (int i = initialTreeLocation; i < windowDimensions.x(); i+=500) {
            int treeLocation = i;
            int treeHeight = rand.nextInt(8) + 4;

            this.create(treeLocation, treeHeight);
        }
    }

    public void create(int treeLocation, int treeHeight) {
        int groundHeight = calcHeightAt(treeLocation);

        // add root
        createRoot(groundHeight, treeLocation, treeHeight);

        // add leaves
        int treeTopSize = (2*treeHeight/3)*BLOCK;
        int leavesCol = treeLocation-treeTopSize/2;
        int leavesRow = treeHeight*BLOCK-treeTopSize/2;

        for (int i = leavesCol; i <= (leavesCol + treeTopSize); i+=BLOCK) {
            for (int j = leavesRow; j <= leavesRow + treeTopSize; j+=BLOCK) {
                Vector2 originalLeafLocation = new Vector2(i, groundHeight - j);
                GameObject leafBlock = createLeaf(originalLeafLocation);

                createLeafAnimation(leafBlock);
                initLeafFallTask(leafBlock, originalLeafLocation);

                gameObjects.addGameObject(leafBlock);
            }
        }
    }

    private int calcHeightAt(int location) {
        return (int) (terrain.groundHeightAt(location)/BLOCK)
                *BLOCK-BLOCK;
    }

    private GameObject createLeaf(Vector2 originalLeafLocation){
        GameObject leafBlock = new GameObject(
                originalLeafLocation,
                new Vector2(BLOCK, BLOCK),
                new RectangleRenderable(
                        pepse.util.ColorSupplier.approximateColor(
                                LEAF_COLOR, 20)
                ));

        leafBlock.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        leafBlock.setTag("leafBlock");

        return leafBlock;
    }


    private void createRoot(int groundHeight, int treeLocation, int treeHeight) {
        Vector2 BlockSizeVector = new Vector2(BLOCK, BLOCK);

        for (int i = 0; i < treeHeight; i++) {
            GameObject rootBlock = new GameObject(
                    new Vector2(treeLocation, groundHeight - (i*BLOCK)),
                    BlockSizeVector,
                    new RectangleRenderable(
                            pepse.util.ColorSupplier.approximateColor(
                                    ROOT_COLOR, 10)
                    ));

            rootBlock.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
            rootBlock.setTag("rootBlock");

            gameObjects.addGameObject(rootBlock);
        }
    }

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

    private void createDimensionsChangeTransition(GameObject leafBlock) {
        new Transition<Float>(
                leafBlock,
                (a) -> leafBlock.setDimensions(new Vector2(BLOCK+a, BLOCK+a)),
                -1f,
                4f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                rand.nextInt(7) + 3,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
    }

    private void createAngleChangeTransition(GameObject leafBlock) {
        new Transition<Float>(
                leafBlock,
                (a) -> leafBlock.renderer().setRenderableAngle(a),
                0f,
                5f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                rand.nextInt(7) + 3,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
    }

    private void initLeafFallTask(GameObject leafBlock, Vector2 originalLeafLocation) {
        leafBlock.renderer().setOpaqueness(1);
        leafBlock.setTopLeftCorner(originalLeafLocation);

        new ScheduledTask(
            leafBlock,
            rand.nextInt(60) + 5,
            false,
            () -> {
                Transition<Float> verticalTransition = initLeafVerticalFallTransition(leafBlock);
                leafBlock.renderer().fadeOut(FADEOUT_TIME, () -> {
                    leafBlock.transform().setVelocity(0, 0);
                    leafBlock.removeComponent(verticalTransition);
                    initLeafAfterlifeWaitTask(
                            leafBlock,
                            originalLeafLocation,
                            rand.nextInt(5));
                });
            });
    }

    private void initLeafAfterlifeWaitTask(GameObject leafBlock, Vector2 originalLeafLocation, int afterlifeTime) {
        new ScheduledTask(
            leafBlock,
            afterlifeTime,
            false,
            () -> initLeafFallTask(leafBlock, originalLeafLocation));
    }

    private Transition<Float> initLeafVerticalFallTransition(GameObject leafBlock) {
        return new Transition<Float>(
                leafBlock,
                (val) -> {
                    if(val < 3) {
                        leafBlock.transform().setVelocity(20, 25);
                    }
                    if(val > 7) {
                        leafBlock.transform().setVelocity(-20, 25);
                    }
                },
                0f,
                10f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                rand.nextInt(5) + 3,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
    }
}
