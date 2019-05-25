package com.mcmoddev.lib.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;

public interface IMMDEntity<T extends Entity> {
	
	T getEntity();
	
	void setContainer(final EntityContainer containerIn);
	
	EntityContainer getContainer();
}
