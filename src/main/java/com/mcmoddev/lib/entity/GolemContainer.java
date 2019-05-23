package com.mcmoddev.lib.entity;

import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.lib.material.IMMDObject;
import com.mcmoddev.lib.material.MMDMaterial;

import net.minecraft.util.ResourceLocation;

/**
 * This class contains all of the information used to customize
 * a {@link EntityCustomGolem} based on a given {@link MMDMaterial}.
 * Must be created using the {@link GolemContainer.Builder} and should
 * not be modified at any point.
 * @author skyjay1
 */
public class GolemContainer implements IMMDObject {
	
	public static final GolemContainer EMPTY = new GolemContainer.Builder(Materials.EMPTY).build();
	
	private final MMDMaterial material;
	private final ResourceLocation texture;

	private final double health;
	private final double attack;
	private final double walkSpeed;
	private final double knockbackResist;
	private final boolean hasTint;
	private final boolean fallDamage;
	
	private final float[] colorRGBA = new float[4];
	
	public GolemContainer(final MMDMaterial materialIn, final ResourceLocation textureIn,
			final boolean hasTintIn, final boolean fallDamageIn,
			final double healthIn, final double attackIn, final double walkSpeedIn, final double knockbackResistIn) {
		this.material = materialIn;
		this.texture = textureIn;
		this.hasTint = hasTintIn;
		this.fallDamage = fallDamageIn;
		this.health = healthIn;
		this.attack = attackIn;
		this.walkSpeed = walkSpeedIn;
		this.knockbackResist = knockbackResistIn;
		// set color values based on tint
		long tmpColor = materialIn.getTintColor();
		if ((tmpColor & -67108864) == 0) {
			tmpColor |= -16777216;
		}
		this.colorRGBA[0] = (float) (tmpColor >> 16 & 255) / 255.0F;
		this.colorRGBA[1] = (float) (tmpColor >> 8 & 255) / 255.0F;
		this.colorRGBA[2] = (float) (tmpColor & 255) / 255.0F;
		this.colorRGBA[3] = (float) (tmpColor >> 24 & 255) / 255.0F;
	}
	
	public ResourceLocation getTexture() { return texture; }
	public double getHealth() { return health; }
	public double getAttack() { return attack; }
	public double getMoveSpeed() { return walkSpeed; }
	public double getKnockbackResist() { return knockbackResist; }
	public boolean hasFallDamage() { return fallDamage; }
	public boolean hasTint() { return hasTint; }
	public float[] getRGBA() { return this.colorRGBA; }

	@Override
	public MMDMaterial getMMDMaterial() {
		return this.material;
	}
	
	/**
	 * Builder class for {@link GolemContainer}. Uses default values
	 * for important stats for the custom golem:  material, texture,
	 * color, fall damage, health, attack, movement speed, and
	 * knockback resistance.
	 * @author skyjay1
	 */
	public static class Builder {
		
		/** Must be specified in the constructor **/
		private final MMDMaterial material;
		/** Defaults to the grayscale metal golem texture **/
		private ResourceLocation texture = new ResourceLocation("TODO");
		/** Defaults to true **/
		private boolean hasTint = true;
		/** Defaults to false **/
		private boolean fallDamage = false;
		/** 
		 * Initializes to a value based on the {@link MMDMaterial}
		 * given in the constructor.
		 * @see #calculateHealth(MMDMaterial)
		 **/
		private double health;
		/** 
		 * Initializes to a value based on the {@link MMDMaterial}
		 * given in the constructor.
		 * @see #calculateAttack(MMDMaterial)
		 **/
		private double attack;
		/** Defaults to 0.25 (moderately fast) **/
		private double walkSpeed = 0.25D;
		/** Defaults to 1.0 (full resistance) **/
		private double knockbackResist = 1.0D;
		
		public Builder(final MMDMaterial mat) {
			this.material = mat;
			this.health = calculateHealth(mat);
			this.attack = calculateAttack(mat);
		}
		
		/**
		 * Call this method to prevent tinting (coloring)
		 * of the golem texture. Usually used in conjunction
		 * with {@link #setTexture(ResourceLocation)} unless
		 * you want a grayscale golem.
		 * @return the Builder (for chaining methods)
		 **/
		public Builder disableTint() {
			this.hasTint = false;
			return this;
		}
		
		/**
		 * Specify a texture location to use for this golem.
		 * If one is not specified, the default texture will
		 * be the basic grayscale metal golem. For pre-colored
		 * textures (where a GL coloring should not be applied)
		 * you will also want to call {@link #disableTint()}
		 * @param rl the entity texture to be applied
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setTexture(final ResourceLocation rl) {
			this.texture = rl;
			return this;
		}
		
		/**
		 * Specify the max health of the golem that will
		 * be built. Default value for Iron Golem is 100.0.
		 * If this method is not called, the max health
		 * will be calculated from the {@link MMDMaterial}
		 * that was given in the constructor.
		 * @param healthIn the max health value
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setHealth(final double healthIn) {
			this.health = healthIn;
			return this;
		}
		
		/**
		 * Specify the base attack damage of the golem that will
		 * be built. Default value for Iron Golem is 7.5.
		 * If this method is not called, the attack damage
		 * will be calculated from the {@link MMDMaterial}
		 * that was given in the constructor.
		 * @param attackIn the base attack value
		 * @return the Builder (for chaining methods)
		 **/
		public Builder setAttack(final double attackIn) {
			this.attack = attackIn;
			return this;
		}
		
		/**
		 * Allows the golem to take fall damage. Iron Golems
		 * do not take fall damage, but a golem made from a 
		 * fragile gem-like material (etc.) may want to call
		 * this method to remove that invulnerability.
		 * @return the Builder (for chaining methods)
		 **/
		public Builder takesFallDamage() {
			this.fallDamage = true;
			return this;
		}
		
		/**
		 * Builds the GolemContainer with all values as specified in previously chained methods.
		 * @return a new GolemContainer that is ready to be registered.
		 **/
		public GolemContainer build() {
			return new GolemContainer(material, texture, hasTint, fallDamage, health, attack, walkSpeed, knockbackResist);
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

}
