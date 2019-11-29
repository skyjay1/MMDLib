package com.mcmoddev.lib.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.mcmoddev.lib.MMDLib;
import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.init.Entities;
import com.mcmoddev.lib.material.MMDMaterial;
import com.mcmoddev.lib.properties.EntityProperties;
import com.mcmoddev.lib.properties.IMMDEntityProperty;
import com.mcmoddev.lib.properties.MMDEntityPropertyBase;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.block.state.pattern.BlockMaterialMatcher;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;

@Mod.EventBusSubscriber(modid=MMDLib.MODID)
public class EntityHelpers {

	private static Map<MMDMaterial, BlockPattern> golemPatterns = null;
	private static final Predicate<IBlockState> IS_PUMPKIN = 
			i -> i != null && (i.getBlock() == Blocks.PUMPKIN || i.getBlock() == Blocks.LIT_PUMPKIN);

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
	

	/** Only Applicable for EntityLiving **/
	public static boolean addTaskIfAbsent(final EntityLiving entity, final int priority, final EntityAIBase ai) {
		for(final EntityAITaskEntry entry : entity.tasks.taskEntries) {
			if(entry.action.getClass() == ai.getClass()) {
				return false;
			}
		}
		entity.tasks.addTask(priority, ai);	
		return true;
	}
	
	public static boolean removeTaskIfPresent(final EntityLiving entity, final Class<? extends EntityAIBase> ai) {
		int removed = 0;
		for(final EntityAITaskEntry entry : entity.tasks.taskEntries) {
			if(entry.action.getClass() == ai) {
				entity.tasks.removeTask(entry.action);
				removed++;
			}
		}
		return removed > 0;
	}
	
	@SubscribeEvent
	public static void registerEntities(final RegistryEvent.Register<EntityEntry> event) {
		// here we find all the entities that should be registered and... register them!
		for(final EntityEntry entry : Entities.getEntriesToRegister()) {
			event.getRegistry().register(entry);
		}
	}
	
	// DEBUG
	@SubscribeEvent
	public static void registerEntityProperties(final com.mcmoddev.lib.events.MMDLibRegisterEntityProperties event) {
		event.getRegistry().register(new MMDEntityPropertyBase("prop") {

			@Override
			public void onAttackMob(EntityLiving entity, Entity target) {
				System.out.println("on Attack Mob");
			}

			@Override
			public void onHurt(EntityLiving entity, DamageSource source, float amount) {
				System.out.println("on Hurt");
			}

			@Override
			public void onPlayerInteract(EntityLiving entity, EntityPlayer player, EnumHand hand) {
				System.out.println("on Player Interact");
			}

			@Override
			public void onInitAI(EntityLiving entity) {
				System.out.println("on Init AI");
			}

//			@Override
//			public void onTick(EntityLiving entity) {
//				System.out.println("on Tick");
//			}

			@Override
			public void onDeath(EntityLiving entity, DamageSource cause) {
				System.out.println("on Death");
			}

			@Override
			public void onFirstSpawned(EntityLiving entity) {
				System.out.println("on First Spawned");
			}

			@Override
			public void onWriteNBT(EntityLiving entity, NBTTagCompound tag) {
				System.out.println("on Write NBT");
			}

			@Override
			public void onReadNBT(EntityLiving entity, NBTTagCompound tag) {
				System.out.println("on Read NBT");
			}

			@Override
			public Set<ResourceLocation> getHandledEntities() {
				return Sets.newHashSet(new ResourceLocation("mmdlib", "golem_gold"));
			}

			@Override
			public Set<ListenerType> getListenerTypes() {
				return EnumSet.of(ListenerType.AI, ListenerType.ATTACK, ListenerType.DEATH, ListenerType.HURT,
						ListenerType.INTERACT, ListenerType.NBT, ListenerType.SPAWN, ListenerType.TICK);
			}
			
		});
	}
	
