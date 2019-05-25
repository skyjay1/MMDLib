package com.mcmoddev.lib.client.renderer;

import com.mcmoddev.lib.entity.EntityCustomGolem;
import com.mcmoddev.lib.entity.GolemContainer;

import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderCustomGolem extends RenderLiving<EntityCustomGolem> {
	
	private ResourceLocation texture;

	public RenderCustomGolem(RenderManager renderManagerIn) {
		super(renderManagerIn, new ModelIronGolem(), 0.5F);
		this.addLayer(new LayerCustomGolemFlower(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCustomGolem entity) {
		return this.texture != null ? this.texture : GolemContainer.TEXTURE_METAL_GRAYSCALE_HIGH;
	}

	@Override
	public void doRender(EntityCustomGolem entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// container may be null when first rendered
		final GolemContainer cont = entity.getContainer();
		// prepare to render the colored golem
		GlStateManager.pushMatrix();
		// render the (optionally) colored layer
		if(cont != null) {
			this.texture = cont.getTexture();
			if(cont.hasTint()) {
				final float[] rgba = cont.getRGBA();
				GlStateManager.color(rgba[0], rgba[1], rgba[2], rgba[3]);
			}
			super.doRender(entity, x, y, z, entityYaw, partialTicks);
		}
		// render the overlay (vines + eyes)
		this.texture = GolemContainer.TEXTURE_OVERLAY;
		if(this.texture != null) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			super.doRender(entity, x, y, z, entityYaw, partialTicks);
		}
		GlStateManager.popMatrix();
	}
}
