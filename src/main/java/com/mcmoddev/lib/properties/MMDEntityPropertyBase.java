package com.mcmoddev.lib.properties;

import com.mcmoddev.lib.entity.IMMDEntity;

import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class MMDEntityPropertyBase<T> extends IForgeRegistryEntry.Impl<MMDEntityPropertyBase<?>> implements IMMDEntityProperty<T> {

	@Override
	public boolean hasAttackBehavior(IMMDEntity<? super T> entity) { return false; }
	@Override
	public boolean hasHurtBehavior(IMMDEntity<? super T> entity) { return false; }
	@Override
	public boolean hasCustomAI(IMMDEntity<? super T> entity) { return false; }
	@Override
	public boolean hasTickBehavior(IMMDEntity<? super T> entity) { return false; }
	@Override
	public boolean hasDeathBehavior(IMMDEntity<? super T> entity) { return false; }
	@Override
	public boolean hasSpawnBehavior(IMMDEntity<? super T> entity) { return false; }
	@Override
	public boolean hasNBTBehavior(IMMDEntity<? super T> entity) { return false; }
}
