package oortcloud.modulardrills.items;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import oortcloud.modulardrills.ModularDrills;
import oortcloud.modulardrills.libs.References;

import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The original code of AOE part is belonged to SlimeKnights who is the author
 * of TinkersConstruct
 */

public class ItemModularDrill extends ItemTool implements IElectricItem, IBoxable, IItemHudInfo {

	private static final Material[] materials = { Material.rock, Material.iron, Material.ice, Material.glass, Material.piston, Material.anvil, Material.grass, Material.ground, Material.sand,
			Material.snow, Material.craftedSnow, Material.clay };
	private static final double operationEnergyCost = 50;
	private static final double[] operationEnergyCostMultiplierByMotor = new double[] { 1, 1.78, 3.17, 5.64, 10.04, 17.87, 31.87, 56.62, 100 };
	private static final double[] operationEnergyCostDividerByTip = new double[] { 1, 1.5, 2.25, 3.38, 5.06, 7.59 };
	private static final double[] maxchargeByBattery = new double[] { 10000, 40000, 100000, 250000, 500000, 1000000, 2500000, 5000000 };
	private static final double[] transferLimitByBattery = new double[] { 100, 250, 500, 1000, 2500, 5000, 10000, 25000 };
	private static final int[] tierByBattery = new int[] { 1, 1, 2, 2, 3, 3, 4, 4 };
	private static final float[] digSpeedMultiplierByMotor = new float[] { 12.0F, 18.0F, 27.0F, 40.5F, 60.8F, 91.1F, 136.7F, 205.0F };
	private static final float[] digSpeedDividerByTip = new float[] { 1.0F, 1.0F, 1.0F, 9.0F, 25.0F, 49.0F };
	private static final int[] digRadiusByTip = new int[] { 0, 0, 0, 1, 2, 3 };
	private static final int[] harvestLevelByTip = new int[] { 2, 2, 3, 4, 5, 6 };

