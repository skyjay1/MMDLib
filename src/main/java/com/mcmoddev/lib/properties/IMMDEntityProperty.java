package com.mcmoddev.lib.properties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

public interface IMMDEntityProperty<T extends Entity> {
	
	/**
	 * @param entityName a specific entity name
	 * @param type a specific ListenerType
	 * @return whether this property should apply to the given entity and listener type
	 **/
	boolean isListenerFor(final ResourceLocation entityName, final ListenerType type);
	
	/** 
	 * Called immediately after the entity attacks
	 **/
	void onAttackMob(final T entity, final Entity target);
	/**
	 * Called immediately after an entity takes damage.
	 */
	void onHurt(final T entity, DamageSource source, float amount);
	/**
	 * Called when the player clicks on the entity
	 */
	void onPlayerInteract(final T entity, final EntityPlayer player, final EnumHand hand);
	/**
	 * Called when the entity AI is initialized
	 */
	void onInitAI(final T entity);
	/**
	 * Called each tick the entity is alive
	 */
	void onTick(final T entity);
	/**
	 * Called immediately before entity death is processed
	 */
	void onDeath(final T entity, DamageSource cause);
	/**
	 * Called the first time the entity spawns
	 */
	void onFirstSpawned(final T entity);
	/**
	 * Called after writing other values to NBT
	 */
	void onWriteNBT(final T entity, final NBTTagCompound tag);
	/**
	 * Called after reading other values from NBT
	 */
	void onReadNBT(final T entity, final NBTTagCompound tag);
	
	/**
	 * <em>Events that can be handled by entity property listeners:</em>
	 * <br><b>ATTACK</b>: when the entity attacks another entity
	 * <br><b>HURT</b>: when the entity is hurt
	 * <br><b>INTERACT</b>: when the player clicks on the entity
	 * <br><b>AI</b>: when the entity AI is initialized
	 * <br><b>TICK</b>: when the entity updates each tick
	 * <br><b>DEATH</b>: when the entity dies
	 * <br><b>SPAWN</b>: when the entity first spawns
	 * <br><b>NBT</b>: when the entity is read from or written to NBT
	 **/
	public enum ListenerType {
		ATTACK,
		HURT,
		INTERACT,
		AI,
		TICK,
		DEATH,
		SPAWN,
		NBT
	}
}
