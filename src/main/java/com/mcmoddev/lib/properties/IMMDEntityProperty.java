package com.mcmoddev.lib.properties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

public interface IMMDEntityProperty<T extends Entity> {
	
	/** 
	 * Called immediately after an entity attacks
	 **/
	boolean hasAttackBehavior(final T entity);
	/**
	 * Called immediately before an entity takes damage.
	 */
	boolean hasHurtBehavior(final T entity);
	boolean hasInteractBehavior(final T entity);
	boolean hasCustomAI(final T entity);
	boolean hasTickBehavior(final T entity);
	boolean hasDeathBehavior(final T entity);
	boolean hasSpawnBehavior(final T entity);
	boolean hasNBTBehavior(final T entity);
	
	void onAttackMob(final T entity, final Entity target);
	void onHurt(final T entity, DamageSource source, float amount);
	void onPlayerInteract(final T entity, final EntityPlayer player, final EnumHand hand);
	void onInitAI(final T entity);
	void onTick(final T entity);
	void onDeath(final T entity, DamageSource cause);
	void onFirstSpawned(final T entity);
	
	void onWriteNBT(final T entity, final NBTTagCompound tag);
	void onReadNBT(final T entity, final NBTTagCompound tag);
}
