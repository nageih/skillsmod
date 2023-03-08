package net.puffish.skillsmod.server.event;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface ServerRegistrar {
	<V, T extends V> void register(Registry<V> registry, Identifier id, T entry);
}
