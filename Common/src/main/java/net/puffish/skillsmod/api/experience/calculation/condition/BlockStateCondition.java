package net.puffish.skillsmod.api.experience.calculation.condition;

import net.minecraft.block.BlockState;
import net.minecraft.predicate.StatePredicate;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.ArrayList;

public final class BlockStateCondition implements Condition<BlockState> {
	private final StatePredicate state;

	public BlockStateCondition(StatePredicate state) {
		this.state = state;
	}

	public static ConditionFactory<BlockState> factory() {
		return ConditionFactory.withData(BlockStateCondition::parse);
	}

	public static Result<BlockStateCondition, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(BlockStateCondition::parse);
	}

	public static Result<BlockStateCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optState = rootObject.get("state")
				.andThen(JsonParseUtils::parseStatePredicate)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new BlockStateCondition(
					optState.orElseThrow()
			));
		} else {
			return Result.failure(ManyFailures.ofList(failures));
		}
	}

	@Override
	public boolean test(BlockState blockState) {
		return state.test(blockState);
	}
}
