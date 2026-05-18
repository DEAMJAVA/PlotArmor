package net.deamjava.plotarmor

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permissions
import org.slf4j.LoggerFactory

object PlotArmor : ModInitializer {
	private val logger = LoggerFactory.getLogger("plot-armor")

	override fun onInitialize() {
		logger.info("PlotArmor initializing!")
		PlotArmorData.init(FabricLoader.getInstance().configDir.toFile())
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			dispatcher.register(
				Commands.literal("plotarmor")
					.requires { it.permissions().hasPermission(Permissions.COMMANDS_ADMIN) }
					.then(
						Commands.literal("add")
							.then(
								Commands.argument("player", EntityArgument.player())
									.suggests { ctx, builder ->
										// Suggest players NOT already armored
										val server = ctx.source.server
										server.playerList.players
											.filter { !PlotArmorData.isArmored(it.uuid) }
											.forEach { builder.suggest(it.gameProfile.name) }
										builder.buildFuture()
									}
									.executes { ctx -> executeAdd(ctx) }
							)
					)
					.then(
						Commands.literal("remove")
							.then(
								Commands.argument("player", EntityArgument.player())
									.suggests { ctx, builder ->
										// Suggest only armored players
										val server = ctx.source.server
										server.playerList.players
											.filter { PlotArmorData.isArmored(it.uuid) }
											.forEach { builder.suggest(it.gameProfile.name) }
										builder.buildFuture()
									}
									.executes { ctx -> executeRemove(ctx) }
							)
					)
					.then(
						Commands.literal("list")
							.executes { ctx -> executeList(ctx) }
					)
			)
		}
	}

	private fun executeAdd(ctx: CommandContext<CommandSourceStack>): Int {
		val player: ServerPlayer = EntityArgument.getPlayer(ctx, "player")
		return if (PlotArmorData.add(player.uuid)) {
			ctx.source.sendSuccess(
				{ Component.literal("${player.gameProfile.name} now has plot armor.") },
				true
			)
			1
		} else {
			ctx.source.sendFailure(
				Component.literal("${player.gameProfile.name} already has plot armor.")
			)
			0
		}
	}

	private fun executeRemove(ctx: CommandContext<CommandSourceStack>): Int {
		val player: ServerPlayer = EntityArgument.getPlayer(ctx, "player")
		return if (PlotArmorData.remove(player.uuid)) {
			ctx.source.sendSuccess(
				{ Component.literal("${player.gameProfile.name} no longer has plot armor.") },
				true
			)
			1
		} else {
			ctx.source.sendFailure(
				Component.literal("${player.gameProfile.name} does not have plot armor.")
			)
			0
		}
	}

	private fun executeList(ctx: CommandContext<CommandSourceStack>): Int {
		val uuids = PlotArmorData.getAll()
		if (uuids.isEmpty()) {
			ctx.source.sendSuccess({ Component.literal("No players have plot armor.") }, false)
			return 0
		}
		val server = ctx.source.server
		val names = uuids.joinToString(", ") { uuid ->
			server.playerList.getPlayer(uuid)?.gameProfile?.name ?: uuid.toString()
		}
		ctx.source.sendSuccess({ Component.literal("Plot armored players: $names") }, false)
		return uuids.size
	}
}