package com.stanislav.game.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Stanislav on 17.03.2016.
 */
public class Fireball extends Sprite {

    private Vector3 position;
    private Vector3 velocity;
    private static final int MOVEMENT = 1000;
    private Rectangle bounds;
    private boolean myShot;

    public Fireball(Texture texture, float xPos, float yPos, boolean myShot){
        super(texture);
        setPosition(xPos, yPos);
        this.myShot = myShot;
        bounds = new Rectangle(xPos,yPos,texture.getWidth(), texture.getHeight());
    }

    public void update(float dt) {
        //Gdx.app.log("MyTag", "DeltaTime calculation: " + dt);
        float y = getY() + MOVEMENT * dt;
        setPosition(getX(), y);
        bounds.setPosition(getX(), y);
    }

    public boolean firedByMe(){
        return myShot;
    }


    public void dispose(){

    }

}
