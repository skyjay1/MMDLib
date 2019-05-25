package com.mcmoddev.lib.entity;

import javax.annotation.Nonnull;
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
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityCustomGolem extends EntityIronGolem implements IMMDEntity<EntityCustomGolem>, IMMDObject {
	
	protected static final DataParameter<String> CONTAINER_NAME = EntityDataManager.<String>createKey(EntityCustomAnimal.class, DataSerializers.STRING);
	private static final String KEY_CONTAINER_NAME = "ContainerName";
	private static final byte KEY_ATTACK = (byte)24;
	
	private GolemContainer container;
	private int attackTimer2;

	public EntityCustomGolem(final World world) {
		super(world);
	}
	
	/**
	 * ALWAYS CALL THIS WHEN YOU FIRST MAKE THE GOLEM IN-WORLD
	 **/
	public EntityCustomGolem setMMDMaterial(@Nonnull final MMDMaterial mat) {
		
		return this;
	}
	

	@Override
	public void notifyDataManagerChange(final DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if(CONTAINER_NAME.equals(key)) {
			final String containerName = this.getDataManager().get(CONTAINER_NAME);
			// make sure a container is registered for this material
			final EntityContainer cont = Entities.getEntityContainer(containerName);
			if(cont instanceof GolemContainer) {
				// actually use the container to update golem stats
				this.updateContainerStats((GolemContainer)cont);
			} else {
				com.mcmoddev.lib.MMDLib.logger.error("Failed to update golem stats - no GolemContainer was found with name '%s'", containerName);
			}
		}
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
		EntityHelpers.fireOnInitAI(this);
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
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		super.applyEntityAttributes();
	}

	@Override
	protected int decreaseAirSupply(int air) {
		// TODO should we allow golems to drown?
		return super.decreaseAirSupply(air);
	}

	@Override
	protected void collideWithEntity(final Entity entityIn) {
		super.collideWithEntity(entityIn);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.attackTimer2 > 0) {
			--this.attackTimer2;
		}
		EntityHelpers.fireOnLivingUpdate(this);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return EntityHelpers.fireOnHurt(this, source, amount) && super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean canAttackClass(final Class<? extends EntityLivingBase> cls) {
		// TODO should we allow custom golems to attack creepers? Only purpose of this method...
		return super.canAttackClass(cls);
	}

//	    public static void registerFixesIronGolem(DataFixer fixer)
//	    {
//	        EntityLiving.registerFixesMob(fixer, EntityIronGolem.class);
//	    }

	@Override
	public void writeEntityToNBT(final NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		// TODO safety checks
		compound.setString(KEY_CONTAINER_NAME, this.getContainer().getEntityName());
		EntityHelpers.fireOnWriteNBT(this, compound);
	}
	
	@Override
	public void readEntityFromNBT(final NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		final String name = compound.getString(KEY_CONTAINER_NAME);
		this.setContainer(Entities.getEntityContainer(name));
		EntityHelpers.fireOnReadNBT(this, compound);
	}

	@Override
	public boolean attackEntityAsMob(final Entity entityIn) {
		if(super.attackEntityAsMob(entityIn)) {
			this.attackTimer2 = 10;
			this.world.setEntityState(this, KEY_ATTACK);
			final float baseAttack = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
			boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 
					baseAttack + this.rand.nextInt(Math.max(2, (int)(baseAttack * 2.0F))));

			if (flag) {
				entityIn.motionY += 0.4000000059604645D;
				this.applyEnchantments(this, entityIn);
				EntityHelpers.fireOnAttack(this, entityIn);
			}
			this.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0F, 1.0F);
			return flag;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void handleStatusUpdate(final byte id) {
		if (id == KEY_ATTACK) {
			this.attackTimer2 = 10;
			this.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0F, 1.0F);
		}
		super.handleStatusUpdate(id);
	}

	@Override
	public Village getVillage() {
		return super.getVillage();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getAttackTimer() {
		return attackTimer2;
	}

	@Override
	public void setHoldingRose(final boolean holdingRose) {
		super.setHoldingRose(holdingRose);
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource damageSourceIn) {
		// TODO add sound to EntityContainer
		return super.getHurtSound(damageSourceIn);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return super.getDeathSound();
	}

	@Override
	protected void playStepSound(final BlockPos pos, final Block blockIn) {
		super.playStepSound(pos, blockIn);
	}

	@Nullable
	@Override
	protected ResourceLocation getLootTable() {
		return this.container != null ? this.container.getLootTable() : LootTableList.EMPTY;
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
	public void setPlayerCreated(final boolean playerCreated) {
		super.setPlayerCreated(playerCreated);
	}

	@Override
	public void onDeath(final DamageSource cause) {
		EntityHelpers.fireOnDeath(this, cause);
		super.onDeath(cause);
	}
	
	@Override
	public void fall(float distance, float damageMultiplier) {
		if (this.container.hasFallDamage()) {
			// COPY PASTED FROM ENTITYLIVING CLASS
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
	
	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		EntityHelpers.fireOnSpawned(this, livingdata);
		return super.onInitialSpawn(difficulty, livingdata);
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
	public String getName() {
		if (this.hasCustomName()) {
			return this.getCustomNameTag();
		} else {
			return I18n.format("mmd.entity.customgolem.name", getMMDMaterial().getCapitalizedName());
		}
	}
	
	@Override
	public MMDMaterial getMMDMaterial() {
		return this.container != null ? this.container.getMMDMaterial() : Materials.EMPTY;
	}
	
	@Override
	public void setContainer(final EntityContainer containerIn) {
		this.getDataManager().set(CONTAINER_NAME, containerIn.getEntityName());
	}
	
	@Override
	public GolemContainer getContainer() {
		return this.container;
	}

	@Override
	public EntityCustomGolem getEntity() {
		return this;
	}
	
	public static final class EntityAIAttackGolem extends EntityAINearestAttackableTarget<EntityCustomGolem> {
		public EntityAIAttackGolem(final EntityCreature creature) {
			super(creature, EntityCustomGolem.class, true);
		}
	}

}
