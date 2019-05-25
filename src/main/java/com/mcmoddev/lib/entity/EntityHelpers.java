package com.mcmoddev.lib.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.mcmoddev.lib.MMDLib;
import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.init.Entities;
import com.mcmoddev.lib.material.MMDMaterial;
import com.mcmoddev.lib.properties.EntityProperties;

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
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearest;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;

@Mod.EventBusSubscriber(modid=MMDLib.MODID)
public class EntityHelpers {

	private static Map<MMDMaterial, BlockPattern> golemPatterns = null;
	private static final Predicate<IBlockState> IS_PUMPKIN = new Predicate<IBlockState>() {
		public boolean apply(@Nullable IBlockState iblockstate) {
			return iblockstate != null
					&& (iblockstate.getBlock() == Blocks.PUMPKIN || iblockstate.getBlock() == Blocks.LIT_PUMPKIN);
		}
	};

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
		
	/**
	 * AFTER Entity has been spawned and placed in the world
	 **/
	public static void fireOnFirstSpawned(final EntityLiving entity) {
		EntityProperties.getRegistry().getValuesCollection().stream()
			.filter(p -> p.hasSpawnBehavior(entity))
			.forEach(p -> p.onFirstSpawned(entity));
	}

	/**
	 * AFTER all Entity update code has been processed.
	 **/
	public static void fireOnLivingUpdate(final EntityLiving entity) {
		EntityProperties.getRegistry().getValuesCollection().stream()
			.filter(p -> p.hasTickBehavior(entity))
			.forEach(p -> p.onTick(entity));
	}

	/**
	 * AFTER Entity attacks target.
	 **/
	public static void fireOnAttack(final EntityLiving entity, final Entity target) {
		EntityProperties.getRegistry().getValuesCollection().stream()
			.filter(p -> p.hasAttackBehavior(entity))
			.forEach(p -> p.onAttackMob(entity, target));
	}

	/**
	 * BEFORE Entity takes damage.
	 **/
	public static void fireOnHurt(final EntityLiving entity, final DamageSource source, float amount) {
		EntityProperties.getRegistry().getValuesCollection().stream()
			.filter(p -> p.hasHurtBehavior(entity))
			.forEach(p -> p.onHurt(entity, source, amount));
	}

	/**
	 * BEFORE any death code has been processed
	 **/
	public static void fireOnDeath(final EntityLiving entity, final DamageSource cause) {
		EntityProperties.getRegistry().getValuesCollection().stream()
			.filter(p -> p.hasDeathBehavior(entity))
			.forEach(p -> p.onDeath(entity, cause));
	}
	
	/**
	 * AFTER all Entity AI has already been loaded
	 **/
	public static void fireOnInitAI(final EntityLiving entity) {
		EntityProperties.getRegistry().getValuesCollection().stream()
			.filter(p -> p.hasCustomAI(entity))
			.forEach(p -> p.onInitAI(entity));
	}
	
	/**
	 * AFTER all Entity NBT has already been written
	 **/
	public static void fireOnWriteNBT(final EntityLiving entity, final NBTTagCompound tag) {
		EntityProperties.getRegistry().getValuesCollection().stream()
			.filter(p -> p.hasNBTBehavior(entity))
			.forEach(p -> p.onWriteNBT(entity, tag));
	}
	
	/**
	 * AFTER all Entity NBT has already been read
	 **/
	public static void fireOnReadNBT(final EntityLiving entity, final NBTTagCompound tag) {
		EntityProperties.getRegistry().getValuesCollection().stream()
			.filter(p -> p.hasNBTBehavior(entity))
			.forEach(p -> p.onReadNBT(entity, tag));
	}
	
	/**
	 * BEFORE any other interaction code has been processed
	 **/
	public static void fireOnPlayerInteract(final EntityLiving entity, final EntityPlayer player, final EnumHand hand) {
		EntityProperties.getRegistry().getValuesCollection().stream()
			.filter(p -> p.hasInteractBehavior(entity))
			.forEach(p -> p.onPlayerInteract(entity, player, hand));
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
			EntityCustomGolem entityirongolem = (EntityCustomGolem) EntityList.newEntity(EntityCustomGolem.class,
					worldIn);
			entityirongolem.setPlayerCreated(true);
			entityirongolem.setLocationAndAngles((double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.05D,
					(double) blockpos.getZ() + 0.5D, 0.0F, 0.0F);
			final String key = Entities.makeGolemKey(material);
			if (Entities.hasEntityContainer(key)) {
				entityirongolem.setContainer(Entities.getEntityContainer(key));
			} else {
				com.mcmoddev.lib.MMDLib.logger
						.error("Somehow failed to get the GolemContainer after using the GolemContainer? wtf?");
			}
			// trigger acheivements
			for (EntityPlayerMP entityplayermp1 : worldIn.getEntitiesWithinAABB(EntityPlayerMP.class,
					entityirongolem.getEntityBoundingBox().grow(5.0D))) {
				CriteriaTriggers.SUMMONED_ENTITY.trigger(entityplayermp1, entityirongolem);
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
			return entityirongolem;
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
