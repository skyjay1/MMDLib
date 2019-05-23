package com.mcmoddev.lib.client.renderer;

import com.mcmoddev.lib.entity.EntityCustomGolem;
import com.mcmoddev.lib.entity.GolemContainer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderIronGolem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.util.ResourceLocation;

public class RenderCustomGolem extends RenderIronGolem {

	public RenderCustomGolem(RenderManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityIronGolem entity) {
		return ((EntityCustomGolem)entity).getContainer().getTexture();
	}

	@Override
	public void doRender(EntityIronGolem entity, double x, double y, double z, float entityYaw, float partialTicks) {
		final EntityCustomGolem golem = (EntityCustomGolem)entity;
		final GolemContainer cont = golem.getContainer();
		
		// prepare to render the colored golem
		GlStateManager.pushMatrix();
		if(cont.hasTint()) {
			final float[] rgba = cont.getRGBA();
			GlStateManager.color(rgba[0], rgba[1], rgba[2], rgba[3]);
		}
		this.bindTexture(cont.getTexture());
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.popMatrix();
	}
}