	/**
	 * AFTER all Entity update code has been processed.
	 **/
	public static void fireOnLivingUpdate(final EntityLiving entity, final ResourceLocation name) {
		EntityProperties.getListeners(name, IMMDEntityProperty.ListenerType.TICK)
			.forEach(p -> p.onTick(entity));
	}

	/**
	 * AFTER Entity attacks target.
	 **/
	public static void fireOnAttack(final EntityLiving entity, final ResourceLocation name, final Entity target) {
		EntityProperties.getListeners(name, IMMDEntityProperty.ListenerType.ATTACK)
			.forEach(p -> p.onAttackMob(entity, target));
	}

	/**
	 * AFTER Entity takes damage.
	 **/
	public static void fireOnHurt(final EntityLiving entity, final ResourceLocation name, final DamageSource source, float amount) {
		EntityProperties.getListeners(name, IMMDEntityProperty.ListenerType.HURT)
			.forEach(p -> p.onHurt(entity, source, amount));
	}

	/**
	 * BEFORE any death code has been processed
	 **/
	public static void fireOnDeath(final EntityLiving entity, final ResourceLocation name, final DamageSource cause) {
		EntityProperties.getListeners(name, IMMDEntityProperty.ListenerType.DEATH)
			.forEach(p -> p.onDeath(entity, cause));
	}
	
	/**
	 * AFTER all Entity AI has already been loaded
	 **/
	public static void fireOnInitAI(final EntityLiving entity, final ResourceLocation name) {
		EntityProperties.getListeners(name, IMMDEntityProperty.ListenerType.AI)
			.forEach(p -> p.onInitAI(entity));
	}
	
	/**
	 * AFTER all Entity NBT has already been written
	 **/
	public static void fireOnWriteNBT(final EntityLiving entity, final ResourceLocation name, final NBTTagCompound tag) {
		EntityProperties.getListeners(name, IMMDEntityProperty.ListenerType.NBT)
			.forEach(p -> p.onWriteNBT(entity, tag));
	}
	
	/**
	 * AFTER all Entity NBT has already been read
	 **/
	public static void fireOnReadNBT(final EntityLiving entity, final ResourceLocation name, final NBTTagCompound tag) {
		EntityProperties.getListeners(name, IMMDEntityProperty.ListenerType.NBT)
			.forEach(p -> p.onReadNBT(entity, tag));
	}
	
	/**
	 * BEFORE any other interaction code has been processed
	 **/
	public static void fireOnPlayerInteract(final EntityLiving entity, final ResourceLocation name, final EntityPlayer player, final EnumHand hand) {
		EntityProperties.getListeners(name, IMMDEntityProperty.ListenerType.INTERACT)
			.forEach(p -> p.onPlayerInteract(entity, player, hand));
	}
	
	public static void fireOnFirstSpawned(final EntityLiving entity, final ResourceLocation name) {
		EntityProperties.getListeners(name, IMMDEntityProperty.ListenerType.SPAWN)
			.forEach(p -> p.onFirstSpawned(entity));
	}
	
