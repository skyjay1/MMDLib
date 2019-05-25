package com.mcmoddev.lib.entity;

import com.mcmoddev.lib.init.Entities;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/**
 * Must be created using the {@link AnimalContainer.Builder} and should
 * not be modified at any point.
 * <br>Adapted from BetterAnimalsPlus by its_meow. Used with permission.
 */
public class AnimalContainer extends EntityContainer {
	
	public static final AnimalContainer EMPTY_ANIMAL_CONTAINER = AnimalContainer.Builder.create("customanimal").build();

	protected final Item temptItem;
	
	protected AnimalContainer(final Class<? extends EntityLiving> entityClassIn, final String entityNameIn, 
			final ResourceLocation textureIn, final ResourceLocation lootTableIn, final EntityContainer.MobHostility attitudeIn, 
			final boolean canSwimIn, final Item temptItemIn,
			final double healthIn, final double attackIn, final double walkSpeedIn, 
			final double knockbackResistIn) {
		super(entityClassIn, entityNameIn, textureIn, lootTableIn, attitudeIn, canSwimIn, healthIn, attackIn, walkSpeedIn, knockbackResistIn);
		this.temptItem = temptItemIn;
	}
	
	public boolean hasTemptItem() { return temptItem != null; }
	public Item getTemptItem() { return temptItem; }
	
	/**
	 * Builder class for {@link AnimalContainer}. Uses default values
	 * for important stats for the entity:  texture, health, attack, 
	 * movement speed, hostility, knockback resistance, and mating item
	 * are included.
	 * @author skyjay1
	 */
	public static class Builder extends EntityContainer.Builder {
		
		/** Defaults to null (no tempt item and no mating) **/
		protected Item temptItem = null;
		
		protected Builder(final Class<? extends EntityLiving> entityClassIn, final String entityNameIn) {
			super(entityClassIn, Entities.PREFIX_ANIMAL.concat(entityNameIn));
		} 
		
		/**
		 * Specify an item that will allow this entity
		 * to heal / mate. Only applies to {@link EntityCustomAnimal}.
		 * Default value is no mating item and no mating behavior.
		 * @param temptItemIn the item to trigger mating
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setMatingItem(final Item temptItemIn) {
			this.temptItem = temptItemIn;
			return this;
		}
		
		/**
		 * Builds the EntityContainer with all values as specified in previously chained methods.
		 * @return a new EntityContainer that is ready to be registered.
		 **/
		@Override
		public AnimalContainer build() {
			return new AnimalContainer(entityClass, entityName, texture, lootTable, attitude, 
					canSwim, temptItem, health, attack, walkSpeed, knockbackResist);
		}
		
		public static AnimalContainer.Builder create(final String name) {
			return new AnimalContainer.Builder(EntityCustomAnimal.class, name);
		}
	}
}
