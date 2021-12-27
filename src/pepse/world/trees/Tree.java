package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.world.Block;

import java.awt.*;
import java.util.Random;

public class Tree {
    private final Color rootColor = new Color(100, 50, 20);
    private final Color leafColor = new Color(50, 200, 30);
    private final GameObjectCollection gameObjects;
    private final Vector2 windowDimensions;
    private final Random rand = new Random();

    // TODO: is this the correct way to get the game objects?
    public Tree(GameObjectCollection gameObjects, Vector2 windowDimensions) {
        this.gameObjects = gameObjects;
        this.windowDimensions = windowDimensions;
    }

    public void createInRange(int minX, int maxX) {
        int initialNumOfTrees = rand.nextInt(1,6);
        // TODO:  make sure indexes don't repeat
        for (int i = 0; i < initialNumOfTrees; i++) {
            int treeLocation = rand.nextInt(minX, maxX);
            int treeHeight = rand.nextInt(4, 12);
            this.create(treeLocation, treeHeight);
        }
    }

    public void create(int treeLocation, int treeHeight) {
        int groundHeight = (int) (windowDimensions.y() - 0); // TODO: Terrain.groundHeightAt
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
                Vector2 leafLocation = new Vector2(i, windowDimensions.y() - j);
                GameObject leafBlock = new GameObject(
                        leafLocation,
                        new Vector2(Block.SIZE, Block.SIZE),
                        new RectangleRenderable(pepse.util.ColorSupplier.approximateColor(leafColor, 20)
                    ));
                leafBlock.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
                leafBlock.setTag("leafBlock");

                //leaf movement in the wind
//                leafBlock.renderer().setRenderableAngle(10);



                // TODO: decide on the range for fade
//                int FADEOUT_TIME = rand.nextInt(1,100);
//                leafBlock.renderer().fadeOut(FADEOUT_TIME, () -> {
//                    leafBlock.setTopLeftCorner(leafLocation);
//                    leafBlock.renderer().setOpaqueness(1);
//                });
//                leafBlock.transform().setVelocityY(30);


                new ScheduledTask(
                        leafBlock,
                        rand.nextInt(1,10),
                        true,
                        () -> {
                            new Transition<Float>(
                                    leafBlock,
                                    (a) -> leafBlock.renderer().setRenderableAngle(a),
                                    0f,
                                    5f,
                                    Transition.CUBIC_INTERPOLATOR_FLOAT,
                                    rand.nextInt(3,10),
                                    Transition.TransitionType.TRANSITION_LOOP,
                                    null
                            );
                        });


                new ScheduledTask(
                        leafBlock,
                        rand.nextInt(1,10),
                        true,
                        () -> {
                            new Transition<Float>(
                                    leafBlock,
                                    (a) -> leafBlock.setDimensions(new Vector2(Block.SIZE+a, Block.SIZE+a)),
                                    0f,
                                    3f,
                                    Transition.CUBIC_INTERPOLATOR_FLOAT,
                                    rand.nextInt(3,10),
                                    Transition.TransitionType.TRANSITION_LOOP,
                                    null
                            );
                        });
                gameObjects.addGameObject(leafBlock);
            }
        }
    }
}
