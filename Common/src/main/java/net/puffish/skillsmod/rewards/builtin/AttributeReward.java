package net.puffish.skillsmod.rewards.builtin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.rewards.Reward;
import net.puffish.skillsmod.api.rewards.RewardContext;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AttributeReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("attribute");

	private final List<UUID> uuids = new ArrayList<>();

	private final EntityAttribute attribute;
	private final float value;
	private final EntityAttributeModifier.Operation operation;

	private AttributeReward(EntityAttribute attribute, float value, EntityAttributeModifier.Operation operation) {
		this.attribute = attribute;
		this.value = value;
		this.operation = operation;
	}

	public static void register() {
		SkillsAPI.registerRewardWithData(
				ID,
				AttributeReward::create
		);
	}

	private static Result<AttributeReward, Failure> create(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(AttributeReward::create);
	}

	private static Result<AttributeReward, Failure> create(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optAttribute = rootObject.get("attribute")
				.andThen(attributeElement -> JsonParseUtils.parseAttribute(attributeElement)
						.andThen(attribute -> {
							if (DefaultAttributeRegistry.get(EntityType.PLAYER).has(attribute)) {
								return Result.success(attribute);
							} else {
								return Result.failure(attributeElement.getPath().failureAt("Expected a valid player attribute"));
							}
						})
				)
				.ifFailure(failures::add)
				.getSuccess();

		var optValue = rootObject.getFloat("value")
				.ifFailure(failures::add)
				.getSuccess();

		var optOperation = rootObject.get("operation")
				.andThen(JsonParseUtils::parseAttributeOperation)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new AttributeReward(
					optAttribute.orElseThrow(),
					optValue.orElseThrow(),
					optOperation.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	private void createMissingUUIDs(int count) {
		while (uuids.size() < count) {
			uuids.add(UUID.randomUUID());
		}
	}

	@Override
	public void update(ServerPlayerEntity player, RewardContext context) {
		var count = context.count();
		var instance = Objects.requireNonNull(player.getAttributeInstance(attribute));

		createMissingUUIDs(count);

		for (int i = 0; i < uuids.size(); i++) {
			var uuid = uuids.get(i);
			if (instance.getModifier(uuid) == null) {
				if (i < count) {
					instance.addTemporaryModifier(new EntityAttributeModifier(
							uuid,
							"",
							value,
							operation
					));
				}
			} else {
				if (i >= count) {
					instance.removeModifier(uuid);
				}
			}
		}
	}

	@Override
	public void dispose(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			var instance = Objects.requireNonNull(player.getAttributeInstance(attribute));
			for (UUID uuid : uuids) {
				instance.removeModifier(uuid);
			}
		}
		uuids.clear();
	}
}
