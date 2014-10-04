package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.automation.item.AWAutomationItemLoader;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueWindmillController;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RotationType;

public class BlockWindmillControll extends BlockTorqueBase
{

public BlockWindmillControll(String regName)
  {
  super(Material.rock);
  this.setBlockName(regName);
  this.setCreativeTab(AWAutomationItemLoader.automationTab);
  }

@Override
public RotationType getRotationType()
  {
  return RotationType.FOUR_WAY;
  }

@Override
public boolean invertFacing()
  {
  return true;
  }

@Override
public TileEntity createTileEntity(World world, int metadata)
  {
  return new TileTorqueWindmillController();
  }

@Override
public boolean shouldSideBeRendered(net.minecraft.world.IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_) {return false;}

@Override
public boolean isOpaqueCube() {return false;}

@Override
public boolean isNormalCube() {return false;}

@Override
public void registerBlockIcons(IIconRegister register){}

@Override
public IIcon getIcon(int side, int meta){return Blocks.planks.getIcon(side, 0);}

}
