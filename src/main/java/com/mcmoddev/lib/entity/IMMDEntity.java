package com.mcmoddev.lib.entity;

import com.mcmoddev.lib.init.Entities;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public interface IMMDEntity<T extends Entity, M extends EntityContainer> {
	
	T getEntity();
	
	void setContainer(final M containerIn);
	
	M getContainer();
	
	default M findContainer(final String name) {
		return Entities.getEntityContainer(new ResourceLocation(name));
	}
}
