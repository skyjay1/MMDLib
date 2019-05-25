package com.mcmoddev.lib.init;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mcmoddev.lib.data.SharedStrings;
import com.mcmoddev.lib.entity.GolemContainer;
import com.mcmoddev.lib.material.MMDMaterial;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class Entities {
		
	private static final Map<MMDMaterial, GolemContainer> customGolems = new HashMap<>();
	private static final Collection<EntityEntry> entitiesToRegister = new HashSet<>();
	private static int entityId = 0;
	
	protected Entities() {
		throw new IllegalAccessError(SharedStrings.NOT_INSTANTIABLE);
	}
	
	protected static <E extends Entity> void add(@Nonnull final Class<E> entityClass, @Nonnull final String name) {
		entitiesToRegister.add(build(entityClass, name));
		// TODO we need to register render class too, somehow, without breaking the server
	}
	
	protected static <E extends Entity> EntityEntry build(@Nonnull final Class<E> entityClass, @Nonnull final String name) {
		EntityEntryBuilder builder = EntityEntryBuilder.<E>create();
		builder.entity(entityClass);
		builder.name("mmdlib.".concat(name));
		builder.id(new ResourceLocation("mmdlib", name), entityId++);
		// trackingRange=48; updateFrequency=3; sendVelocityUpdates=true
		builder.tracker(48, 3, true);
		return builder.build();
	}
	
	/**
	 * Registers information about a golem with the Material associated
	 * with the given GolemContainer.
	 * @param container Obtained using {@link GolemContainer.Builder}
	 **/
	protected static void addGolem(final GolemContainer container) {
		final MMDMaterial mat = container.getMMDMaterial();
		if(null == mat) {
			com.mcmoddev.lib.MMDLib.logger.warn("Skipping null MMDMaterial object in GolemContainer registration.");
			return;
		}
		if(customGolems.containsKey(mat)) {
			com.mcmoddev.lib.MMDLib.logger.warn("Cannot register GolemContainer for MMDMaterial '%s' as it has already been registered.", mat.getName());
		}
		customGolems.put(mat, container);
		
	}
	
	public static Map<MMDMaterial, GolemContainer> getGolemRegistry() {
		return customGolems;
	}
	
	public static Collection<EntityEntry> getAllEntries() {
		return entitiesToRegister;
	}
	
	@Nullable
	public static GolemContainer getContainer(final MMDMaterial mat) {
		return customGolems.get(mat);
	}
	
	
}
