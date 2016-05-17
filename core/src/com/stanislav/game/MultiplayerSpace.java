package com.stanislav.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.stanislav.game.sprites.EnemyShip;
import com.stanislav.game.sprites.Fireball;
import com.stanislav.game.sprites.Starship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MultiplayerSpace extends ApplicationAdapter implements InputProcessor {
    private final float UPDATE_TIME = 1 / 60f;
    float timer;
    private SpriteBatch batch;
    private Socket socket;
    private String id;
    private Starship player;
    private Texture playerShip;
    private Texture friendlyShip;
    private Texture fireballTexture;
    private Texture enemyTexture;
    private List<Fireball> fireballs;
    private List<EnemyShip> enemyShips;
    private HashMap<String, Starship> friendlyPlayers;
    private BitmapFont font;
    private int highscore;

    @Override
    public void create() {
        batch = new SpriteBatch();
        playerShip = new Texture("playerShip2.png");
        friendlyShip = new Texture("playerShip.png");
        fireballTexture = new Texture("fireball.png");
        enemyTexture = new Texture("enemyShip.png");
        fireballs = new CopyOnWriteArrayList<Fireball>();
        enemyShips = new CopyOnWriteArrayList<EnemyShip>();
        friendlyPlayers = new HashMap<String, Starship>();
        //font = new BitmapFont();
        //font.setColor(Color.WHITE);
        //font.getData().setScale(10, 10);


        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 60;
        font = generator.generateFont(parameter);


        highscore = 0;
        connectSocket();

        getConfigSocketEvents();
        Gdx.input.setInputProcessor(this);
    }

    public void getConfigSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected");
                player = new Starship(playerShip);
            }
        }).on("socketID", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
                    Gdx.app.log("SocketIO", "My ID: " + id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting ID");
                }
            }
        }).on("newPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerId = data.getString("id");
                    Gdx.app.log("SocketIO", "New Player Connected: " + playerId);
                    friendlyPlayers.put(playerId, new Starship(friendlyShip));
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting New PlayerID");
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
                    friendlyPlayers.remove(id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting playerDisconnected");
                }
            }
        }).on("playerMoved", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String playerId = data.getString("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    if (friendlyPlayers.get(playerId) != null) {
                        friendlyPlayers.get(playerId).setPosition(x.floatValue(), y.floatValue());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("enemiesMoved", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                try {
                    JSONArray data = (JSONArray) args[0];
                    enemyShips.clear();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject enemyData = data.getJSONObject(i);
                        Integer id = enemyData.getInt("id");
                        Double x = enemyData.getDouble("x");
                        Double y = enemyData.getDouble("y");
                        EnemyShip enemyShip = new EnemyShip(enemyTexture, id, x.floatValue(), y.floatValue());
                        enemyShips.add(enemyShip);
                        //Gdx.app.log("MyTag", "Enemy "+enemyData.getDouble("id")+ " x Position: "+ enemyData.getDouble("x"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("shotFired", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    Fireball fireball = new Fireball(fireballTexture, x.floatValue(), y.floatValue() + 1000 * 0.150f, false);
                    fireballs.add(fireball);
                    Gdx.app.log("MyTag", "Received: " + new Date().getTime());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("enemyDead", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    Boolean enemyDead = data.getBoolean("enemyDead");
                    if (enemyDead) {
                        highscore++;
                    }
                    Gdx.app.log("MyTag", "Enemy Dead: " + new Date().getTime());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("getPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray objects = (JSONArray) args[0];
                try {
                    for (int i = 0; i < objects.length(); i++) {
                        Starship coopPlayer = new Starship(friendlyShip);
                        Vector2 position = new Vector2();
                        position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                        position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                        coopPlayer.setPosition(position.x, position.y);
                        friendlyPlayers.put(objects.getJSONObject(i).getString("id"), coopPlayer);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void handleInput(float dt) {
        if (player != null) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                player.setPosition(player.getX() + (-200 * dt), player.getY());
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                player.setPosition(player.getX() + (+200 * dt), player.getY());
            }
        }
    }

    public void updateServer(float dt) {
        timer += dt;
        if (timer >= UPDATE_TIME && player != null && player.hasMoved()) {
            JSONObject data = new JSONObject();
            try {
                data.put("x", player.getX());
                data.put("y", player.getY());
                socket.emit("playerMoved", data);
            } catch (JSONException e) {
                Gdx.app.log("SOCKET.IO", "Error sending update data");
            }
        }
    }

    @Override
    public void render() {
        handleInput(Gdx.graphics.getDeltaTime());
        updateServer(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        handleInput(Gdx.graphics.getDeltaTime());

        update(Gdx.graphics.getDeltaTime());

        batch.begin();
        if (player != null) {
            player.draw(batch);
        }
        for (HashMap.Entry<String, Starship> entry : friendlyPlayers.entrySet()) {
            entry.getValue().draw(batch);
        }
        for (Fireball fireball : fireballs) {
            fireball.draw(batch);
        }
        for (EnemyShip enemyShip : enemyShips) {
            enemyShip.draw(batch);
        }
        font.draw(batch, String.valueOf(highscore), 1500, 1000);
        //font.draw(batch, String.valueOf(highscore), 500, 500);
        batch.end();
    }

    private void update(float dt) {
        if (fireballs != null && fireballs.size() > 0)
            synchronized (fireballs) {
                for (Fireball fireball : fireballs) {
                    if (fireball.getY() > 600) {
                        fireballs.remove(fireball);
                        Gdx.app.log("MyTag", "fireball removed");
                        Gdx.app.log("MyTag", fireballs.size() + " fireballs in list");
                    }
                    fireball.update(dt);
                }
            }
        try {
            synchronized (enemyShips) {
                for (int i = 0; i < enemyShips.size(); i++) {
                    EnemyShip enemyShip = enemyShips.get(i);
                    for (Fireball fireball : fireballs) {
                        if (enemyShip.collides(fireball.getBoundingRectangle())) {
                            enemyShips.remove(enemyShip);
                            JSONObject data = new JSONObject();
                            data.put("id", enemyShip.getId());
                            if (fireball.firedByMe())
                                socket.emit("enemyDestroyed", data);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        playerShip.dispose();
        friendlyShip.dispose();
        fireballTexture.dispose();
        enemyTexture.dispose();
        font.dispose();
    }

    public void connectSocket() {
        try {
            //socket = IO.socket("http://localhost:8080");
            socket = IO.socket("http://multiplayer-macrosystems.rhcloud.com");
            socket.connect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        player.setPosition(screenX, player.getY());
        Fireball fireball = new Fireball(fireballTexture, player.getX() + 25, player.getY() + 80, true);
        fireballs.add(fireball);
        try {
            JSONObject data = new JSONObject();
            data.put("x", player.getX() + 25);
            data.put("y", player.getY() + 80);
            socket.emit("shotFired", data);
            Gdx.app.log("MyTag", "Fired: " + new Date().getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        player.setPosition(screenX, player.getY());
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
