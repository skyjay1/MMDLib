package com.mcmoddev.lib.entity;

import com.mcmoddev.lib.init.Entities;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

/**
 * Must be created using the {@link MobContainer.Builder} and should
 * not be modified at any point.
 * <br>Adapted from BetterAnimalsPlus by its_meow. Used with permission.
 */
public class MobContainer extends EntityContainer {
	
	public static final MobContainer EMPTY_MOB_CONTAINER = MobContainer.Builder.create("custommob").build();

	protected final boolean burnInDay;
	protected final boolean leapsAtTarget;
	
	protected MobContainer(final Class<? extends EntityLiving> entityClassIn, final ResourceLocation entityNameIn, 
			final ResourceLocation textureIn, final ResourceLocation lootTableIn,
			final EntityContainer.MobHostility attitudeIn, final boolean canSwimIn, 
			final SoundEvent livingSoundIn, final SoundEvent hurtSoundIn, final SoundEvent deathSoundIn,
			final boolean burnInDayIn, final boolean leapsAtTargetIn, final double healthIn, final double attackIn, 
			final double walkSpeedIn, final double knockbackResistIn) {
		super(entityClassIn, entityNameIn, textureIn, lootTableIn, attitudeIn, 
				livingSoundIn, hurtSoundIn, deathSoundIn, canSwimIn, 
				healthIn, attackIn, walkSpeedIn, knockbackResistIn);
		this.burnInDay = burnInDayIn;
		this.leapsAtTarget = leapsAtTargetIn;
	}
	
	public boolean doesBurnInDay() { return burnInDay; }
	public boolean leapsAtTarget() { return leapsAtTarget; }
	
	/**
	 * Builder class for {@link MobContainer}. Uses default values
	 * for important stats for the entity:  texture, health, attack, 
	 * movement speed, hostility, knockback resistance, and daylight
	 * behavior are included.
	 * @author skyjay1
	 */
	public static class Builder extends EntityContainer.Builder {
		
		/** Defaults to false (does not burn in sunlight) **/
		protected boolean burnInDay = false;
		/** Defaults to false (touch of death, not leap) **/
		protected boolean leapsAtTarget = false;
		
		protected Builder(final Class<? extends EntityLiving> entityClassIn, final String entityNameIn) {
			super(entityClassIn, entityNameIn);
			this.attitude = MobHostility.HOSTILE;
		} 
		
		/**
		 * Allow the entity to burn in daylight.
		 * Disabled by default.
		 * @return the Builder (for chaining methods)
		 **/
		public MobContainer.Builder enableBurnInDay() {
			this.burnInDay = true;
			return this;
		}
		
		/**
		 * When enabled, the mob will attack by jumping
		 * at the player, like a spider or wolf does.
		 * @return the Builder (for chaining methods)
		 **/
		public MobContainer.Builder enableLeap() {
			this.leapsAtTarget = true;
			return this;
		}
		
		/**
		 * Builds the EntityContainer with all values as specified in previously chained methods.
		 * @return a new EntityContainer that is ready to be registered.
		 **/
		@Override
		public MobContainer build() {
			return new MobContainer(entityClass, entityName, texture, lootTable, attitude, 
					canSwim, livingSound, hurtSound, deathSound, burnInDay, leapsAtTarget, 
					health, attack, walkSpeed, knockbackResist);
		}
		
		public static MobContainer.Builder create(final String name) {
			return new MobContainer.Builder(EntityCustomAnimal.class, name);
		}
	}
}
