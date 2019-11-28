package com.mcmoddev.lib.properties;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class MMDEntityPropertyBase extends IForgeRegistryEntry.Impl<MMDEntityPropertyBase> implements IMMDEntityProperty<EntityLiving> {

	private final ImmutableSet<ResourceLocation> handledEntities;
	private final ImmutableSet<ListenerType> listenerTypes;
	
	public MMDEntityPropertyBase() {
		final String modid = Loader.instance().activeModContainer().getModId().concat(":");
		final ImmutableSet.Builder<ResourceLocation> builder = new ImmutableSet.Builder();
		for(final String s : getHandledEntities()) {
			builder.add(new ResourceLocation(modid, s));
		}
		handledEntities = builder.build();
		listenerTypes = Sets.immutableEnumSet(getListenerTypes());
	}
	
	@Override
	public boolean isListenerFor(final ResourceLocation entity, final ListenerType listenerType) {
		return handledEntities.contains(entity) && listenerTypes.contains(listenerType);
	}
	
	/**
	 * @return the names of entities that should be handled.
	 **/
	public abstract Set<String> getHandledEntities();

	/**
	 * @return the events that this property should receive and handle
	 * @see IMMDEntityProperty.ListenerType
	 **/
	public abstract Set<ListenerType> getListenerTypes();
	
}