	/**
	 * Handles the event where a player might be using a pumpkin itemblock,
	 * in which case we manually intercept, place the block without any of its
	 * "can stay here" checks, and attempt to spawn a golem using the blocks
	 * if any are detected. 
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
						// spawn the golem (if a match is found)
						if(!event.getWorld().isRemote) {
							EntityCustomGolem golem = trySpawnGolem(event.getWorld(), pumpkinPos);
							if(golem != null) {
								// location, container, etc. were set in #trySpawnGolem
								event.getWorld().spawnEntity(golem);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Adapted from BlockPumpkin code
	 **/
	private static EntityCustomGolem trySpawnGolem(World worldIn, BlockPos pos) {
		
		BlockPattern patternMatcher = null;
		BlockPattern.PatternHelper patternHelper = null;
		MMDMaterial material = null;
		// check a pattern matcher for each registered material
		for(Entry<MMDMaterial, BlockPattern> entry : getPatterns().entrySet()) {
			patternHelper = entry.getValue().match(worldIn, pos);
			if (patternHelper != null) {
				patternMatcher = entry.getValue();
				material = entry.getKey();
				break;
			}
		}

		if (patternMatcher != null && patternHelper != null && material != null) {
			// remove the blocks that were used
			for (int j = 0; j < patternMatcher.getPalmLength(); ++j) {
				for (int k = 0; k < patternMatcher.getThumbLength(); ++k) {
					worldIn.setBlockState(patternHelper.translateOffset(j, k, 0).getPos(), Blocks.AIR.getDefaultState(),
							2);
				}
			}
			// create the entity golem
			BlockPos blockpos = patternHelper.translateOffset(1, 2, 0).getPos();
			EntityCustomGolem entitycustomgolem = (EntityCustomGolem) EntityList.newEntity(EntityCustomGolem.class,
					worldIn);
			entitycustomgolem.setPlayerCreated(true);
			entitycustomgolem.setLocationAndAngles((double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.05D,
					(double) blockpos.getZ() + 0.5D, 0.0F, 0.0F);
			final ResourceLocation key = new ResourceLocation("mmdlib", Entities.PREFIX_GOLEM.concat(material.getName()));
			
			// DEBUG:
			System.out.println("looking for entity container with name '" + key + "'...");
			
			if (Entities.hasEntityContainer(key)) {
				System.out.println("...found!");
				entitycustomgolem.setContainer(Entities.getEntityContainer(key));
			} else {
				com.mcmoddev.lib.MMDLib.logger
						.error("Somehow failed to get the GolemContainer after using the GolemContainer? wtf?");
			}
			// trigger acheivements
			for (EntityPlayerMP entityplayermp1 : worldIn.getEntitiesWithinAABB(EntityPlayerMP.class,
					entitycustomgolem.getEntityBoundingBox().grow(5.0D))) {
				CriteriaTriggers.SUMMONED_ENTITY.trigger(entityplayermp1, entitycustomgolem);
			}
			// spawn particles
			for (int j1 = 0; j1 < 120; ++j1) {
				worldIn.spawnParticle(EnumParticleTypes.SNOWBALL, (double) blockpos.getX() + worldIn.rand.nextDouble(),
						(double) blockpos.getY() + worldIn.rand.nextDouble() * 3.9D,
						(double) blockpos.getZ() + worldIn.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
			}
			// notify that blocks were removed
			for (int k1 = 0; k1 < patternMatcher.getPalmLength(); ++k1) {
				for (int l1 = 0; l1 < patternMatcher.getThumbLength(); ++l1) {
					BlockWorldState blockworldstate1 = patternHelper.translateOffset(k1, l1, 0);
					worldIn.notifyNeighborsRespectDebug(blockworldstate1.getPos(), Blocks.AIR, false);
				}
			}
			// return the golem, ready to spawn
			return entitycustomgolem;
		}
		return null;
	}
	
	/**
	 * Get or create the golem pattern matchers for each
	 * {@link MMDMaterial} that is registered.
	 **/
	protected static Map<MMDMaterial, BlockPattern> getPatterns() {
		if(golemPatterns == null) {
			golemPatterns = new HashMap<>();
			for (final MMDMaterial mat : Entities.getGolemMaterials()) {
				golemPatterns.put(mat, makeGolemPattern(mat.getBlock(Names.BLOCK)));
			}
			// DEBUG:
			System.out.println("initialized golemPatterns:\n" + golemPatterns);
		}
		return golemPatterns;
	}

	protected static final BlockPattern makeGolemPattern(final Block body) {
		return FactoryBlockPattern.start().aisle("~^~", "###", "~#~")
					.where('^', BlockWorldState.hasState(IS_PUMPKIN))
					.where('#', BlockWorldState.hasState(BlockMatcher.forBlock(body)))
					.where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
	}
}
