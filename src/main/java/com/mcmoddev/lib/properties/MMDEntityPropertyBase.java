package com.mcmoddev.lib.properties;

import net.minecraft.entity.EntityLiving;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class MMDEntityPropertyBase extends IForgeRegistryEntry.Impl<MMDEntityPropertyBase> implements IMMDEntityProperty<EntityLiving> {

	@Override
	public boolean hasAttackBehavior(EntityLiving entity) { return false; }
	@Override
	public boolean hasHurtBehavior(EntityLiving entity) { return false; }
	@Override
	public boolean hasInteractBehavior(EntityLiving entity) { return false; }
	@Override
	public boolean hasCustomAI(EntityLiving entity) { return false; }
	@Override
	public boolean hasTickBehavior(EntityLiving entity) { return false; }
	@Override
	public boolean hasDeathBehavior(EntityLiving entity) { return false; }
	@Override
	public boolean hasSpawnBehavior(EntityLiving entity) { return false; }
	@Override
	public boolean hasNBTBehavior(EntityLiving entity) { return false; }
}
