package com.mcmoddev.lib.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;

public interface IMMDEntity<T extends EntityLiving> {
	
	T getEntity();
	
	void setContainer(final EntityContainer containerIn);
	
	EntityContainer getContainer();
	
	default boolean addTaskIfAbsent(final int priority, final EntityAIBase ai) {
		for(final EntityAITaskEntry entry : getEntity().tasks.taskEntries) {
			if(entry.action.getClass() == ai.getClass()) {
				return false;
			}
		}
		getEntity().tasks.addTask(priority, ai);
		return true;
	}
}
