package com.mcmoddev.lib.properties;

import javax.annotation.Nullable;

import com.mcmoddev.lib.entity.IMMDEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

public interface IMMDEntityProperty<T> {
	
	/** 
	 * Called immediately before an entity is damaged by an attack.
	 **/
	boolean hasAttackBehavior(final IMMDEntity<? super T> entity);
	/**
	 * Called immediately before an entity takes damage.
	 */
	boolean hasHurtBehavior(final IMMDEntity<? super T> entity);
	boolean hasCustomAI(final IMMDEntity<? super T> entity);
	boolean hasTickBehavior(final IMMDEntity<? super T> entity);
	boolean hasDeathBehavior(final IMMDEntity<? super T> entity);
	boolean hasSpawnBehavior(final IMMDEntity<? super T> entity);
	boolean hasNBTBehavior(final IMMDEntity<? super T> entity);
	
	void onAttackMob(final IMMDEntity<? super T> entity);
	void onHurt(final IMMDEntity<? super T> entity, DamageSource source, float amount);
	void onInitAI(final IMMDEntity<? super T> entity);
	void onTick(final IMMDEntity<? super T> entity);
	void onDeath(final IMMDEntity<? super T> entity, DamageSource cause);
	void onSpawned(final IMMDEntity<? super T> entity, @Nullable final EntityPlayer player);
	
	void onWriteNBT(final IMMDEntity<? super T> entity, final NBTTagCompound tag);
	void onReadNBT(final IMMDEntity<? super T> entity, final NBTTagCompound tag);
}
