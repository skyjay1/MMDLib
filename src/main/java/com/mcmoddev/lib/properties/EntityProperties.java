package com.mcmoddev.lib.properties;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class EntityProperties {
	private static final IForgeRegistry<MMDEntityPropertyBase> REGISTRY = new RegistryBuilder<MMDEntityPropertyBase>()
			.allowModification().setIDRange(0, 8192).setName(new ResourceLocation("mmdlib", "entity_properties"))
			.setMaxID(8192).setType(MMDEntityPropertyBase.class).create();
		
	public static IForgeRegistry<MMDEntityPropertyBase> getRegistry() {
		return REGISTRY;
	}
	
	public static List<IMMDEntityProperty<EntityLiving>> getListeners(final ResourceLocation name, final IMMDEntityProperty.ListenerType type) {		
		return REGISTRY.getValuesCollection().stream()
			.filter(p -> p.isListenerFor(name, type))
			.collect(Collectors.toList());
	}
}
