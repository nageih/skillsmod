package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;

public final class BlockTagCondition implements Condition<BlockState> {
	private final RegistryEntryList.Named<Block> entries;

	private BlockTagCondition(RegistryEntryList.Named<Block> entries) {
		this.entries = entries;
	}

	public static ConditionFactory<BlockState> factory() {
		return ConditionFactory.withData(BlockTagCondition::parse);
	}

	public static Result<BlockTagCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(BlockTagCondition::parse);
	}

	public static Result<BlockTagCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.get("tag")
				.andThen(JsonParseUtils::parseBlockTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new BlockTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(BlockState blockState) {
		return blockState.isIn(entries);
	}
}
