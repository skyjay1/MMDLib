package com.mcmoddev.lib.init;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mcmoddev.lib.data.SharedStrings;
import com.mcmoddev.lib.entity.AnimalContainer;
import com.mcmoddev.lib.entity.EntityContainer;
import com.mcmoddev.lib.entity.EntityCustomAnimal;
import com.mcmoddev.lib.entity.EntityCustomGolem;
import com.mcmoddev.lib.entity.GolemContainer;
import com.mcmoddev.lib.entity.MobContainer;
import com.mcmoddev.lib.material.MMDMaterial;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class Entities {
	
	/**
	 * A map to store the current number of Entity IDs used per Mod ID
	 **/
	private static final Map<String, Integer> entityIdMap = new HashMap<>();
	/* Basically... we add one registration for the custom entities we provide.
	 * Those are added both to the customEntities map and entitiesToRegister list.
	 * Users will only add to the customEntities map when using our custom entities.
	 */
	private static final Map<String, EntityContainer> customEntities = new HashMap<>();
	private static final Collection<EntityEntry> entitiesToRegister = new HashSet<>();
	
	public static final String PREFIX_GOLEM = "golem_";
	public static final String PREFIX_ANIMAL = "animal_";
	public static final String PREFIX_MOB = "mob_";
	
	
	static {
		add(addGolem(GolemContainer.Builder.create(Materials.EMPTY).build()).getEntityClass(), "customgolem");
		add(addAnimal(AnimalContainer.Builder.create("customanimal").build()).getEntityClass(), "customanimal");
		add(addMob(MobContainer.Builder.create("custommob").build()).getEntityClass(), "custommob");
		
	}
	
	protected Entities() {
		throw new IllegalAccessError(SharedStrings.NOT_INSTANTIABLE);
	}
	
	/**
	 * Add a custom {@code EntityLiving} to the game. This method
	 * requires that you have written a class that fulfills the
	 * requirements and would like to register it as-is.
	 * Different methods should be used if you are registering
	 * any of the following:
	 * <br>{@link EntityCustomAnimal} (use {@link #addAnimal(AnimalContainer)})
	 * <br>{@link EntityCustomMob} (use {@link #addMob(MobContainer)})
	 * <br>{@link EntityCustomGolem} (use {@link #addGolem(GolemContainer)})
	 * @param entityClass the Entity Class
	 * @param name a unique name to which your mod id will be added
	 * for entity registration.
	 **/
	protected static Class<? extends EntityLiving> add(final Class<? extends EntityLiving> entityClass, final String name) {
		entitiesToRegister.add(build(entityClass, name));
		return entityClass;
	}
	
	protected static <E extends Entity> EntityEntry build(final Class<E> entityClass, final String name) {
		final String modid = Loader.instance().activeModContainer().getModId();
		final int entityId = entityIdMap.getOrDefault(modid, 0);
		EntityEntryBuilder builder = EntityEntryBuilder.<E>create();
		builder.entity(entityClass);
		builder.name(modid.concat(".").concat(name));
		builder.id(new ResourceLocation(modid, name), entityId);
		// trackingRange=48; updateFrequency=3; sendVelocityUpdates=true
		builder.tracker(48, 3, true);
		entityIdMap.put(modid, entityId + 1);
		return builder.build();
	}
	
	/**
	 * Adds a {@link EntityCustomAnimal}, giving it stats and abilities
	 * as specified in the given {@link AnimalContainer}
	 * @param container Obtained using {@link AnimalContainer.Builder}
	 **/
	protected static AnimalContainer addAnimal(final AnimalContainer container) {
		final String key = PREFIX_ANIMAL.concat(container.getEntityName());
		if(null == key || key.isEmpty()) {
			com.mcmoddev.lib.MMDLib.logger.warn("Skipping invalid String name in AnimalContainer registration.");
		} else if(customEntities.containsKey(key)) {
			com.mcmoddev.lib.MMDLib.logger.warn("Cannot register AnimalContainer with name '%s' as it already exists.", key);
		} else {
			customEntities.put(key, container);
		}		
		return container;
	}	
	
	/**
	 * Adds a {@link EntityCustomMob}, giving it stats and abilities
	 * as specified in the given {@link MobContainer}
	 * @param container Obtained using {@link MobContainer.Builder}
	 **/
	protected static MobContainer addMob(final MobContainer container) {
		final String key = PREFIX_MOB.concat(container.getEntityName());
		if(null == key || key.isEmpty()) {
			com.mcmoddev.lib.MMDLib.logger.warn("Skipping invalid String name in MobContainer registration.");
		} else if(customEntities.containsKey(key)) {
			com.mcmoddev.lib.MMDLib.logger.warn("Cannot register MobContainer with name '%s' as it already exists.", key);
		} else {
			customEntities.put(key, container);
		}		
		return container;
	}
	
	/**
	 * Adds a {@link EntityCustomGolem} that can be built using the
	 * specified {@link MMDMaterial}, giving it stats and abilities
	 * as specified in the given {@link GolemContainer}
	 * @param container Obtained using {@link GolemContainer.Builder}
	 **/
	protected static GolemContainer addGolem(final GolemContainer container) {
		final String material = container.getMMDMaterial().getName();
		final String key = PREFIX_GOLEM.concat(material);
		if(customEntities.containsKey(key)) {
			com.mcmoddev.lib.MMDLib.logger.warn("Cannot register GolemContainer for MMDMaterial '%s' as it already exists.", material);
		} else {
			customEntities.put(key, container);
		}		
		return container;
	}
	
	public static Collection<EntityEntry> getEntriesToRegister() {
		return entitiesToRegister;
	}
	
	public static boolean hasEntityContainer(final String name) {
		return customEntities.containsKey(name);
	}
	
	@Nullable
	public static EntityContainer getEntityContainer(final String name) {
		return customEntities.get(name);
	}
}
