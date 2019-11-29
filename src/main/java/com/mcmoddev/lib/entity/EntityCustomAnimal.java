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
	private EntityContainer container = AnimalContainer.EMPTY_ANIMAL_CONTAINER;
	
	public EntityCustomAnimal(World worldIn) {
		super(worldIn);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(CONTAINER_NAME, AnimalContainer.EMPTY_ANIMAL_CONTAINER.getEntityName().toString());
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
		EntityHelpers.addTaskIfAbsent(this, 4, new EntityAIFollowParent(this, 1.2D));
		EntityHelpers.addTaskIfAbsent(this, 5, new EntityAIWanderAvoidWater(this, 0.7D));
		EntityHelpers.addTaskIfAbsent(this, 6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		EntityHelpers.addTaskIfAbsent(this, 7, new EntityAILookIdle(this));
        // add optional AI
		if(cont.canSwim()) {
			 EntityHelpers.addTaskIfAbsent(this, 0, new EntityAISwimming(this));
		}
		if(cont.hasTemptItem()) {
			EntityHelpers.addTaskIfAbsent(this, 3, new EntityAITempt(this, 1.0D, cont.getTemptItem(), false));
			EntityHelpers.addTaskIfAbsent(this, 2, new EntityAIMate(this, 1.0D));
		}
		// add hostility level
		switch(cont.getHostility()) {
		case HOSTILE:
			EntityHelpers.addTaskIfAbsent(this, 1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
			 // intentional omission of break statement
		case NEUTRAL:
			EntityHelpers.addTaskIfAbsent(this, 4, new EntityAILeapAtTarget(this, 0.4F));
			EntityHelpers.addTaskIfAbsent(this, 5, new EntityAIAttackMelee(this, 1.0D, true));
			break;
		case PASSIVE: default:
			EntityHelpers.addTaskIfAbsent(this, 1, new EntityAIPanic(this, 1.2D));
			break;
		}
		
		EntityHelpers.fireOnInitAI(this, this.container.getEntityName());
	}
	
	@Override
	public void notifyDataManagerChange(final DataParameter<?> key) {
		super.notifyDataManagerChange(key);
		if(CONTAINER_NAME.equals(key)) {
			final String containerName = this.getDataManager().get(CONTAINER_NAME);
			// make sure a container is registered for this material
			final AnimalContainer cont = Entities.getEntityContainer(new ResourceLocation(containerName));
			if(cont != null) {
				// actually use the container to update animal stats
				this.updateContainerStats(cont);
			} else {
				com.mcmoddev.lib.MMDLib.logger.error("Failed to update animal stats - no AnimalContainer was found with name '%s'", containerName);
			}
		}
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		EntityHelpers.fireOnPlayerInteract(this, this.container.getEntityName(), player, hand);
		return super.processInteract(player, hand);
	}

	@Override
	public boolean attackEntityAsMob(final Entity entityIn) {
		if(super.attackEntityAsMob(entityIn)) {
			EntityHelpers.fireOnAttack(this, this.container.getEntityName(), entityIn);
			return true;
		}
		return false;
	}

	@Nullable
	@Override
	protected ResourceLocation getLootTable() {
		return this.container.getLootTable();
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
	public void onDeath(final DamageSource cause) {
		EntityHelpers.fireOnDeath(this, this.container.getEntityName(), cause);
		super.onDeath(cause);
	}
	
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
			this.setContainer(Entities.getEntityContainer(new ResourceLocation(name)));
		}
		EntityHelpers.fireOnReadNBT(this, this.container.getEntityName(), compound);
	}
	
	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		EntityHelpers.fireOnFirstSpawned(this, this.container.getEntityName());
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
		if(containerIn != null) {
			this.getDataManager().set(CONTAINER_NAME, containerIn.getEntityName().toString());
		}
	}

	@Override
	public EntityContainer getContainer() {
		return this.container;
	}

}
