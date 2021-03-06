package com.stanislav.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.stanislav.game.MultiplayerSpace;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//config.useHDPI = true;
		config.height = 1000;
		config.width = 1000;
		new LwjglApplication(new MultiplayerSpace(), config);
	}
}
