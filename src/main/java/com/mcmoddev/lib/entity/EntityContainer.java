package com.mcmoddev.lib.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Must be created using the {@link EntityContainer.Builder} and should
 * not be modified at any point.
 * <br>Adapted from BetterAnimalsPlus by its_meow. Used with permission.
 */
public class EntityContainer extends IForgeRegistryEntry.Impl<EntityContainer> {
		
	protected final Class<? extends EntityLiving> entityClass;
	protected final ResourceLocation entityName;
	protected final ResourceLocation texture;
	protected final ResourceLocation lootTable;
	protected final MobHostility attitude;
	protected final SoundEvent livingSound;
	protected final SoundEvent hurtSound;
	protected final SoundEvent deathSound;
	protected final boolean canSwim;
	protected final double health;
	protected final double attack;
	protected final double walkSpeed;
	protected final double knockbackResist;
	
	protected EntityContainer(final Class<? extends EntityLiving> entityClassIn, final ResourceLocation entityNameIn, 
			final ResourceLocation textureIn, final ResourceLocation lootTableIn, final MobHostility attitudeIn, 
			final SoundEvent livingSoundIn, final SoundEvent hurtSoundIn, final SoundEvent deathSoundIn,
			final boolean canSwimIn,
			final double healthIn, final double attackIn, final double walkSpeedIn, 
			final double knockbackResistIn) {
		this.entityClass = entityClassIn;
		this.entityName = entityNameIn;
		this.texture = textureIn;
		this.lootTable = lootTableIn;
		this.attitude = attitudeIn;
		this.livingSound = livingSoundIn;
		this.hurtSound = hurtSoundIn;
		this.deathSound = deathSoundIn;
		this.canSwim = canSwimIn;
		this.health = healthIn;
		this.attack = attackIn;
		this.walkSpeed = walkSpeedIn;
		this.knockbackResist = knockbackResistIn;
	}
	
	public Class<? extends EntityLiving> getEntityClass() { return entityClass; }
	public ResourceLocation getEntityName() { return entityName; }
	public ResourceLocation getTexture() { return texture; }
	public double getHealth() { return health; }
	public double getAttack() { return attack; }
	public double getMoveSpeed() { return walkSpeed; }
	public double getKnockbackResist() { return knockbackResist; }
	public MobHostility getHostility() { return attitude; }
	public ResourceLocation getLootTable() { return lootTable; }
	public boolean canSwim() { return canSwim; }
	@Nullable
	public SoundEvent getLivingSound() { return livingSound; }
	@Nullable
	public SoundEvent getHurtSound() { return hurtSound; }
	@Nullable
	public SoundEvent getDeathSound() { return deathSound; }
	
	/**
	 * Builder class for {@link EntityContainer}. Uses default values
	 * for important stats for the entity:  texture, health, attack, 
	 * movement speed, hostility, and knockback resistance are included.
	 * @author skyjay1
	 */
	public static class Builder {
		
		protected final Class<? extends EntityLiving> entityClass;
		protected final ResourceLocation entityName;
		/** Defaults to the grayscale metal golem texture **/
		protected ResourceLocation texture = GolemContainer.TEXTURE_METAL_GRAYSCALE_HIGH;
		/** Defaults to empty loot table **/
		protected ResourceLocation lootTable = LootTableList.EMPTY;
		protected double health = 10.0D;
		protected double attack = 1.0D;
		/** Defaults to 0.25 (moderately fast) **/
		protected double walkSpeed = 0.25D;
		/** Defaults to 0 (no resistance) **/
		protected double knockbackResist = 0.0D;
		/** Defaults to PASSIVE **/
		protected MobHostility attitude = MobHostility.PASSIVE;
		/** Defaults to true (can swim) **/
		protected boolean canSwim = true;
		/** Defaults to null (no sound) **/
		protected SoundEvent livingSound = null;
		/** Defaults to null (no sound) **/
		protected SoundEvent hurtSound = null;
		/** Defaults to null (no sound) **/
		protected SoundEvent deathSound = null;
		
		protected Builder(final Class<? extends EntityLiving> entityClassIn, final String entityNameIn) {
			this.entityClass = entityClassIn;
			this.entityName = new ResourceLocation(entityNameIn);
		} 
		
