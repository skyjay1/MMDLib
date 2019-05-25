package com.mcmoddev.lib.entity;

import javax.annotation.Nullable;

import com.mcmoddev.lib.init.Entities;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCustomMob extends EntityMob implements IMMDEntity<EntityCustomMob> {

	protected static final DataParameter<String> CONTAINER_NAME = EntityDataManager.<String>createKey(EntityCustomAnimal.class, DataSerializers.STRING);
	private static final String KEY_CONTAINER_NAME = "ContainerName";
	private MobContainer container = MobContainer.EMPTY_MOB_CONTAINER;
	
	public EntityCustomMob(World worldIn) {
		super(worldIn);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(CONTAINER_NAME, Entities.makeKey(MobContainer.EMPTY_MOB_CONTAINER));
	}
	
	/**
	 * Adjusts all stats, textures, tints, etc. of this golem based on the
	 * values in the given {@link GolemContainer}
	 * @param cont
	 **/
	private void updateContainerStats(final MobContainer cont) {
		this.container = cont;
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(cont.getHealth());
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(cont.getMoveSpeed());
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(cont.getKnockbackResist());
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(cont.getAttack());
		// add AI
		EntityHelpers.addTaskIfAbsent(this, 5, new EntityAIWanderAvoidWater(this, cont.getMoveSpeed() * 4.0D));
		EntityHelpers.addTaskIfAbsent(this, 6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		EntityHelpers.addTaskIfAbsent(this, 7, new EntityAILookIdle(this));
        // add optional AI
		if(cont.canSwim()) {
			EntityHelpers.addTaskIfAbsent(this, 0, new EntityAISwimming(this));
		}
		if(cont.leapsAtTarget()) {
			EntityHelpers.addTaskIfAbsent(this, 3, new EntityAILeapAtTarget(this, 0.4F));
		}
		// add hostility level
		switch(cont.getHostility()) {
		case HOSTILE:
			EntityHelpers.addTaskIfAbsent(this, 1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
			EntityHelpers.addTaskIfAbsent(this, 3, new EntityAINearestAttackableTarget(this, EntityVillager.class, false));
			EntityHelpers.addTaskIfAbsent(this, 3, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, true));
			 // intentional omission of break statement
		case NEUTRAL:
			EntityHelpers.addTaskIfAbsent(this, 4, new EntityAILeapAtTarget(this, 0.4F));
			EntityHelpers.addTaskIfAbsent(this, 5, new EntityAIAttackMelee(this, 1.0D, true));
			break;
		case PASSIVE: default:
			EntityHelpers.addTaskIfAbsent(this, 1, new EntityAIPanic(this, cont.getMoveSpeed() * 8.0D));
			break;
		}
		
		EntityHelpers.fireOnInitAI(this);
	}
	
	@Override
	public void notifyDataManagerChange(final DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if(CONTAINER_NAME.equals(key)) {
			final String containerName = this.getDataManager().get(CONTAINER_NAME);
			// make sure a container is registered for this material
			final MobContainer cont = Entities.getEntityContainer(containerName);
			if(cont != null) {
				// actually use the container to update mob stats
				this.updateContainerStats(cont);
			} else {
				com.mcmoddev.lib.MMDLib.logger.error("Failed to update mob stats - no MobContainer was found with name '%s'", containerName);
			}
		}
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		EntityHelpers.fireOnPlayerInteract(this, player, hand);
		return super.processInteract(player, hand);
	}

	@Override
	public boolean attackEntityAsMob(final Entity entityIn) {
		if(super.attackEntityAsMob(entityIn)) {
			float f = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();

			if (this.getHeldItemMainhand().isEmpty() && this.isBurning() && this.rand.nextFloat() < f * 0.3F) {
				entityIn.setFire(2 * (int) f);
			}
			EntityHelpers.fireOnAttack(this, entityIn);
			return true;
		}
		return false;
	}

	@Nullable
	@Override
	protected ResourceLocation getLootTable() {
		return this.container != null ? this.container.getLootTable() : LootTableList.EMPTY;
	}

	@Override
	public void onLivingUpdate() {
		if (this.world.isDaytime() && !this.world.isRemote && !this.isChild() && this.shouldBurnInDay()) {
			float f = this.getBrightness();

			if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world
					.canSeeSky(new BlockPos(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ))) {
				boolean flag = true;
				ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

				if (!itemstack.isEmpty()) {
					if (itemstack.isItemStackDamageable()) {
						itemstack.setItemDamage(itemstack.getItemDamage() + this.rand.nextInt(2));

						if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
							this.renderBrokenItemStack(itemstack);
							this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
						}
					}

					flag = false;
				}

				if (flag) {
					this.setFire(8);
				}
			}
		}

		super.onLivingUpdate();
		EntityHelpers.fireOnLivingUpdate(this);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		EntityHelpers.fireOnHurt(this, source, amount);
		return super.attackEntityFrom(source, amount);
	}
	
	@Override
	public void onDeath(final DamageSource cause) {
		EntityHelpers.fireOnDeath(this, cause);
		super.onDeath(cause);
	}
	
	@Override
	public void writeEntityToNBT(final NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		if(this.getContainer() != null) {
			compound.setString(KEY_CONTAINER_NAME, this.getContainer().getEntityName());
		}
		EntityHelpers.fireOnWriteNBT(this, compound);
	}
	
	@Override
	public void readEntityFromNBT(final NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if(compound.hasKey(KEY_CONTAINER_NAME)) {
			final String name = compound.getString(KEY_CONTAINER_NAME);
			this.setContainer(Entities.getEntityContainer(name));
		}
		EntityHelpers.fireOnReadNBT(this, compound);
	}
	
	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		EntityHelpers.fireOnFirstSpawned(this);
		return livingdata;
	}
	
	@Override
	public String getName() {
		if (this.hasCustomName()) {
			return this.getCustomNameTag();
		} else {
			return I18n.format("mmd.entity." + getContainer().getEntityName() + ".name");
		}
	}

	@Override
	public EntityCustomMob getEntity() {
		return this;
	}

	@Override
	public void setContainer(EntityContainer containerIn) {
		if(containerIn != null) {
			this.getDataManager().set(CONTAINER_NAME, containerIn.getEntityName());
		}
	}

	@Override
	public MobContainer getContainer() {
		return this.container;
	}
	
	public boolean shouldBurnInDay() {
		return getContainer().doesBurnInDay();
	}
}