	protected ItemModularDrill() {
		super(1.0F, ToolMaterial.IRON, Sets.newHashSet());
		setCreativeTab(ModularDrills.tabModulardrills);
		setUnlocalizedName(References.RESOURCESPREFIX + "modulardrill");
		ModItems.register(this);

		setMaxStackSize(1);
		setNoRepair();
		setHarvestLevel("pickaxe", 0);
		setHarvestLevel("shovel", 0);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		this.itemIcon = iconRegister.registerIcon(ModItems.getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player) {
		// only effective materials matter. We don't want to aoe when beraking
		// dirt with a hammer.
		Block block = player.worldObj.getBlock(x, y, z);
		int meta = player.worldObj.getBlockMetadata(x, y, z);

		if (block == null || !isToolEffective(stack, block, meta))
			return super.onBlockStartBreak(stack, x, y, z, player);

		// need energy to use
		if (!ElectricItem.manager.canUse(stack, getOperationEnergyCost(stack)))
			return super.onBlockStartBreak(stack, x, y, z, player);

		MovingObjectPosition mop = raytraceFromEntity(player.worldObj, player, false, 4.5d);
		if (mop == null)
			return super.onBlockStartBreak(stack, x, y, z, player);
		int sideHit = mop.sideHit;
		// int sideHit = Minecraft.getMinecraft().objectMouseOver.sideHit;

		int breakRadius = getDigRadius(stack);
		int breakDepth = 0;

		if (breakRadius == 0)
			return super.onBlockStartBreak(stack, x, y, z, player);

		// we successfully destroyed a block. time to do AOE!
		int xRange = breakRadius;
		int yPosRange = 2 * breakRadius - 1;
		int yNegRange = 1;
		int zRange = breakDepth;
		switch (sideHit) {
		case 0:
		case 1:
			yPosRange = breakDepth;
			yNegRange = breakDepth;
			zRange = breakRadius;
			break;
		case 2:
		case 3:
			xRange = breakRadius;
			zRange = breakDepth;
			break;
		case 4:
		case 5:
			xRange = breakDepth;
			zRange = breakRadius;
			break;
		}

		for (int xPos = x - xRange; xPos <= x + xRange; xPos++)
			for (int yPos = y - yNegRange; yPos <= y + yPosRange; yPos++)
				for (int zPos = z - zRange; zPos <= z + zRange; zPos++) {
					// don't break the originally already broken block, duh
					if (xPos == x && yPos == y && zPos == z)
						continue;

					if (!super.onBlockStartBreak(stack, xPos, yPos, zPos, player))
						if (ElectricItem.manager.canUse(stack, getOperationEnergyCost(stack)))
							breakExtraBlock(player.worldObj, xPos, yPos, zPos, sideHit, player, x, y, z);
				}

		return super.onBlockStartBreak(stack, x, y, z, player);
	}

	private boolean isToolEffective(ItemStack stack, Block block, int meta) {
		String tool = block.getHarvestTool(meta);

		if (tool != null && (tool.equals("pickaxe") || tool.equals("shovel")))
			return true;

		else
			return isEffective(block.getMaterial());
	}

	private boolean isEffective(Material material) {
		for (Material m : materials)
			if (m == material)
				return true;

		return false;
	}

	public static MovingObjectPosition raytraceFromEntity(World world, Entity player, boolean par3, double range) {
		float f = 1.0F;
		float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
		float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
		double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double) f;
		if (!world.isRemote && player instanceof EntityPlayer)
			d1 += 1.62D;
		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
		Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = range;
		if (player instanceof EntityPlayerMP) {
			d3 = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
		}
		Vec3 vec31 = vec3.addVector((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
		return world.func_147447_a(vec3, vec31, par3, !par3, par3);
	}

	protected void breakExtraBlock(World world, int x, int y, int z, int sidehit, EntityPlayer playerEntity, int refX, int refY, int refZ) {
		// prevent calling that stuff for air blocks, could lead to unexpected
		// behaviour since it fires events
		if (world.isAirBlock(x, y, z))
			return;

		// what?
		if (!(playerEntity instanceof EntityPlayerMP))
			return;
		EntityPlayerMP player = (EntityPlayerMP) playerEntity;

		// check if the block can be broken, since extra block breaks shouldn't
		// instantly break stuff like obsidian
		// or precious ores you can't harvest while mining stone
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		// only effective materials
		if (!isToolEffective(playerEntity.getHeldItem(), block, meta))
			return;

		Block refBlock = world.getBlock(refX, refY, refZ);
		float refStrength = ForgeHooks.blockStrength(refBlock, player, world, refX, refY, refZ);
		float strength = ForgeHooks.blockStrength(block, player, world, x, y, z);

		// only harvestable blocks that aren't impossibly slow to harvest
		if (!ForgeHooks.canHarvestBlock(block, player, meta) || refStrength / strength > 10f)
			return;

		// send the blockbreak event
		BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(world, player.theItemInWorldManager.getGameType(), player, x, y, z);
		if (event.isCanceled())
			return;

		if (player.capabilities.isCreativeMode) {
			block.onBlockHarvested(world, x, y, z, meta, player);
			if (block.removedByPlayer(world, player, x, y, z, false))
				block.onBlockDestroyedByPlayer(world, x, y, z, meta);

			// send update to client
			if (!world.isRemote) {
				player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
			}
			return;
		}

		// callback to the tool the player uses. Called on both sides. This
		// damages the tool n stuff.
		player.getCurrentEquippedItem().func_150999_a(world, block, x, y, z, player);

		// server sided handling
		if (!world.isRemote) {
			// serverside we reproduce ItemInWorldManager.tryHarvestBlock

			// ItemInWorldManager.removeBlock
			block.onBlockHarvested(world, x, y, z, meta, player);

			if (block.removedByPlayer(world, player, x, y, z, true)) // boolean
																		// is if
																		// block
																		// can
																		// be
																		// harvested,
																		// checked
																		// above
			{
				block.onBlockDestroyedByPlayer(world, x, y, z, meta);
				block.harvestBlock(world, player, x, y, z, meta);
				block.dropXpOnBlockBreak(world, x, y, z, event.getExpToDrop());
			}

			// always send block update to client
			player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
		}
		// client sided handling
		else {
			// PlayerControllerMP pcmp =
			// Minecraft.getMinecraft().playerController;
			// clientside we do a
			// "this clock has been clicked on long enough to be broken" call.
			// This should not send any new packets
			// the code above, executed on the server, sends a block-updates
			// that give us the correct state of the block we destroy.

			// following code can be found in
			// PlayerControllerMP.onPlayerDestroyBlock
			world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
			if (block.removedByPlayer(world, player, x, y, z, true)) {
				block.onBlockDestroyedByPlayer(world, x, y, z, meta);
			}
			// callback to the tool
			ItemStack itemstack = player.getCurrentEquippedItem();
			if (itemstack != null) {
				itemstack.func_150999_a(world, block, x, y, z, player);

				if (itemstack.stackSize == 0) {
					player.destroyCurrentEquippedItem();
				}
			}

			// send an update to the server, so we get an update back
			/*
			 * if(PHConstruct.extraBlockUpdates)
			 * Minecraft.getMinecraft().getNetHandler().addToSendQueue(new
			 * C07PacketPlayerDigging(2, x,y,z,
			 * Minecraft.getMinecraft().objectMouseOver.sideHit));
			 */
		}
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float xOffset, float yOffset, float zOffset) {
		ElectricItem.manager.use(stack, 0.0D, player);
		return super.onItemUse(stack, player, world, x, y, z, side, xOffset, yOffset, zOffset);
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		ElectricItem.manager.use(stack, 0.0D, player);
		return super.onItemRightClick(stack, world, player);
	}

	@Override
	public boolean hitEntity(ItemStack p_77644_1_, EntityLivingBase p_77644_2_, EntityLivingBase p_77644_3_) {
		return true;
	}

	@Override
	public int getItemEnchantability() {
		return 0;
	}

	public boolean getIsRepairable(ItemStack p_82789_1_, ItemStack p_82789_2_) {
		return false;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass) {
		if (toolClass.equals("pickaxe") || toolClass.equals("shovel")) {
			return harvestLevelByTip[getNBTTipTier(stack)];
		}
		return -1;
	}

	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta) {
		double operationEnergyCost = getOperationEnergyCost(stack);
		if (isToolEffective(stack, block, meta) && ElectricItem.manager.canUse(stack, operationEnergyCost)) {
			return getDigSpeed(stack);
		}
		return super.getDigSpeed(stack, block, meta);
	}

	public float getDigSpeed(ItemStack stack) {
		return digSpeedMultiplierByMotor[getNBTMotorTier(stack)] / digSpeedDividerByTip[getNBTTipTier(stack)];
	}

	public int getDigRadius(ItemStack stack) {
		return digRadiusByTip[getNBTTipTier(stack)];
	}

	public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World, Block block, int par4, int par5, int par6, EntityLivingBase par7EntityLiving) {
		if (block.getBlockHardness(par2World, par4, par5, par6) != 0.0D) {
			double operationEnergyCost = getOperationEnergyCost(par1ItemStack);
			if (par7EntityLiving != null)
				ElectricItem.manager.use(par1ItemStack, operationEnergyCost, par7EntityLiving);
			else {
				ElectricItem.manager.discharge(par1ItemStack, operationEnergyCost, getTier(par1ItemStack), true, false, false);
			}
		}
		return true;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b) {
		info.add(StatCollector.translateToLocal("ic2.item.tooltip.PowerTier") + " " + getTier(itemStack));
		info.add(StatCollector.translateToLocal("modulardrills.item.tooltip.OperationCost") + " " + (int) getOperationEnergyCost(itemStack));
		info.add(StatCollector.translateToLocal("modulardrills.item.tooltip.MaxCharge") + " " + (int) getMaxCharge(itemStack));
		info.add(StatCollector.translateToLocal("modulardrills.item.tooltip.DigSpeed") + " " + (int) getDigSpeed(itemStack));
		info.add(StatCollector.translateToLocal("modulardrills.item.tooltip.Radius") + " " + getDigRadius(itemStack));
		info.add("");
		info.add(StatCollector.translateToLocal("modulardrills.item.tooltip.TipTier") + " " + getNBTTipTier(itemStack));
		info.add(StatCollector.translateToLocal("modulardrills.item.tooltip.MotorTier") + " " + getNBTMotorTier(itemStack));
		info.add(StatCollector.translateToLocal("modulardrills.item.tooltip.BatteryTier") + " " + getNBTBatteryTier(itemStack));
	}

