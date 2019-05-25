package com.mcmoddev.lib.entity;

import com.mcmoddev.lib.init.Entities;
import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.lib.material.IMMDObject;
import com.mcmoddev.lib.material.MMDMaterial;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

/**
 * This class contains all of the information used to customize
 * a {@link EntityCustomGolem} based on a given {@link MMDMaterial}.
 * Must be created using the {@link GolemContainer.Builder} and should
 * not be modified at any point.
 * <br>Adapted from BetterAnimalsPlus by its_meow. Used with permission.
 */
public final class GolemContainer extends EntityContainer implements IMMDObject {
	
	/** High-Contrast grayscale metal texture **/
	public static final ResourceLocation TEXTURE_METAL_GRAYSCALE_HIGH = 
			new ResourceLocation("mmdlib:textures/entity/golem_metal_grayscale_high.png");
	/** Medium-to-Low-Contrast grayscale metal texture **/
	public static final ResourceLocation TEXTURE_METAL_GRAYSCALE_LOW = 
			new ResourceLocation("mmdlib:textures/entity/golem_metal_grayscale_low.png");
	/** Non-grayscale layer of eyes and vines **/
	public static final ResourceLocation TEXTURE_OVERLAY = 
			new ResourceLocation("mmdlib:textures/entity/golem_overlay.png");
	
	public static final GolemContainer EMPTY_GOLEM_CONTAINER = GolemContainer.Builder.create(Materials.EMPTY).build();
		
	private final MMDMaterial material;
	private final boolean hasTint;
	private final boolean fallDamage;
	private final float[] colorRGBA = new float[4];
	
	protected GolemContainer(final Class<? extends EntityLiving> entityClassIn, final String nameIn,
			final MMDMaterial materialIn, final ResourceLocation textureIn,
			final ResourceLocation lootTableIn, final MobHostility attitudeIn, 
			final boolean canSwimIn, final boolean hasTintIn, final boolean fallDamageIn, 
			final double healthIn, final double attackIn, 
			final double walkSpeedIn, final double knockbackResistIn) {
		super(entityClassIn, nameIn, textureIn, lootTableIn, attitudeIn, canSwimIn, healthIn, attackIn, walkSpeedIn, knockbackResistIn);
		this.material = materialIn;
		this.hasTint = hasTintIn;
		this.fallDamage = fallDamageIn;
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
	
	public boolean hasFallDamage() { return fallDamage; }
	public boolean hasTint() { return hasTint; }
	public float[] getRGBA() { return this.colorRGBA; }
	@Override
	public MMDMaterial getMMDMaterial() { return this.material; }
	
	/**
	 * Builder class for {@link GolemContainer}. Uses default values
	 * for important stats for the custom golem:  material, texture,
	 * color, fall damage, health, attack, movement speed, and
	 * knockback resistance are included.
	 * @author skyjay1
	 */
	public static class Builder extends EntityContainer.Builder {
		
		/** Must be specified in the constructor **/
		protected final MMDMaterial material;
		/** Defaults to true **/
		protected boolean hasTint = true;
		/** Defaults to false **/
		protected boolean fallDamage = false;
		
		public Builder(final MMDMaterial mat) {
			super(EntityCustomGolem.class, Entities.makeGolemKey(mat));
			this.material = mat;
			this.knockbackResist = 0.8D;
			this.canSwim = false;
			this.health = calculateHealth(mat);
			this.attack = calculateAttack(mat);
			this.setHostility(MobHostility.NEUTRAL);
			// TODO make grayscale textures and RLs for each of these
			switch(mat.getType()) {
			case CRYSTAL:
				break;
			case GEM:
				break;
			case METAL:
				this.setTexture(TEXTURE_METAL_GRAYSCALE_HIGH);
				this.knockbackResist = 1.0D;
				break;
			case MINERAL:
				break;
			case ROCK:
				break;
			case WOOD:
				this.knockbackResist = 0;
				this.canSwim = true;
				break;
			default:
				break;
			}
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
		@Override
		public GolemContainer build() {
			return new GolemContainer(entityClass, entityName, material, texture, lootTable,
					attitude, canSwim, hasTint, fallDamage, health, attack, walkSpeed, knockbackResist);
		}
		
		public static GolemContainer.Builder create(final MMDMaterial materialIn) {
			return new GolemContainer.Builder(materialIn);
		}
		
		/**
		 * Given that MMDMaterial 'Iron' has ArmorMaxDamageFactor of 16,
		 * this equation returns 100.0 when given an MMDMaterial with
		 * the same {@code Strength} stat as Iron.
		 * @param mat the material used for this golem
		 * @return an appropriate value for the golem's max health.
		 * @see MMDMaterial#getArmorMaxDamageFactor()
		 **/
		private static double calculateHealth(final MMDMaterial mat) {
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
		private static double calculateAttack(final MMDMaterial mat) {
			// note: #getBlockHardness returns HARDNESS * 2
			return mat.getBlockHardness() * 0.46875;
		}
	}

}
