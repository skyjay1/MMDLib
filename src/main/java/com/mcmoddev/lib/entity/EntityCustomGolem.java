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
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
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
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class EntityCustomGolem extends EntityIronGolem implements IMMDEntity<EntityCustomGolem>, IMMDObject {
	
	protected static final DataParameter<String> CONTAINER_NAME = EntityDataManager.<String>createKey(EntityCustomAnimal.class, DataSerializers.STRING);
	private static final String KEY_CONTAINER_NAME = "ContainerName";
	private static final byte KEY_ATTACK = (byte)24;
	
	private GolemContainer container = GolemContainer.EMPTY_GOLEM_CONTAINER;

	public EntityCustomGolem(final World world) {
		super(world);
	}

	@Override
	public void notifyDataManagerChange(final DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if(CONTAINER_NAME.equals(key)) {
			final String containerName = this.getDataManager().get(CONTAINER_NAME);
			// make sure a container is registered for this material
			final GolemContainer cont = Entities.getEntityContainer(containerName);
			if(cont != null) {
				// actually use the container to update golem stats
				this.updateContainerStats(cont);
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
		EntityHelpers.fireOnInitAI(this, this.container.getEntityName());
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(CONTAINER_NAME, Entities.makeGolemKey(Materials.EMPTY));
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
	protected void collideWithEntity(final Entity entityIn) {
		super.collideWithEntity(entityIn);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		EntityHelpers.fireOnLivingUpdate(this, this.container.getEntityName());
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(super.attackEntityFrom(source, amount)) {
			EntityHelpers.fireOnHurt(this, this.container.getEntityName(), source, amount);
			return true;
		}
		return false;
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
		if(this.getContainer() != null) {
			compound.setString(KEY_CONTAINER_NAME, this.getContainer().getEntityName().toString());
		}
		EntityHelpers.fireOnWriteNBT(this, this.container.getEntityName(), compound);
	}
	
	@Override
	public void readEntityFromNBT(final NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if(compound.hasKey(KEY_CONTAINER_NAME)) {
			final String name = compound.getString(KEY_CONTAINER_NAME);
			this.setContainer(Entities.getEntityContainer(name));
		}
		EntityHelpers.fireOnReadNBT(this, this.container.getEntityName(), compound);
	}

	@Override
	public boolean attackEntityAsMob(final Entity entityIn) {
		
		final float baseAttack = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 
				baseAttack + this.rand.nextInt(Math.max(2, (int)(baseAttack * 2.0F))));
		// use reflection to reset 'attackTimer' field
		ReflectionHelper.setPrivateValue(EntityIronGolem.class, this, 10, "field_70855_f", "attackTimer");
		this.world.setEntityState(this, KEY_ATTACK);

		if (flag) {
			entityIn.motionY += 0.4000000059604645D;
			this.applyEnchantments(this, entityIn);
			EntityHelpers.fireOnAttack(this, this.container.getEntityName(), entityIn);
		}
		this.playSound(getAttackSound(), 1.0F, 0.9F + rand.nextFloat() * 0.2F);
		return flag;
	}

	@Override
	@Nullable
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return container.getHurtSound();
	}

	@Override
	@Nullable
	protected SoundEvent getDeathSound() {
		return container.getDeathSound();
	}
	
	protected SoundEvent getStepSound() {
		return container.getLivingSound();
	}
	
	protected SoundEvent getAttackSound() {
		return container.getLivingSound();
	}

	@Override
	protected void playStepSound(final BlockPos pos, final Block blockIn) {
		this.playSound(getStepSound(), 1.0F, 0.9F + rand.nextFloat() * 0.2F);
	}

	@Nullable
	@Override
	protected ResourceLocation getLootTable() {
		return this.container.getLootTable();
	}

	@Override
	public void onDeath(final DamageSource cause) {
		EntityHelpers.fireOnDeath(this, this.container.getEntityName(), cause);
		super.onDeath(cause);
	}
	
	@Override
	public void fall(float distance, float damageMultiplier) {
		if (this.getContainer() != null && this.getContainer().hasFallDamage()) {
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
				final BlockPos blockBelow = getBlockBelow();
				IBlockState iblockstate = this.world.getBlockState(blockBelow);

				if (iblockstate.getMaterial() != Material.AIR) {
					SoundType soundtype = iblockstate.getBlock().getSoundType(iblockstate, world, blockBelow, this);
					this.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.5F,
							soundtype.getPitch() * 0.75F);
				}
			}
		}
	}
	
	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		EntityHelpers.fireOnFirstSpawned(this, this.container.getEntityName());
		return livingdata;
	}

	/**
	 * Called when a user uses the creative pick block button on this entity.
	 * @param target The full target the player is looking at
	 * @return A ItemStack to add to the player's inventory, Null if nothing should be added.
	 */
	@Override
	public ItemStack getPickedResult(final RayTraceResult target) {
		if(this.container.getMMDMaterial().hasBlock(Names.BLOCK)) {
			return this.container.getMMDMaterial().getItemStack(Names.BLOCK);
		}
		return super.getPickedResult(target);
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
		return this.container.getMMDMaterial();
	}
	
	@Override
	public void setContainer(final EntityContainer containerIn) {
		if(containerIn != null) {
			this.getDataManager().set(CONTAINER_NAME, containerIn.getEntityName().toString());
		}
	}
	
	@Override
	public GolemContainer getContainer() {
		return this.container;
	}

	@Override
	public EntityCustomGolem getEntity() {
		return this;
	}

	protected BlockPos getBlockBelow() {
		int j = MathHelper.floor(this.posX);
		int k = MathHelper.floor(this.posY - 0.20000000298023224D);
		int l = MathHelper.floor(this.posZ);
		return new BlockPos(j, k, l);
	}

}
