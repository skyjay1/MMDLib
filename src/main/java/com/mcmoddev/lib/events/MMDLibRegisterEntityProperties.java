package com.mcmoddev.lib.events;

import com.mcmoddev.lib.properties.EntityProperties;
import com.mcmoddev.lib.properties.MMDEntityPropertyBase;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IContextSetter;
import net.minecraftforge.registries.IForgeRegistry;

public class MMDLibRegisterEntityProperties extends Event implements IContextSetter {
	//private final IForgeRegistry<MMDEntityPropertyBase<?>> reg;
	
	public MMDLibRegisterEntityProperties() {
		//this.reg = EntityProperties.getRegistry();
	}

//	public IForgeRegistry<MMDEntityPropertyBase<?>> getRegistry() {
//		return this.reg;
//	}
}
