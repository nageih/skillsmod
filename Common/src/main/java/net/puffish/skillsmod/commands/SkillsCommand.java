package net.puffish.skillsmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.puffish.skillsmod.utils.CommandUtils;

public class SkillsCommand {
	public static LiteralArgumentBuilder<ServerCommandSource> create() {
		return CommandManager.literal("skills")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("unlock")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", IdentifierArgumentType.identifier())
										.then(CommandManager.argument("skill", StringArgumentType.string())
												.executes(context -> {
													var players = EntityArgumentType.getPlayers(context, "players");
													var categoryId = IdentifierArgumentType.getIdentifier(context, "category");
													var skillId = StringArgumentType.getString(context, "skill");

													var category = CommandUtils.getCategory(categoryId);
													var skill = CommandUtils.getSkill(category, skillId);

													for (var player : players) {
														skill.unlock(player);
													}
													return CommandUtils.sendSuccess(
															context,
															players,
															"skills.unlock",
															categoryId,
															skillId
													);
												})
										)
								)
						)
				)
				.then(CommandManager.literal("reset")
						.then(CommandManager.argument("players", EntityArgumentType.players())
								.then(CommandManager.argument("category", IdentifierArgumentType.identifier())
										.executes(context -> {
											var players = EntityArgumentType.getPlayers(context, "players");
											var categoryId = IdentifierArgumentType.getIdentifier(context, "category");

											var category = CommandUtils.getCategory(categoryId);

											for (var player : players) {
												category.resetSkills(player);
											}
											return CommandUtils.sendSuccess(
													context,
													players,
													"skills.reset",
													categoryId
											);
										})
								)
						)
				);
	}
}
