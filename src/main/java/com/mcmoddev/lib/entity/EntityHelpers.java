package com.mcmoddev.lib.entity;

import javax.annotation.Nullable;

import com.mcmoddev.lib.MMDLib;
import com.mcmoddev.lib.init.Entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIFindEntityNearest;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;

@Mod.EventBusSubscriber(modid=MMDLib.MODID)
public class EntityHelpers {

	private EntityHelpers() {
		// private constructor
	}

	/**
	 *
	 * @param compound
	 * @param itemStack
	 * @return
	 */
	public static NBTTagCompound writeToNBTItemStack(final NBTTagCompound compound,
			final ItemStack itemStack) {
		final NBTTagCompound itemStackCompound = new NBTTagCompound();
		itemStack.writeToNBT(itemStackCompound);
		compound.setTag("itemstack", itemStackCompound);
		return compound;
	}

	/**
	 *
	 * @param compound
	 * @return
	 */
	public static ItemStack readFromNBTItemStack(final NBTTagCompound compound) {
		return new ItemStack(compound.getCompoundTag("itemstack"));
	}
	
	@SubscribeEvent
	public static void registerEntities(final RegistryEvent.Register<EntityEntry> event) {
		// here we find all the entities that should be registered and... register them!
		for(final EntityEntry entry : Entities.getEntriesToRegister()) {
			event.getRegistry().register(entry);
		}
	}
	
	/**
	 * Handles the event where a player might be using a pumpkin itemblock,
	 * in which case we manually intercept, place the block without any of its
	 * "can stay here" checks, and attempt to spawn a golem using the blocks
	 * detected. 
	 * <br>Author: skyjay1
	 * <br>From: Extra Golems 12.2
	 * <br>Used with permission.
	 **/
	@SubscribeEvent
	public static void onPlacePumpkin(PlayerInteractEvent.RightClickBlock event) {
		ItemStack stack = event.getItemStack();
		// check qualifications for running this event...
		if(!event.isCanceled() && !stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
			Block heldBlock = ((ItemBlock)stack.getItem()).getBlock();
			// if player is holding pumpkin or lit pumpkin, start to place the block manually
			if(heldBlock instanceof BlockPumpkin) {
				// update the location to place block
				BlockPos pumpkinPos = event.getPos();
				Block clicked = event.getWorld().getBlockState(pumpkinPos).getBlock();
				if (!clicked.isReplaceable(event.getWorld(), pumpkinPos)) {
		            pumpkinPos = pumpkinPos.offset(event.getFace());
				}
				// now we're ready to place the block
				if(event.getEntityPlayer().canPlayerEdit(pumpkinPos, event.getFace(), stack)) {
					IBlockState pumpkin = heldBlock.getDefaultState().withProperty(BlockHorizontal.FACING, 
							event.getEntityPlayer().getHorizontalFacing().getOpposite());
					// set block and trigger golem-checking
					if(event.getWorld().setBlockState(pumpkinPos, pumpkin)) {
						// cancel event and reduce itemstack
						event.setCanceled(true);
						if(!event.getEntityPlayer().isCreative()) {
							event.getItemStack().shrink(1);
						}
						//
						//
						// TODO golem spawning code here!
						//
						//
						
					}
				}
			}
		}
	}
	
	/**	 
	 * <br>Author: skyjay1
	 * <br>From: Extra Golems 12.2
	 * <br>Used with permission.
	 **/
	@SubscribeEvent
	public static void onLivingSpawned(final EntityJoinWorldEvent event) {
		// add custom 'attack golem' AI to hostile mobs. They already have this for regular iron golems
		if(event.getEntity() instanceof EntityCreature) {
			final EntityCreature creature = (EntityCreature) event.getEntity();
			if (creatureAttacksGolems(creature)) {
				for (final EntityAITasks.EntityAITaskEntry entry : creature.targetTasks.taskEntries) {
					if (entry.action instanceof EntityCustomGolem.EntityAIAttackGolem) {
						return;
					}
				}
				creature.targetTasks.addTask(3, new EntityCustomGolem.EntityAIAttackGolem(creature));
			}
		// add custom 'chase golem' AI to hostile entities that do not inherit from EntityCreature
		// (currently just EntitySlime)
		} else if(event.getEntity() instanceof EntityLiving) {
			final EntityLiving living = (EntityLiving) event.getEntity();
			if (livingAttacksGolems(living)) {
				living.targetTasks.addTask(3, new EntityAIFindEntityNearest(living, EntityCustomGolem.class));
			}
		}
	}
	
	/** 
	 * Returns true if this entity is an EntityCreature AND normally attacks Iron Golems 
	 * <br>Author: skyjay1
	 * <br>From: Extra Golems 12.2
	 * <br>Used with permission.
	 **/
	private static boolean creatureAttacksGolems(EntityCreature e) {
		return e instanceof AbstractSkeleton || e instanceof EntitySpider 
				|| e instanceof AbstractIllager
				|| (e instanceof EntityZombie && !(e instanceof EntityPigZombie));
	}
	
	/** Returns true if this entity is any EntityLivingBase AND chases after Iron Golems 
	 * <br>Author: skyjay1
	 * <br>From: Extra Golems 12.2
	 * <br>Used with permission.
	 **/
	private static boolean livingAttacksGolems(EntityLivingBase e) {
		return e instanceof EntitySlime;
	}
	
	//////// TODO all of these! //////////
	
	/**
	 * AFTER Entity has been spawned and placed in the world
	 * @param livingdata 
	 **/
	public static void fireOnSpawned(final IMMDEntity<?> entity, @Nullable IEntityLivingData livingdata) {
		
	}

	/**
	 * AFTER all Entity update code has been processed.
	 **/
	public static void fireOnLivingUpdate(final IMMDEntity<?> entity) {

	}

	/**
	 * AFTER Entity attacks target.
	 **/
	public static void fireOnAttack(final IMMDEntity<?> entity, final Entity target) {

	}

	/**
	 * BEFORE Entity takes damage.
	 **/
	public static boolean fireOnHurt(final IMMDEntity<?> entity, final DamageSource source, float amount) {
		return true;
	}

	/**
	 * BEFORE any death code has been processed
	 **/
	public static void fireOnDeath(final IMMDEntity<?> entity, final DamageSource source) {

	}
	
	/**
	 * AFTER all Entity AI has already been loaded
	 **/
	public static void fireOnInitAI(final IMMDEntity<?> entity) {

	}
	
	/**
	 * AFTER all Entity NBT has already been written
	 **/
	public static void fireOnWriteNBT(final IMMDEntity<?> entity, final NBTTagCompound tag) {

	}
	
	/**
	 * AFTER all Entity NBT has already been read
	 **/
	public static void fireOnReadNBT(final IMMDEntity<?> entity, final NBTTagCompound tag) {

	}
	
	/**
	 * BEFORE any other interaction code has been processed
	 **/
	public static void fireOnPlayerInteract(final IMMDEntity<?> entity, final EntityPlayer player, final EnumHand hand) {
		
	}

}
