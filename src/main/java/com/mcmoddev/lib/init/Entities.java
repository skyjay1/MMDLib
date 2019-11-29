package com.mcmoddev.lib.init;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mcmoddev.lib.data.MaterialNames;
import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.data.SharedStrings;
import com.mcmoddev.lib.entity.AnimalContainer;
import com.mcmoddev.lib.entity.EntityContainer;
import com.mcmoddev.lib.entity.EntityCustomAnimal;
import com.mcmoddev.lib.entity.EntityCustomGolem;
import com.mcmoddev.lib.entity.EntityCustomMob;
import com.mcmoddev.lib.entity.GolemContainer;
import com.mcmoddev.lib.entity.MobContainer;
import com.mcmoddev.lib.material.MMDMaterial;
import com.mcmoddev.lib.properties.MMDEntityPropertyBase;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class Entities {
	
	/**
	 * A map to store the current number of Entity IDs used per Mod ID
	 **/
	private static final Map<String, Integer> entityIdMap = new HashMap<>();
	/* Basically... we add one registration for the custom entities we provide.
	 * Those are added both to the customEntities map and entitiesToRegister list.
	 * Users will only add to the customEntities map when using our custom entities.
	 */
	private static final Map<ResourceLocation, EntityContainer> customEntities = new HashMap<>();
	private static final Collection<EntityEntry> entitiesToRegister = new HashSet<>();
	
	public static final String PREFIX_GOLEM = "golem_";
	
	private static boolean frozen = false;
	
	public static final void init() {
		if(frozen) {
			return;
		}
		add(addGolem(GolemContainer.EMPTY_GOLEM_CONTAINER).getEntityClass(), "customgolem");
		add(addAnimal(AnimalContainer.EMPTY_ANIMAL_CONTAINER).getEntityClass(), "customanimal");
		add(addMob(MobContainer.EMPTY_MOB_CONTAINER).getEntityClass(), "custommob");
		frozen = true;
		// DEBUG / TEST
		addGolem(GolemContainer.Builder.create(Materials.getMaterialByName(MaterialNames.GOLD)).build());
		//addAnimal(AnimalContainer.Builder.create("test_animal").build());
		//addMob(MobContainer.Builder.create("test_mob").build());
		
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
	protected static EntityEntry add(final Class<? extends EntityLiving> entityClass, final String name) {
		final EntityEntry entry = buildEntityEntry(entityClass, name);
		entitiesToRegister.add(entry);
		return entry;
	}
	
	private static final EntityEntry buildEntityEntry(final Class<? extends EntityLiving> entityClass, final String name) {
		final String modid = Loader.instance().activeModContainer().getModId();
		final int entityId = entityIdMap.getOrDefault(modid, 0);
		EntityEntryBuilder builder = EntityEntryBuilder.create();
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
	protected static final AnimalContainer addAnimal(final AnimalContainer container) {
		final ResourceLocation key = container.getEntityName();
		if(null == key || key.toString().isEmpty()) {
			com.mcmoddev.lib.MMDLib.logger.warn("Skipping invalid String name in AnimalContainer registration.");
		} else if(customEntities.containsKey(key)) {
			com.mcmoddev.lib.MMDLib.logger.warn("Cannot register AnimalContainer with name '" + key + "' as it already exists.");
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
	protected static final MobContainer addMob(final MobContainer container) {
		final ResourceLocation key = container.getEntityName();
		if(null == key || key.toString().isEmpty()) {
			com.mcmoddev.lib.MMDLib.logger.warn("Skipping invalid String name in MobContainer registration.");
		} else if(customEntities.containsKey(key)) {
			com.mcmoddev.lib.MMDLib.logger.warn("Cannot register MobContainer with name '" + key + "' as it already exists.");
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
	protected static final GolemContainer addGolem(final GolemContainer container) {
		final String material = container.getMMDMaterial().getName();
		final ResourceLocation key = container.getEntityName();
		if(customEntities.containsKey(key)) {
			com.mcmoddev.lib.MMDLib.logger.warn("Cannot register GolemContainer for MMDMaterial '" + material + "' as it already exists.");
		} else {
			customEntities.put(key, container);
			com.mcmoddev.lib.MMDLib.logger.info("Registered GolemContainer with key '" + key + "'");
		}		
		return container;
	}
	
	public static final Collection<EntityEntry> getEntriesToRegister() {
		return entitiesToRegister;
	}
	
	/**
	 * @return a list of all registered golem materials where
	 * the material has a valid, registered block.
	 **/
	public static final List<MMDMaterial> getGolemMaterials() {
		// DEBUG
		System.out.println("registered entities:\n" + Entities.customEntities);

		return customEntities.values().stream()
				.filter(e -> e instanceof GolemContainer)
				.map(e -> ((GolemContainer)e).getMMDMaterial())
				.filter(m -> m.hasBlock(Names.BLOCK))
				.collect(Collectors.toList());
	}
	
	public static final boolean hasEntityContainer(final ResourceLocation name) {
		return customEntities.containsKey(name);
	}
	
	@Nullable
	public static <E extends EntityContainer> E getEntityContainer(final ResourceLocation name) {
		EntityContainer cont = customEntities.get(name);
		if(cont != null) {
			return (E)cont;
		}
		return null;
	}
}
