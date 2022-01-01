package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Represents Avatar object. Takes care of all avatar's properties and features.
 * @author Yonatan Chocron
 */
public class Avatar extends GameObject {

    // CONSTANTS
    private static final String STAND_IMAGE_PATH = "src/pepse/assets/avatarStand.png";
    private static final String WALK_RIGHT_IMAGE_PATH = "src/pepse/assets/avatarWalkRight.png";
    private static final String WALK_LEFT_IMAGE_PATH = "src/pepse/assets/avatarWalkLeft.png";
    private static final float AVATAR_ANIMATION_DELTA_TIME = 0.2f;
    private static final int VELOCITY_X = 300;
    private static final int VELOCITY_JUMP = 300;
    private static final int GRAVITY = 300;
    private static final Vector2 DIMENSIONS = new Vector2(50,50);
    private static final int MAX_ENERGY = 100;
    private static final String ENERGY_STRING = "Energy: %d";
    private static final Vector2 ENERGY_COUNTER_POS = new Vector2(30,30);
    private static final Vector2 ENERGY_COUNTER_DIMENSIONS = new Vector2(100,30);

    // FIELDS
    private static Renderable standingRenderable;
    private static Renderable walkingRenderable;

    private final UserInputListener inputListener;
    private final GameObjectCollection gameObjects;
    private float energy = MAX_ENERGY;
    private TextRenderable energyRenderable;


    /**
     * Construct a new Avatar instance.
     * @param inputListener inputListener instance
     */
    public Avatar(UserInputListener inputListener, GameObjectCollection gameObjects) {
        super(Vector2.ZERO, DIMENSIONS, new RectangleRenderable(Color.BLACK));
        this.gameObjects = gameObjects;
        this.inputListener = inputListener;
    }

    /**
     * Creates new Avatar.
     * @param gameObjects gameObjects
     * @param layer layer
     * @param topLeftCorner initial position of the avatar
     * @param inputListener inputListener instance
     * @param imageReader imageReader instance
     * @return Avatar instance defined according to supplied parameters
     */
    public static Avatar create(GameObjectCollection gameObjects,
                                int layer,
                                Vector2 topLeftCorner,
                                UserInputListener inputListener,
                                ImageReader imageReader){


        createRenderables(imageReader);

        Avatar avatar = new Avatar(inputListener, gameObjects);
        avatar.setTopLeftCorner(topLeftCorner);
        avatar.transform().setAccelerationY(GRAVITY);
        avatar.renderer().setRenderable(standingRenderable);
        avatar.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        avatar.createEnergyCounter();
        gameObjects.addGameObject(avatar, layer);

        return avatar;

    }

    /**
     * creates all avatar's renderables
     * @param imageReader
     */
    private static void createRenderables(ImageReader imageReader) {

        // standing
        standingRenderable = imageReader.readImage(STAND_IMAGE_PATH, true);

        // walking
        Renderable walkLeft = imageReader.readImage(WALK_LEFT_IMAGE_PATH, true);
        Renderable walkRight = imageReader.readImage(WALK_RIGHT_IMAGE_PATH, true);
        walkingRenderable = new AnimationRenderable(new Renderable[] {walkLeft, walkRight}, AVATAR_ANIMATION_DELTA_TIME);

    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // Update energy renderer
        energyRenderable.setString(String.format(ENERGY_STRING, (int) energy));

        // Conduct avatar possible operations
        walk();
        fly();
        jump();
    }

    /**
     * Takes care of jumping logic, including listening to the user's input (will jump only if jump key was
     * pressed)
     */
    private void jump() {
        if (    inputListener.isKeyPressed(KeyEvent.VK_SPACE)
                && transform().getVelocity().y() == 0){
            transform().setVelocityY(-VELOCITY_JUMP);  // minus since value should be negative
        }
    }

    /**
     * Takes care of flying logic, including listening to the user's input (will fly only if fly keys were
     * pressed and all other conditions (enough energy) are met).
     */
    private void fly() {
        if (    inputListener.isKeyPressed(KeyEvent.VK_SPACE) &&
                inputListener.isKeyPressed(KeyEvent.VK_SHIFT) &&
                energy > 0){
            transform().setVelocityY(-VELOCITY_JUMP);  // minus since value should be negative
            renderer().setRenderableAngle(-80);
            energy -= 0.5;  // reduce energy while flying
        }
        else { renderer().setRenderableAngle(0); }

        // charge energy while on the ground
        if (energy < MAX_ENERGY && transform().getVelocity().y() == 0){
            energy += 0.5;
        }

    }

    /**
     * Takes care of walking logic, including listening to the user's input (will walk only if walking keys
     * were pressed)
     */
    private void walk() {
        float xVel = 0;
        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)){
            xVel -= VELOCITY_X;
            renderer().setIsFlippedHorizontally(true);
        }
        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)){
            xVel += VELOCITY_X;
            renderer().setIsFlippedHorizontally(false);
        }

        if (xVel != 0){
            renderer().setRenderable(walkingRenderable);
        } else { renderer().setRenderable(standingRenderable); }

        transform().setVelocityX(xVel);
    }

    /**
     * creates energy counter
     */
    private void createEnergyCounter(){

        this.energyRenderable = new TextRenderable(String.format(ENERGY_STRING, (int) energy));
        GameObject energyCounter = new GameObject(ENERGY_COUNTER_POS,
                ENERGY_COUNTER_DIMENSIONS,
                energyRenderable);
        energyCounter.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects.addGameObject(energyCounter, Layer.UI);
    }
}
