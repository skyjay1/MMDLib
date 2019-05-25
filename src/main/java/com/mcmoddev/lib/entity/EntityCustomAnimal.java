package com.mcmoddev.lib.entity;

import javax.annotation.Nullable;

import com.mcmoddev.lib.init.Entities;
import com.mcmoddev.lib.init.Materials;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCustomAnimal extends EntityAnimal implements IMMDEntity<EntityCustomAnimal> {

	protected static final DataParameter<String> CONTAINER_NAME = EntityDataManager.<String>createKey(EntityCustomAnimal.class, DataSerializers.STRING);
	private static final String KEY_CONTAINER_NAME = "ContainerName";
	private EntityContainer container;
	
	public EntityCustomAnimal(World worldIn) {
		super(worldIn);
	}
	
	/**
	 * Adjusts all stats, textures, tints, etc. of this golem based on the
	 * values in the given {@link GolemContainer}
	 * @param cont
	 **/
	private void updateContainerStats(final AnimalContainer cont) {
		this.container = cont;
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(cont.getHealth());
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(cont.getMoveSpeed());
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(cont.getKnockbackResist());
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(cont.getAttack());
		// add AI
		this.addTaskIfAbsent(4, new EntityAIFollowParent(this, cont.getMoveSpeed() + 1.0D));
		this.addTaskIfAbsent(5, new EntityAIWanderAvoidWater(this, cont.getMoveSpeed() * 4.0D));
		this.addTaskIfAbsent(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.addTaskIfAbsent(7, new EntityAILookIdle(this));
        // add optional AI
		if(cont.canSwim()) {
			 this.addTaskIfAbsent(0, new EntityAISwimming(this));
		}
		if(cont.hasTemptItem()) {
			this.tasks.addTask(3, new EntityAITempt(this, 1.25D, cont.getTemptItem(), false));
			this.tasks.addTask(2, new EntityAIMate(this, cont.getMoveSpeed() * 4.0D));
		}
		// add hostility level
		switch(cont.getHostility()) {
		case HOSTILE:
			this.addTaskIfAbsent(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
			 // intentional omission of break statement
		case NEUTRAL:
			this.addTaskIfAbsent(4, new EntityAILeapAtTarget(this, 0.4F));
			this.addTaskIfAbsent(5, new EntityAIAttackMelee(this, 1.0D, true));
			break;
		case PASSIVE: default:
			this.addTaskIfAbsent(1, new EntityAIPanic(this, cont.getMoveSpeed() * 8.0D));
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
			final EntityContainer cont = Entities.getEntityContainer(containerName);
			if(cont instanceof AnimalContainer) {
				// actually use the container to update golem stats
				this.updateContainerStats((AnimalContainer)cont);
			} else {
				com.mcmoddev.lib.MMDLib.logger.error("Failed to update animal stats - no AnimalContainer was found with name '%s'", containerName);
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
		super.onLivingUpdate();
		EntityHelpers.fireOnLivingUpdate(this);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return EntityHelpers.fireOnHurt(this, source, amount) && super.attackEntityFrom(source, amount);
	}
	
	@Override
	public void onDeath(final DamageSource cause) {
		EntityHelpers.fireOnDeath(this, cause);
		super.onDeath(cause);
	}
	
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
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		EntityHelpers.fireOnSpawned(this, livingdata);
		return super.onInitialSpawn(difficulty, livingdata);
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
	public EntityCustomAnimal getEntity() {
		return this;
	}

	@Override
	public EntityAgeable createChild(EntityAgeable ageable) {
		EntityCustomAnimal entity = new EntityCustomAnimal(ageable.getEntityWorld());
		entity.setContainer(this.getContainer());
		return entity;
	}

	@Override
	public void setContainer(EntityContainer containerIn) {
		this.getDataManager().set(CONTAINER_NAME, containerIn.getEntityName());
	}

	@Override
	public EntityContainer getContainer() {
		return this.container;
	}

}
