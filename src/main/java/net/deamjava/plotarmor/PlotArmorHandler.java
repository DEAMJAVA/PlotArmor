package net.deamjava.plotarmor;

import net.minecraft.server.level.ServerPlayer;

public final class PlotArmorHandler {
	private PlotArmorHandler() {}

	public static void saveFromDeath(ServerPlayer player) {
		player.setHealth(1.0F);
		player.invulnerableTime = 20;
		player.deathTime = 0;
	}
}