	@Override
	public List<String> getHudInfo(ItemStack itemStack) {
		List info = new LinkedList();
		info.add(ElectricItem.manager.getToolTip(itemStack));
		info.add(StatCollector.translateToLocal("ic2.item.tooltip.PowerTier") + " " + getTier(itemStack));
		return info;
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack) {
		return true;
	}

	@Override
	public boolean canProvideEnergy(ItemStack itemStack) {
		return true;
	}

	@Override
	public Item getChargedItem(ItemStack itemStack) {
		return this;
	}

	@Override
	public Item getEmptyItem(ItemStack itemStack) {
		return this;
	}

	@Override
	public double getMaxCharge(ItemStack itemStack) {
		return maxchargeByBattery[getNBTBatteryTier(itemStack)];
	}

	@Override
	public int getTier(ItemStack itemStack) {
		return tierByBattery[getNBTBatteryTier(itemStack)];
	}

	@Override
	public double getTransferLimit(ItemStack itemStack) {
		return transferLimitByBattery[getNBTBatteryTier(itemStack)];
	}

	public double getOperationEnergyCost(ItemStack itemStack) {
		return operationEnergyCost * operationEnergyCostMultiplierByMotor[getNBTMotorTier(itemStack)] / operationEnergyCostDividerByTip[getNBTTipTier(itemStack)];
	}

	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tabs, List itemList) {
		itemList.add(getItemStack((1.0D / 0.0D)));
		itemList.add(getItemStack(0.0D));
	}

	protected ItemStack getItemStack(double charge) {
		ItemStack ret = new ItemStack(this);
		ElectricItem.manager.charge(ret, charge, Integer.MAX_VALUE, true, false);
		return ret;
	}

	public static int getNBTTipTier(ItemStack stack) {
		NBTTagCompound tag = getNBTTag(stack);
		if (!tag.hasKey("tip")) {
			tag.setInteger("tip", 0);
		}
		return tag.getInteger("tip");
	}

	public static void setNBTTipTier(ItemStack stack, int tier) {
		NBTTagCompound tag = getNBTTag(stack);
		tag.setInteger("tip", tier);
	}

	public static int getNBTBatteryTier(ItemStack stack) {
		NBTTagCompound tag = getNBTTag(stack);
		if (!tag.hasKey("battery")) {
			tag.setInteger("battery", 0);
		}
		return tag.getInteger("battery");
	}

	public static void setNBTBatteryTier(ItemStack stack, int tier) {
		NBTTagCompound tag = getNBTTag(stack);
		tag.setInteger("battery", tier);
	}

	public static int getNBTMotorTier(ItemStack stack) {
		NBTTagCompound tag = getNBTTag(stack);
		if (!tag.hasKey("motor")) {
			tag.setInteger("motor", 0);
		}
		return tag.getInteger("motor");
	}

	public static void setNBTMotorTier(ItemStack stack, int tier) {
		NBTTagCompound tag = getNBTTag(stack);
		tag.setInteger("motor", tier);
	}

	public static NBTTagCompound getNBTTag(ItemStack stack) {
		if (stack.stackTagCompound == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		if (!stack.stackTagCompound.hasKey("modulardrill")) {
			stack.stackTagCompound.setTag("modulardrill", new NBTTagCompound());
		}
		return (NBTTagCompound) stack.stackTagCompound.getTag("modulardrill");
	}

	/*
	 * @Override public boolean canHarvestBlock(Block par1Block, ItemStack
	 * itemStack) { return par1Block.getHarvestLevel(0) <=
	 * getHarvestLevel(itemStack, par1Block.getHarvestTool(0)); }
	 */
	public boolean func_150897_b(Block p_150897_1_) {
		return p_150897_1_ == Blocks.obsidian ? this.toolMaterial.getHarvestLevel() == 3
				: (p_150897_1_ != Blocks.diamond_block && p_150897_1_ != Blocks.diamond_ore ? (p_150897_1_ != Blocks.emerald_ore && p_150897_1_ != Blocks.emerald_block ? (p_150897_1_ != Blocks.gold_block
						&& p_150897_1_ != Blocks.gold_ore ? (p_150897_1_ != Blocks.iron_block && p_150897_1_ != Blocks.iron_ore ? (p_150897_1_ != Blocks.lapis_block && p_150897_1_ != Blocks.lapis_ore ? (p_150897_1_ != Blocks.redstone_ore
						&& p_150897_1_ != Blocks.lit_redstone_ore ? (p_150897_1_.getMaterial() == Material.rock ? true : (p_150897_1_.getMaterial() == Material.iron ? true
						: p_150897_1_.getMaterial() == Material.anvil)) : this.toolMaterial.getHarvestLevel() >= 2) : this.toolMaterial.getHarvestLevel() >= 1)
						: this.toolMaterial.getHarvestLevel() >= 1)
						: this.toolMaterial.getHarvestLevel() >= 2)
						: this.toolMaterial.getHarvestLevel() >= 2)
						: this.toolMaterial.getHarvestLevel() >= 2);
	}
}
