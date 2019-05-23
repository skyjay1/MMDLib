package com.mcmoddev.lib.entity;

import javax.annotation.Nullable;

import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.init.Entities;
import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.lib.material.IMMDObject;
import com.mcmoddev.lib.material.MMDMaterial;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityCustomGolem extends EntityIronGolem implements IMMDObject {
	
	protected static final DataParameter<String> MATERIAL = EntityDataManager.<String>createKey(EntityCustomGolem.class, DataSerializers.STRING);
	private static final String KEY_MATERIAL = "MMDMaterial";
	
	private GolemContainer container = GolemContainer.EMPTY;

	public EntityCustomGolem(final World world) {
		super(world);
	}
	
	/**
	 * ALWAYS CALL THIS WHEN YOU FIRST MAKE THE GOLEM IN-WORLD
	 **/
	public EntityCustomGolem setContainer(final GolemContainer containerIn) {
		this.updateContainerStats(containerIn);
		return this;
	}

	/**
	 * Adjusts all stats, textures, tints, etc. of this golem based on the
	 * values in the given {@link GolemContainer}
	 * @param cont
	 **/
	private void updateContainerStats(final GolemContainer cont) {
		this.container = cont;
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(cont.getHealth());
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(cont.getMoveSpeed());
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(cont.getKnockbackResist());
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(cont.getAttack());
		// TODO hook to add/remove entity AI as needed.
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	@Override
	protected void updateAITasks() {
		// TODO maybe add a hook for custom EntityAI update tasks? We'll see

		super.updateAITasks();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
	}

	@Override
	protected int decreaseAirSupply(int air) {
		// TODO should we allow golems to drown?
		return super.decreaseAirSupply(air);
	}

	@Override
	protected void collideWithEntity(Entity entityIn) {
		super.collideWithEntity(entityIn);
	}

	@Override
	public void onLivingUpdate() {
		// TODO hook for custom living-update behavior
		super.onLivingUpdate();
	}

	@Override
	public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
		// TODO should we allow custom golems to attack creepers? Only purpose of this method...
		return super.canAttackClass(cls);
	}

//	    public static void registerFixesIronGolem(DataFixer fixer)
//	    {
//	        EntityLiving.registerFixesMob(fixer, EntityIronGolem.class);
//	    }

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		// TODO safety checks
		compound.setString(KEY_MATERIAL, this.container.getMMDMaterial().getName());
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		// TODO safety checks
		this.container = Entities.getContainer(Materials.getMaterialByName(compound.getString(KEY_MATERIAL)));
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		// TODO hook for custom attack behavior
		return super.attackEntityAsMob(entityIn);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void handleStatusUpdate(byte id) {
		super.handleStatusUpdate(id);
	}

	@Override
	public Village getVillage() {
		return super.getVillage();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getAttackTimer() {
		return super.getAttackTimer();
	}

	@Override
	public void setHoldingRose(boolean holdingRose) {
		super.setHoldingRose(holdingRose);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return super.getHurtSound(damageSourceIn);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return super.getDeathSound();
	}

	@Override
	protected void playStepSound(BlockPos pos, Block blockIn) {
		super.playStepSound(pos, blockIn);
	}

	@Nullable
	@Override
	protected ResourceLocation getLootTable() {
		// TODO return custom loot table
		return LootTableList.ENTITIES_IRON_GOLEM;
	}

	@Override
	public int getHoldRoseTick() {
		return super.getHoldRoseTick();
	}

	@Override
	public boolean isPlayerCreated() {
		return super.isPlayerCreated();
	}

	@Override
	public void setPlayerCreated(boolean playerCreated) {
		super.setPlayerCreated(playerCreated);
	}

	@Override
	public void onDeath(DamageSource cause) {
		// TODO hook here for special behavior
		super.onDeath(cause);
	}
	
	@Override
	public void fall(float distance, float damageMultiplier) {
		if (this.container.hasFallDamage()) {
			float[] ret = net.minecraftforge.common.ForgeHooks.onLivingFall(this, distance, damageMultiplier);
			if (ret == null) {
				return;
			}
			distance = ret[0];
			damageMultiplier = ret[1];
			super.fall(distance, damageMultiplier);
			PotionEffect potioneffect = this.getActivePotionEffect(MobEffects.JUMP_BOOST);
			float f = potioneffect == null ? 0.0F : (float) (potioneffect.getAmplifier() + 1);
			int i = MathHelper.ceil((distance - 3.0F - f) * damageMultiplier);

			if (i > 0) {
				this.playSound(this.getFallSound(i), 1.0F, 1.0F);
				this.attackEntityFrom(DamageSource.FALL, (float) i);
				int j = MathHelper.floor(this.posX);
				int k = MathHelper.floor(this.posY - 0.20000000298023224D);
				int l = MathHelper.floor(this.posZ);
				IBlockState iblockstate = this.world.getBlockState(new BlockPos(j, k, l));

				if (iblockstate.getMaterial() != Material.AIR) {
					SoundType soundtype = iblockstate.getBlock().getSoundType(iblockstate, world, new BlockPos(j, k, l),
							this);
					this.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.5F,
							soundtype.getPitch() * 0.75F);
				}
			}
		}
	}

	/**
	 * Called when a user uses the creative pick block button on this entity.
	 * @param target The full target the player is looking at
	 * @return A ItemStack to add to the player's inventory, Null if nothing should be added.
	 */
	@Override
	public ItemStack getPickedResult(final RayTraceResult target) {
		return this.container.getMMDMaterial().getItemStack(Names.BLOCK);
	}
	
	@Override
	public MMDMaterial getMMDMaterial() {
		return this.container.getMMDMaterial();
	}
	
	public GolemContainer getContainer() {
		return this.container;
	}
}
