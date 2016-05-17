package com.stanislav.game.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by Stanislav on 19.03.2016.
 */
public class EnemyShip extends Sprite {

    private int id;
    private Rectangle bounds;

    public EnemyShip(Texture texture, int id, float x, float y){
        super(texture);
        this.id = id;
        setPosition(x, y);
        bounds = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public int getId() {
        return id;
    }

    public boolean collides(Rectangle fireball){
        return fireball.overlaps(bounds);
    }

}