		/**
		 * Specify a texture location to use for this entity.
		 * If one is not specified, the default texture will
		 * be the basic grayscale metal golem.
		 * @param rl the entity texture to be applied
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setTexture(final ResourceLocation rl) {
			this.texture = rl;
			return this;
		}
		
		/**
		 * Specify the max health of the entity that will
		 * be built.
		 * @param healthIn the max health value
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setHealth(final double healthIn) {
			this.health = healthIn;
			return this;
		}
		
		/**
		 * Specify the base attack damage of the entity that will
		 * be built. 
		 * @param attackIn the base attack value
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setAttack(final double attackIn) {
			this.attack = attackIn;
			return this;
		}
		
		/**
		 * Specify the hostility level of the mob:
		 * Passive, Neutral, or Hostile.
		 * @param attitudeIn the hostility level of the mob
		 * @return the Builder (for chaining methods)
		 * @see MobHostility
		 **/
		public Builder setHostility(final EntityContainer.MobHostility attitudeIn) {
			this.attitude = attitudeIn;
			return this;
		}
		
		/**
		 * Turn off the ability of this entity to swim,
		 * if applicable. Swimming is enabled by default.
		 * @return the Builder (for chaining methods)
		 **/
		public Builder disableSwim() {
			this.canSwim = false;
			return this;
		}
		
		/**
		 * Specify the location of a loot table for items
		 * to drop when the mob dies.
		 * @param lootTableIn the location of a loot table
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setLootTable(final ResourceLocation lootTableIn) {
			this.lootTable = lootTableIn;
			registerLootTable();
			return this;
		}
		
		/**
		 * Specify a sound to play randomly while the entity
		 * is alive. If null, no sounds will play.
		 * @param livingSoundIn a sound to play
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setLivingSound(@Nullable final SoundEvent livingSoundIn) {
			this.livingSound = livingSoundIn;
			return this;
		}
		
		/**
		 * Specify a sound to play when the entity is hurt. 
		 * If null, no sounds will play.
		 * @param hurtSoundIn an sound to play
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setHurtSound(@Nullable final SoundEvent hurtSoundIn) {
			this.hurtSound = hurtSoundIn;
			return this;
		}
		
		/**
		 * Specify a sound to play when the entity dies. 
		 * If null, no sounds will play.
		 * @param deathSoundIn a sound to play
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setDeathSound(@Nullable final SoundEvent deathSoundIn) {
			this.deathSound = deathSoundIn;
			return this;
		}
		
		/**
		 * Specify a single sound to play randomly while the entity
		 * is alive, when the entity is hurt, and when the entity dies.
		 * @param soundIn the sound to play
		 * @return the Builder (for chaining methods)
		 * @see #setLivingSound(SoundEvent)
		 * @see #setHurtSound(SoundEvent)
		 * @see #setDeathSound(SoundEvent)
		 **/
		public Builder setSound(@Nullable final SoundEvent soundIn) {
			this.setLivingSound(soundIn);
			this.setHurtSound(soundIn);
			this.setDeathSound(soundIn);
			return this;
		}
		
		/**
		 * Builds the EntityContainer with all values as specified in previously chained methods.
		 * @return a new EntityContainer that is ready to be registered.
		 **/
		public EntityContainer build() {
			// build the container
			return new EntityContainer(entityClass, entityName, texture, lootTable, attitude, 
					livingSound, hurtSound, deathSound,
					canSwim, health, attack, walkSpeed, knockbackResist);
		}
		
		public static EntityContainer.Builder create(final Class<? extends EntityLiving> entityClassIn, 
				final String entityNameIn) {
			return new EntityContainer.Builder(entityClassIn, entityNameIn);
		}
		
		/**
		 * Registers the loot table. Should not be called more than once.
		 * @return whether the loot table was able to be registered.
		 **/
		protected boolean registerLootTable() {
			// register loot table
			if(this.lootTable != LootTableList.EMPTY && !LootTableList.getAll().contains(this.lootTable)) {
				LootTableList.register(this.lootTable);
				return true;
			}
			return false;
		}
	}

	/**
	 * Indicates the level of hostility of this mob
	 * toward the player.
	 * <br>eg, Cows are PASSIVE, Iron Golems are NEUTRAL, and
	 * Zombies are HOSTILE.
	 **/
	public enum MobHostility {
		/**
		 * Does not react when attacked by player.
		 **/
		PASSIVE,
		/**
		 * Attacks player when provoked.
		 **/
		NEUTRAL,
		/**
		 * Always attacks player.
		 **/
		HOSTILE;
	}
}
