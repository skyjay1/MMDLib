package com.mcmoddev.lib.entity;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

public class EntityHelpers {

	private EntityHelpers() {
		// private constructor
	}

	/**
	 *
	 * @param compound
	 * @param itemStack
	 * @return
	 */
	public static NBTTagCompound writeToNBTItemStack(final NBTTagCompound compound,
			final ItemStack itemStack) {
		final NBTTagCompound itemStackCompound = new NBTTagCompound();
		itemStack.writeToNBT(itemStackCompound);
		compound.setTag("itemstack", itemStackCompound);
		return compound;
	}

	/**
	 *
	 * @param compound
	 * @return
	 */
	public static ItemStack readFromNBTItemStack(final NBTTagCompound compound) {
		return new ItemStack(compound.getCompoundTag("itemstack"));
	}
	
	//////// TODO all of these! //////////
	
	/**
	 * AFTER Entity has been spawned and placed in the world
	 **/
	public static void fireOnSpawned(final IMMDEntity<?> entity) {
		
	}

	/**
	 * AFTER all Entity update code has been processed.
	 **/
	public static void fireOnLivingUpdate(final IMMDEntity<?> entity) {

	}

	/**
	 * BEFORE Entity attacks target.
	 * Returning "True" will allow the entity to attack.
	 **/
	public static boolean fireOnAttack(final IMMDEntity<?> entity, final Entity target) {
		return true;
	}

	/**
	 * BEFORE Entity takes damage.
	 * Returning "True" will allow the entity to be hurt.
	 **/
	public static boolean fireOnHurt(final IMMDEntity<?> entity, final DamageSource source, float amount) {
		return true;
	}

	/**
	 * BEFORE any death code has been processed
	 **/
	public static void fireOnDeath(final IMMDEntity<?> entity, final DamageSource source) {

	}
	
	/**
	 * AFTER all Entity AI has already been loaded
	 **/
	public static void fireOnInitAI(final IMMDEntity<?> entity) {

	}
	
	/**
	 * AFTER all Entity NBT has already been written
	 **/
	public static void fireOnWriteNBT(final IMMDEntity<?> entity, final NBTTagCompound tag) {

	}
	
	/**
	 * AFTER all Entity NBT has already been read
	 **/
	public static void fireOnReadNBT(final IMMDEntity<?> entity, final NBTTagCompound tag) {

	}
	

}
