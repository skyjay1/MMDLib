package com.mcmoddev.lib.entity;

import com.mcmoddev.lib.material.IMMDObject;

import net.minecraft.entity.Entity;

public interface IMMDEntity<T extends Entity> extends IMMDObject {
	
	T getEntity();
}
