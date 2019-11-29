package com.mcmoddev.lib.properties;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class MMDEntityPropertyBase extends IForgeRegistryEntry.Impl<MMDEntityPropertyBase> implements IMMDEntityProperty<EntityLiving> {

	private final ImmutableSet<ResourceLocation> handledEntities;
	private final ImmutableSet<ListenerType> listenerTypes;
	
	private MMDEntityPropertyBase(final ResourceLocation registryName) {
		handledEntities = ImmutableSet.copyOf(getHandledEntities());
		listenerTypes = Sets.immutableEnumSet(getListenerTypes());
		this.setRegistryName(registryName);
	}
	
	public MMDEntityPropertyBase(final String id) {
		this(new ResourceLocation(Loader.instance().activeModContainer().getModId(), id));
	}
	
	/**
	 * @return the names of entities that should be handled.
	 **/
	public abstract Set<ResourceLocation> getHandledEntities();

	/**
	 * @return the events that this property should receive and handle
	 * @see IMMDEntityProperty.ListenerType
	 **/
	public abstract Set<ListenerType> getListenerTypes();
	
	@Override
	public boolean isListenerFor(final ResourceLocation entity, final ListenerType listenerType) {
		return handledEntities.contains(entity) && listenerTypes.contains(listenerType);
	}
	
	public void onAttackMob(final EntityLiving entity, final Entity target) {}
	public void onHurt(final EntityLiving entity, DamageSource source, float amount) {}
	public void onPlayerInteract(final EntityLiving entity, final EntityPlayer player, final EnumHand hand) {}
	public void onInitAI(final EntityLiving entity) {}
	public void onTick(final EntityLiving entity) {}
	public void onDeath(final EntityLiving entity, DamageSource cause) {}
	public void onFirstSpawned(final EntityLiving entity) {}
	public void onWriteNBT(final EntityLiving entity, final NBTTagCompound tag) {}
	public void onReadNBT(final EntityLiving entity, final NBTTagCompound tag) {}
	
}
