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
    private final Color rootColor = new Color(100, 50, 20);
    private final Color leafColor = new Color(50, 200, 30);
    private final int FADEOUT_TIME = 10; // TODO set to 15
    private final GameObjectCollection gameObjects;
    private final Vector2 windowDimensions;
    private final Terrain terrain;
    private final Random rand = new Random();

    // TODO: is this the correct way to get the game objects?
    public Tree(GameObjectCollection gameObjects, Vector2 windowDimensions, Terrain terrain) {
        this.gameObjects = gameObjects;
        this.windowDimensions = windowDimensions;
        this.terrain = terrain;
    }

    public void createInRange(int minX, int maxX) {
//        int initialNumOfTrees = rand.nextInt(1,6);
        int initialNumOfTrees = rand.nextInt(5) + 1;
        // TODO:  make sure indexes don't repeat
        for (int i = 0; i < initialNumOfTrees; i++) {
//            int treeLocation = rand.nextInt(minX, maxX);
            int treeLocation = rand.nextInt(maxX - minX) + minX;
//            int treeHeight = rand.nextInt(4, 12);
            int treeHeight = rand.nextInt(8) + 4;
            this.create(treeLocation, treeHeight);
        }
    }

    public void create(int treeLocation, int treeHeight) {
        int groundHeight = (int) (terrain.groundHeightAt(treeLocation)/30)*30 - 30; // TODO: use block sze instead of
        System.out.println(groundHeight);
        // add root
        for (int i = 0; i < treeHeight; i++) { //TODO: tree height?
            GameObject rootBlock = new GameObject(
                    new Vector2(treeLocation, groundHeight - (i* Block.SIZE)),
                    new Vector2(Block.SIZE, Block.SIZE),
                    new RectangleRenderable(pepse.util.ColorSupplier.approximateColor(rootColor, 10)
            ));
            rootBlock.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
            rootBlock.setTag("rootBlock");
            gameObjects.addGameObject(rootBlock);
        }

        // add leaves
        int leavesSize = (int) (2*treeHeight/3)*Block.SIZE;
        int leavesCol = treeLocation - leavesSize/2;
        if(leavesCol < 0) { //TODO: handle that case better
            leavesCol = 0;
        }
        int leavesRow = treeHeight*Block.SIZE - leavesSize/2;

        for (int i = leavesCol; i <= leavesCol + leavesSize; i+=Block.SIZE) {
            for (int j = leavesRow; j <= leavesRow + leavesSize; j+=Block.SIZE) {
                Vector2 leafLocation = new Vector2(i, groundHeight - j);
                GameObject leafBlock = new GameObject(
                        leafLocation,
                        new Vector2(Block.SIZE, Block.SIZE),
                        new RectangleRenderable(pepse.util.ColorSupplier.approximateColor(leafColor, 20)
                    ));
                leafBlock.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
                leafBlock.setTag("leafBlock");


                new ScheduledTask(
                        leafBlock,
//                        rand.nextInt(1,20),
                        rand.nextInt(19) + 1,
                        true,
                        () -> {
                            new Transition<Float>(
                                    leafBlock,
                                    (a) -> leafBlock.renderer().setRenderableAngle(a),
                                    0f,
                                    5f,
                                    Transition.CUBIC_INTERPOLATOR_FLOAT,
//                                    rand.nextInt(3,10),
                                    rand.nextInt(7) + 3,
                                    Transition.TransitionType.TRANSITION_LOOP,
                                    null
                            );
                        });


                new ScheduledTask(
                        leafBlock,
//                        rand.nextInt(1,20),
                        rand.nextInt(19) + 1,
                        true,
                        () -> {
                            new Transition<Float>(
                                    leafBlock,
                                    (a) -> leafBlock.setDimensions(new Vector2(Block.SIZE+a, Block.SIZE+a)),
                                    -1f,
                                    4f,
                                    Transition.CUBIC_INTERPOLATOR_FLOAT,
//                                    rand.nextInt(3,10),
                                    rand.nextInt(7) + 3,
                                    Transition.TransitionType.TRANSITION_LOOP,
                                    null
                            );
                        });



                new ScheduledTask(
                        leafBlock,
//                        rand.nextInt(1,20),
                        rand.nextInt(19) + 1,
                        true,
                        () -> {
                            new Transition<Float>(
                                    leafBlock,
                                    (a) -> leafBlock.setDimensions(new Vector2(Block.SIZE+a, Block.SIZE+a)),
                                    -1f,
                                    4f,
                                    Transition.CUBIC_INTERPOLATOR_FLOAT,
//                                    rand.nextInt(3,10),
                                    rand.nextInt(7) + 3,
                                    Transition.TransitionType.TRANSITION_LOOP,
                                    null
                            );
                        });

                // TODO: decide on the range for fade
                initLeafFallTask(leafBlock, leafLocation);

                gameObjects.addGameObject(leafBlock);
            }
        }
    }

    private void initLeafFallTask(GameObject leafBlock, Vector2 leafLocation) {
        leafBlock.renderer().setOpaqueness(1);
        leafBlock.setTopLeftCorner(leafLocation);
//        int FADEOUT_START = rand.nextInt(5,100); //TODO start form 10 to 100
        int FADEOUT_START = rand.nextInt(95) + 5; //TODO start form 10 to 100

        new ScheduledTask(
            leafBlock,
            FADEOUT_START,
            false,
            () -> {
                leafBlock.renderer().fadeOut(FADEOUT_TIME, () -> {
//                    int AFTERLIFE = rand.nextInt(2,5); //TODO: 10-40
                    int AFTERLIFE = rand.nextInt(3); //TODO: 10-40
                    initLeafAfterlifeWaitTask(leafBlock, leafLocation, AFTERLIFE);
                    leafBlock.transform().setVelocity(0, 0);
                });
                leafBlock.transform().setVelocityY(30);
                //TODO: schedule task before
                new ScheduledTask(
                        leafBlock,
                        3,
                        false,
                        () -> {
                            initLeafVerticalFallTransition(leafBlock);
                        });
            });
    }

    private void initLeafAfterlifeWaitTask(GameObject leafBlock, Vector2 leafLocation, int afterlifeTime) {
        new ScheduledTask(
            leafBlock,
            afterlifeTime,
            false,
            () -> {
                initLeafFallTask(leafBlock, leafLocation);
            });
    }

    private void initLeafVerticalFallTransition(GameObject leafBlock) {
        new Transition<Float>(
                leafBlock,
                (a) -> {
                    // TODO: fix velocity not updating
                    if(a < 3) {
                        leafBlock.transform().setVelocity(15, 30);
                    }
                    if(a > 8) {
                        leafBlock.transform().setVelocity(-15, 30);
                    }
                },
                0f,
                10f,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
//                rand.nextInt(1,4),
                rand.nextInt(3) + 1,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
    }

}
