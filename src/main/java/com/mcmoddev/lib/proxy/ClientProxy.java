package com.mcmoddev.lib.proxy;

import javax.annotation.Nonnull;

import com.mcmoddev.lib.client.registrations.RegistrationHelper;
import com.mcmoddev.lib.client.renderer.RenderCustomGolem;
import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.entity.EntityCustomGolem;
import com.mcmoddev.lib.init.*;
import com.mcmoddev.lib.network.MMDMessages;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;

/**
 * Base Metals Client Proxy
 *
 * @author Jasmine Iwanek
 *
 */
public class ClientProxy extends CommonProxy {
	
	private static final IRenderFactory<EntityCustomGolem> FACTORY_GOLEM = RenderCustomGolem::new;

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		MMDMessages.client_init();
		MinecraftForge.EVENT_BUS.register(this);
	}
	/**
	 * Registers Block and Item models for this mod.
	 *
	 * @param event The Event.
	 */
	@SubscribeEvent
	public static void registerModels(final ModelRegistryEvent event) {
		for (final String name : Items.getItemRegistry().keySet()) {
			if (!name.endsWith(Names.ANVIL.toString())) {
				RegistrationHelper.registerItemRender(name);
			}
		}

		for (final String name : Blocks.getBlockRegistry().keySet()) {
			RegistrationHelper.registerBlockRender(name);
		}
	
		for (final String name : Fluids.getFluidBlockRegistry().keySet()) {
			RegistrationHelper.registerFluidRender(name);
		}
	}
	
	@SubscribeEvent
	public void registerEntities(final RegistryEvent.Register<EntityEntry> event) {
		// TODO call ALL entity renders
		registerGolemRender(com.mcmoddev.lib.entity.EntityCustomGolem.class);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		for (final String name : Items.getItemRegistry().keySet()) {
			registerRenderOuter(Items.getItemByName(name));
		}

		for (final String name : Blocks.getBlockRegistry().keySet()) {
			registerRenderOuter(Blocks.getBlockByName(name));
		}
		
	}
	
	public static void registerGolemRender(@Nonnull Class<? extends EntityCustomGolem> cls) {
		// TODO safety checks?
		RenderingRegistry.registerEntityRenderingHandler(cls, FACTORY_GOLEM);
		// DEBUG
		com.mcmoddev.lib.MMDLib.logger.info("Registered EntityCustomGolem render handler");
	}

	private void registerRenderOuter ( Item item ) {
		if (item != null) {
			registerRender(item, Items.getNameOfItem(item));
		}
	}

	private void registerRenderOuter ( Block block ) {
		if ((block instanceof BlockDoor) || (block instanceof BlockSlab)) {
			return; // do not add door blocks or slabs
		}

		if (block != null) {
			registerRender(Item.getItemFromBlock(block), Blocks.getNameOfBlock(block));
		}
	}

	public void registerRender(Item item, String name) {
		String resourceDomain = item.getRegistryName().getNamespace();
		ResourceLocation resLoc = new ResourceLocation(resourceDomain, name);
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(resLoc, "inventory"));
	}
}
