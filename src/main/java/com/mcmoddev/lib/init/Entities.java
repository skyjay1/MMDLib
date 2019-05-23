package com.mcmoddev.lib.init;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mcmoddev.lib.data.SharedStrings;
import com.mcmoddev.lib.entity.GolemContainer;
import com.mcmoddev.lib.material.MMDMaterial;

public class Entities {
	
	private static final Map<MMDMaterial, GolemContainer> customGolems = new HashMap<>();
	
	protected Entities() {
		throw new IllegalAccessError(SharedStrings.NOT_INSTANTIABLE);
	}
	
	protected static void addGolem(final GolemContainer container) {
		final MMDMaterial mat = container.getMMDMaterial();
		if(null == mat) {
			com.mcmoddev.lib.MMDLib.logger.warn("Skipping null MMDMaterial object in GolemContainer registration.");
			return;
		}
		if(customGolems.containsKey(mat)) {
			com.mcmoddev.lib.MMDLib.logger.warn("Cannot register GolemContainer for MMDMaterial '%s' as it has already been registered.", mat.getName());
		}
		customGolems.put(mat, container);
	}
	
	public static Map<MMDMaterial, GolemContainer> getGolemRegistry() {
		return customGolems;
	}
	
	@Nullable
	public static GolemContainer getContainer(final MMDMaterial mat) {
		return customGolems.get(mat);
	}

}
