package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.security.Key;

public class Avatar extends GameObject {

    private static final int VELOCITY_X = 300;
    private static final int VELOCITY_JUMP = 300;
    private static final int GRAVITY = 300;
    private static final Vector2 DIMENSIONS = new Vector2(50,50);
    private static final int MAX_ENERGY = 5;  // todo change to 100
    private static final String ENERGY_STRING = "Energy: %d";
    private static final Vector2 ENERGY_COUNTER_POS = new Vector2(30,30);
    private static final Vector2 ENERGY_COUNTER_DIMENSIONS = new Vector2(100,30);

    private final UserInputListener inputListener;
    private final GameObjectCollection gameObjects;

    private float energy = MAX_ENERGY;
    private TextRenderable energyRenderable;


    /**
     * Construct a new GameObject instance.
     * @param inputListener // todo
     */
    public Avatar(UserInputListener inputListener, GameObjectCollection gameObjects) { //todo inputListener in ctr??
        super(Vector2.ZERO, DIMENSIONS, new RectangleRenderable(Color.BLACK));
        this.gameObjects = gameObjects;
        this.inputListener = inputListener;
    }

    public static Avatar create(GameObjectCollection gameObjects,
                                int layer,
                                Vector2 topLeftCorner,
                                UserInputListener inputListener,
                                ImageReader imageReader){



//        Renderable avatarImage = imageReader.readImage() // todo

        Renderable avatarImage = new RectangleRenderable(Color.BLACK);

        Avatar avatar = new Avatar(inputListener, gameObjects);
        avatar.setTopLeftCorner(topLeftCorner);
        avatar.transform().setAccelerationY(GRAVITY);
        avatar.renderer().setRenderable(avatarImage);
        avatar.physics().preventIntersectionsFromDirection(Vector2.ZERO);

        avatar.createEnergyCounter();

        gameObjects.addGameObject(avatar, layer);

        return avatar;

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Update energy renderer
        energyRenderable.setString(String.format(ENERGY_STRING, (int) energy));

        // Walking commands
        float xVel = 0;
        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)){
            xVel -= VELOCITY_X;
        }
        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)){
            xVel += VELOCITY_X;
        }
        transform().setVelocityX(xVel);

        // Jumping & flying commands
        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE)){

            if (energy > 0 && inputListener.isKeyPressed(KeyEvent.VK_SHIFT)){
                transform().setVelocityY(-VELOCITY_JUMP);  // minus since value should be negative

                energy -= 0.5;
            }

            if (transform().getVelocity().y() == 0){
                transform().setVelocityY(-VELOCITY_JUMP);  // minus since value should be negative
            }

        }

        // Charge energy while not jumping / flying
        if (energy < MAX_ENERGY && transform().getVelocity().y() == 0){
            energy += 0.5;
        }

    }

    private void createEnergyCounter(){

        this.energyRenderable = new TextRenderable(String.format(ENERGY_STRING, (int) energy));
        GameObject energyCounter = new GameObject(ENERGY_COUNTER_POS, ENERGY_COUNTER_DIMENSIONS,
                energyRenderable);
        energyCounter.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects.addGameObject(energyCounter, Layer.UI);

    }

}
