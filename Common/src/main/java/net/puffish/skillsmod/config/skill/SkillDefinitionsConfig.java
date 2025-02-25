package net.puffish.skillsmod.config.skill;

import net.minecraft.server.MinecraftServer;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.failure.Failure;
import net.puffish.skillsmod.api.utils.failure.ManyFailures;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SkillDefinitionsConfig {
	private final Map<String, SkillDefinitionConfig> definitions;

	private SkillDefinitionsConfig(Map<String, SkillDefinitionConfig> definitions) {
		this.definitions = definitions;
	}

	public static Result<SkillDefinitionsConfig, Failure> parse(JsonElementWrapper rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<SkillDefinitionsConfig, Failure> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		return rootObject.getAsMap((id, element) -> SkillDefinitionConfig.parse(id, element, context))
				.<Failure>mapFailure(ManyFailures::ofMapValues)
				.mapSuccess(SkillDefinitionsConfig::new);
	}

	public Optional<SkillDefinitionConfig> getById(String id) {
		return Optional.ofNullable(definitions.get(id));
	}

	public Collection<SkillDefinitionConfig> getAll() {
		return definitions.values();
	}

	public void dispose(MinecraftServer server) {
		for (var definition : definitions.values()) {
			definition.dispose(server);
		}
	}
}
