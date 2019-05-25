package com.mcmoddev.lib.entity;

import com.mcmoddev.lib.material.MMDMaterial;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Must be created using the {@link EntityContainer.Builder} and should
 * not be modified at any point.
 * <br>Adapted from BetterAnimalsPlus by its_meow. Used with permission.
 */
public class EntityContainer extends IForgeRegistryEntry.Impl<EntityContainer> {
		
	protected final Class<? extends EntityLiving> entityClass;
	protected final String entityName;
	protected final ResourceLocation texture;
	protected final ResourceLocation lootTable;
	protected final MobHostility attitude;
	protected final boolean canSwim;
	protected final double health;
	protected final double attack;
	protected final double walkSpeed;
	protected final double knockbackResist;
	
	protected EntityContainer(final Class<? extends EntityLiving> entityClassIn, final String entityNameIn, 
			final ResourceLocation textureIn, final ResourceLocation lootTableIn, final MobHostility attitudeIn, 
			final boolean canSwimIn,
			final double healthIn, final double attackIn, final double walkSpeedIn, 
			final double knockbackResistIn) {
		this.entityClass = entityClassIn;
		this.entityName = entityNameIn;
		this.texture = textureIn;
		this.lootTable = lootTableIn;
		this.attitude = attitudeIn;
		this.canSwim = canSwimIn;
		this.health = healthIn;
		this.attack = attackIn;
		this.walkSpeed = walkSpeedIn;
		this.knockbackResist = knockbackResistIn;
	}
	
	public Class<? extends EntityLiving> getEntityClass() { return entityClass; }
	public String getEntityName() { return entityName; }
	public ResourceLocation getTexture() { return texture; }
	public double getHealth() { return health; }
	public double getAttack() { return attack; }
	public double getMoveSpeed() { return walkSpeed; }
	public double getKnockbackResist() { return knockbackResist; }
	public MobHostility getHostility() { return attitude; }
	public ResourceLocation getLootTable() { return lootTable; }
	public boolean canSwim() { return canSwim; }
	
	/**
	 * Builder class for {@link EntityContainer}. Uses default values
	 * for important stats for the entity:  texture, health, attack, 
	 * movement speed, hostility, and knockback resistance are included.
	 * @author skyjay1
	 */
	public static class Builder {
		
		protected Class<? extends EntityLiving> entityClass;
		protected String entityName;
		/** Defaults to the grayscale metal golem texture **/
		protected ResourceLocation texture = new ResourceLocation("TODO");
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
		
		protected Builder(final Class<? extends EntityLiving> entityClassIn, final String entityNameIn) {
			this.entityClass = entityClassIn;
			this.entityName = entityNameIn;
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
		public Builder setHostility(final MobHostility attitudeIn) {
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
			LootTableList.register(lootTableIn);
			return this;
		}
		
		/**
		 * Builds the EntityContainer with all values as specified in previously chained methods.
		 * @return a new EntityContainer that is ready to be registered.
		 **/
		public EntityContainer build() {
			return new EntityContainer(entityClass, entityName, texture, lootTable, attitude, 
					canSwim, health, attack, walkSpeed, knockbackResist);
		}
		
		public static EntityContainer.Builder create(final Class<? extends EntityLiving> entityClassIn, 
				final String entityNameIn) {
			return new EntityContainer.Builder(entityClassIn, entityNameIn);
		}
		
		/**
		 * Given that MMDMaterial 'Iron' has ArmorMaxDamageFactor of 16,
		 * this equation returns 100.0 when given an MMDMaterial with
		 * the same {@code Strength} stat as Iron.
		 * @param mat the material used for this golem
		 * @return an appropriate value for the golem's max health.
		 * @see MMDMaterial#getArmorMaxDamageFactor()
		 **/
		protected static double calculateHealth(final MMDMaterial mat) {
			// note: #getArmorMaxDamageFactor returns STRENGTH * 2
			return mat.getArmorMaxDamageFactor() * 6.25D;
		}
		
		/**
		 * Given that MMDMaterial 'Iron' has Block Hardness of 16,
		 * this equation returns 7.5 when given an MMDMaterial with
		 * the same {@code Hardness} stat as Iron.
		 * @param mat the material used for this golem
		 * @return an appropriate value for the golem's attack damage.
		 * @see MMDMaterial#getBlockHardness()
		 **/
		protected static double calculateAttack(final MMDMaterial mat) {
			// note: #getBlockHardness returns HARDNESS * 2
			return mat.getBlockHardness() * 0.46875;
		}
	}

	/**
	 * Indicates the level of hostility of this mob
	 * toward the player.
	 * Cows are PASSIVE, Iron Golems are NEUTRAL, and
	 * Zombies are HOSTILE.
	 * @author skyjay1
	 **/
	public static enum MobHostility {
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
