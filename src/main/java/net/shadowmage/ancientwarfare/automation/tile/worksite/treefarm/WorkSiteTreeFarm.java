package net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.shadowmage.ancientwarfare.automation.registry.TreeFarmRegistry;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteFarm;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class WorkSiteTreeFarm extends TileWorksiteFarm {
	private boolean hasShears;
	private final Set<BlockPos> blocksToShear = new LinkedHashSet<>();
	private final Set<BlockPos> blocksToChop = new LinkedHashSet<>();
	private final Set<BlockPos> blocksToPlant = new HashSet<>();
	private final Set<BlockPos> blocksToFertilize = new HashSet<>();

	private final IItemHandler inventoryForDrops;

	public WorkSiteTreeFarm() {
		super();
		inventoryForDrops = new CombinedInvWrapper(plantableInventory, mainInventory);
	}

	@Override
	protected boolean isPlantable(ItemStack stack) {
		return TreeFarmRegistry.isPlantable(stack);
	}

	@Override
	protected boolean isMiscItem(ItemStack stack) {
		return stack.getItem() == Items.SHEARS || super.isMiscItem(stack);
	}

	@Override
	public void onBoundsAdjusted() {
		validateCollection(blocksToFertilize);
		validateCollection(blocksToChop);
		validateCollection(blocksToPlant);
		if (!hasShears) {
			blocksToShear.clear();
		}
		markDirty();
	}

	@Override
	protected void countResources() {
		super.countResources();
		hasShears = InventoryTools.getCountOf(miscInventory, s -> s.getItem() == Items.SHEARS) > 0;
	}

	@Override
	protected boolean processWork() {
		return shearBlock() || chopBlock() || plant() || bonemealBlock();
	}

	private boolean bonemealBlock() {
		if (bonemealCount <= 0 || blocksToFertilize.isEmpty()) {
			return false;
		}

		Iterator<BlockPos> it = blocksToFertilize.iterator();
		BlockPos position = it.next();
		it.remove();

		IBlockState state = world.getBlockState(position);

		return canFertilize(world, position, state) && fertilize(position);

	}

	private boolean plant() {
		if (plantableCount <= 0 || blocksToPlant.isEmpty()) {
			return false;
		}

		//noinspection ConstantConditions
		Optional<Tuple<ItemStack, ISapling>> plantable = InventoryTools.stream(plantableInventory).map(p -> new Tuple<>(p, TreeFarmRegistry.getSapling(p)))
				.filter(t -> t.getSecond().isPresent()).map(t -> new Tuple<>(t.getFirst(), t.getSecond().get())).findFirst();
		if (plantable.isPresent()) {
			Iterator<BlockPos> it = blocksToPlant.iterator();
			BlockPos position = it.next();
			it.remove();

			ItemStack stack = plantable.get().getFirst();
			ISapling sapling = plantable.get().getSecond();
			if (canReplace(position) && tryPlantingSapling(position, stack, sapling)) {
				InventoryTools.removeItems(plantableInventory, stack, 1);
				return true;
			}
		}

		return false;
	}

	private boolean tryPlantingSapling(BlockPos position, ItemStack stack, ISapling sapling) {
		return sapling.isRightClick() ? BlockTools.placeItemBlockRightClick(stack.copy(), world, position) :
				tryPlace(stack.copy(), position, EnumFacing.UP) || tryPlace(stack.copy(), position, EnumFacing.DOWN);
	}

	private boolean chopBlock() {
		if (blocksToChop.isEmpty()) {
			return false;
		}

		Iterator<BlockPos> it = blocksToChop.iterator();
		BlockPos position = it.next();
		it.remove();
		IBlockState state = world.getBlockState(position);
		IBlockExtraDrop extraDrop = TreeFarmRegistry.getBlockExtraDrop(state);
		NonNullList<ItemStack> extraDrops = extraDrop.getDrops(world, position, state, getFortune());
		if (!harvestBlock(position)) {
			return false;
		}
		InventoryTools.insertOrDropItems(inventoryForDrops, extraDrops, world, position);
		return true;
	}

	private boolean shearBlock() {
		if (!hasShears || blocksToShear.isEmpty()) {
			return false;
		}

		Iterator<BlockPos> it = blocksToShear.iterator();
		BlockPos position = it.next();
		it.remove();
		Block block = world.getBlockState(position).getBlock();
		if (block instanceof IShearable) {
			Optional<ItemStack> shears = InventoryTools.stream(miscInventory).filter(s -> s.getItem() instanceof ItemShears).findFirst();

			if (shears.isPresent() && shear(position, (IShearable) block, shears.get())) {
				return true;
			}
		}

		return false;
	}

	private boolean shear(BlockPos position, IShearable block, ItemStack shears) {
		if (block.isShearable(shears, world, position)) {
			NonNullList<ItemStack> drops = InventoryTools.toNonNullList(block.onSheared(shears, world, position, getFortune()));
			drops = InventoryTools.insertItems(plantableInventory, drops, false);
			InventoryTools.insertOrDropItems(mainInventory, drops, world, pos);
			world.setBlockToAir(position);
			return true;
		}
		return false;
	}

	private void addTreeBlocks(IBlockState state, BlockPos basePos) {
		world.profiler.startSection("TreeFinder");

		ITree tree = TreeFarmRegistry.getTreeScanner(state).scanTree(world, basePos);
		List<BlockPos> leafBlocks = tree.getLeafPositions();
		if (hasShears) {
			blocksToShear.addAll(leafBlocks);
		} else {
			blocksToChop.addAll(leafBlocks);
		}
		List<BlockPos> trunkBlocks = tree.getTrunkPositions();
		blocksToChop.addAll(trunkBlocks);

		if (!leafBlocks.isEmpty() || !trunkBlocks.isEmpty()) {
			markDirty();
		}
		world.profiler.endSection();
	}

	@Override
	public WorkType getWorkType() {
		return WorkType.FORESTRY;
	}

	@Override
	public boolean onBlockClicked(EntityPlayer player, @Nullable EnumHand hand) {
		if (!player.world.isRemote) {
			NetworkHandler.INSTANCE.openGui(player, NetworkHandler.GUI_WORKSITE_TREE_FARM, pos);
		}
		return true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if (!blocksToChop.isEmpty()) {
			NBTTagList chopList = new NBTTagList();
			for (BlockPos position : blocksToChop) {
				chopList.appendTag(new NBTTagLong(position.toLong()));
			}
			tag.setTag("targetList", chopList);
		}
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		blocksToChop.clear();
		if (tag.hasKey("targetList")) {
			NBTTagList chopList = tag.getTagList("targetList", Constants.NBT.TAG_LONG);
			for (int i = 0; i < chopList.tagCount(); i++) {
				blocksToChop.add(BlockPos.fromLong(((NBTTagLong) chopList.get(i)).getLong()));
			}
		}
	}

	@Override
	protected void scanBlockPosition(BlockPos scanPos) {
		if (canReplace(scanPos)) {
			IBlockState state = world.getBlockState(scanPos.down());
			if (TreeFarmRegistry.isSoil(state) || (state.getBlock().isReplaceable(world, scanPos.down()) && TreeFarmRegistry
					.isSoil(world.getBlockState(scanPos.up())))) {
				blocksToPlant.add(scanPos);
			}
		} else {
			IBlockState state = world.getBlockState(scanPos);
			if (canFertilize(world, scanPos, state)) {
				blocksToFertilize.add(scanPos);
			} else if (state.getMaterial() != Material.AIR && blocksToChop.isEmpty()) {
				addTreeBlocks(state, scanPos);
			}
		}
	}

	private boolean canFertilize(World world, BlockPos pos, IBlockState state) {
		return state.getBlock() instanceof IGrowable && ((IGrowable) state.getBlock()).canGrow(world, pos, state, world.isRemote);
	}

	@Override
	protected boolean hasWorksiteWork() {
		return (hasShears && !blocksToShear.isEmpty()) || !blocksToChop.isEmpty() || (bonemealCount > 0 && !blocksToFertilize
				.isEmpty()) || (plantableCount > 0 && !blocksToPlant.isEmpty());
	}
}
