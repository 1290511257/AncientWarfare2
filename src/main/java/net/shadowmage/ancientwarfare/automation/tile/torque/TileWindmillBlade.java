package net.shadowmage.ancientwarfare.automation.tile.torque;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.shadowmage.ancientwarfare.core.config.AWLog;
import net.shadowmage.ancientwarfare.core.util.BlockFinder;
import net.shadowmage.ancientwarfare.core.util.BlockPosition;

public class TileWindmillBlade extends TileEntity
{

public BlockPosition controlPos;
public boolean isControl = false;//set to true if this is the control block for a setup
public double rotation, prevRotation;//used in rendering
public double maxRpm = 100;

/**
 * the raw size of the windmill in blocks tall
 */
public int windmillSize = 0;

/**
 * 0/1 == INVALID
 * 2/3 == north/south face (expands on x-axis)
 * 4/5 == east/west face (expands on z-axis)
 */
public int windmillDirection = 2;// z-facing, expands on the x-axis, alternatively, can be 

public TileWindmillBlade()
  {
  
  }

protected void updateRotation()
  {
  double maxRpm = this.maxRpm;
  double rpm = maxRpm;//TODO vary this by 'wind factor'....uhh..yah...
  prevRotation=rotation;
  rotation += rpm * 360.d / 20.d / 60.d;
  }

public void blockPlaced()
  {
  validateSetup();
  }

public void blockBroken()
  {
  informNeighborsToValidate();
  }

public TileWindmillBlade getMaster()
  {
  if(controlPos!=null)
    {
    TileEntity te = worldObj.getTileEntity(controlPos.x, controlPos.y, controlPos.z);
    return te instanceof TileWindmillBlade ? (TileWindmillBlade)te : null;
    }
  return null;
  }

protected boolean validateSetup()
  {
  Set<BlockPosition> connectedPosSet = new HashSet<BlockPosition>();  
  BlockFinder.findConnectedSixWay(worldObj, xCoord, yCoord, zCoord, getBlockType(), getBlockMetadata(), connectedPosSet);
  int minX=Integer.MAX_VALUE, minY=Integer.MAX_VALUE, minZ=Integer.MAX_VALUE, maxX=Integer.MIN_VALUE, maxY=Integer.MIN_VALUE, maxZ=Integer.MIN_VALUE;
  for(BlockPosition pos : connectedPosSet)
    {
    if(pos.x<minX){minX=pos.x;}
    if(pos.x>maxX){maxX=pos.x;}
    if(pos.y<minY){minY=pos.y;}
    if(pos.y>maxY){maxY=pos.y;}
    if(pos.z<minZ){minZ=pos.z;}
    if(pos.z>maxZ){maxZ=pos.z;}
    }
  
  int min = 5;
  int max = 15;
  int w = maxX-minX + 1;
  int l = maxZ-minZ + 1;
  int h = maxY-minY + 1;
  int cube = w*l*h;
  
  /**
   * if y size >= min
   * and y size <= max
   * and either x or z size == 1 (one needs to be a single block thick)
   * and either x or z == y size (the other needs to be the same size as height)
   * and h%2==1 (is an odd size, 5, 7, 9, etc)
   * and is full cube (all block spots filled) (will need to modify this check for those sizes with missing corner blocks, create bit mask 2d array to test for proper setup)
   */
  boolean valid = h>=min && h<=max && (l==1 || w==1) && (l==h || w==h) && h%2==1 && cube==connectedPosSet.size();  
  if(valid)    
    {
    /**
     * calculate the control block coordinates from the min coordinate and sizes
     */
    int controlX = minX, controlY=minY, controlZ=minZ, face=2;
    
    int halfSize = (h-1)/2;

    controlY = minY + halfSize;//should be the center
    if(w>1)//widest on X axis
      {
      face = 2;//faces north/south
      controlX = minX + halfSize;//should be the center
      controlZ = minZ;//only 1 z-coordinate
      }
    else//widest on Z axis
      {
      face = 4;//faces east/west
      controlX = minX;//only 1 x-coordinatehould be the center
      controlZ = minZ + halfSize;//should be the center
      }
    setValidSetup(connectedPosSet, controlX, controlY, controlZ, w, h, l, face);
    }
  else
    {
    setInvalidSetup(connectedPosSet);   
    }
  return valid;
  }

private void informNeighborsToValidate()
  {
  TileEntity te;
  ForgeDirection d;
  int x, y, z;
  for(int i = 0; i < 6; i++)
    {
    d = ForgeDirection.getOrientation(i);
    x = xCoord+d.offsetX;
    y = yCoord+d.offsetY;
    z = zCoord+d.offsetZ;
    te = worldObj.getTileEntity(x, y, z);
    if(te instanceof TileWindmillBlade)
      {
      ((TileWindmillBlade) te).validateSetup();
      }    
    }
  }

private void setController(BlockPosition pos)
  {
  this.controlPos = pos==null ? null : pos.copy();  
  if(!worldObj.isRemote)
    {
    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);    
    }
  }

private void setInvalidSetup(Set<BlockPosition> set)
  {
  TileEntity te;
  isControl=false;
  setController(null);
  for(BlockPosition pos : set)
    {
    te = worldObj.getTileEntity(pos.x, pos.y, pos.z);
    if(te instanceof TileWindmillBlade)
      {
      ((TileWindmillBlade) te).isControl = false;
      ((TileWindmillBlade) te).setController(null);
      }
    } 
  }

private void setValidSetup(Set<BlockPosition> set, int cx, int cy, int cz, int xs, int ys, int zs, int face)
  {
  TileEntity te; 
  BlockPosition cp = new BlockPosition(cx, cy, cz);
  controlPos = cp;
  for(BlockPosition pos : set)
    {
    te = worldObj.getTileEntity(pos.x, pos.y, pos.z);
    if(te instanceof TileFlywheelStorage)
      {
      ((TileWindmillBlade) te).setController(cp);
      }
    ((TileWindmillBlade)te).isControl = (pos.x==cx && pos.y==cy && pos.z==cz);
    }
  setTileAsController(cp.x, cp.y, cp.z, xs, ys, zs, face);
  }

private void setTileAsController(int x, int y, int z, int xsize, int ysize, int zsize, int face)
  {
  TileEntity te = worldObj.getTileEntity(x, y, z);
  if(te instanceof TileWindmillBlade)
    {
    ((TileWindmillBlade) te).setAsController(xsize, ysize, zsize, face);
    }
  }

private void setAsController(int xSize, int ySize, int zSize, int face)
  {
  windmillDirection = xSize==1 ? 4 : zSize==1? 2 : 0;
  windmillSize = ySize;
  this.isControl = true;
  this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
  }

@Override
public Packet getDescriptionPacket()
  {
  NBTTagCompound tag = new NBTTagCompound();
  tag.setBoolean("isControl", isControl); 
  if(controlPos!=null)
    {
    tag.setTag("controllerPos", controlPos.writeToNBT(new NBTTagCompound()));
    }
  if(isControl)
    {
    tag.setInteger("size", windmillSize);
    tag.setInteger("direction", windmillDirection);
    }
  return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
  }

@Override
public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
  {
  super.onDataPacket(net, pkt);
  NBTTagCompound tag =pkt.func_148857_g();
  controlPos = tag.hasKey("controllerPos") ? new BlockPosition(tag.getCompoundTag("controllerPos")) : null;
  isControl = tag.getBoolean("isControl"); 
  if(isControl)
    {
    windmillSize = tag.getInteger("size");
    windmillDirection = tag.getInteger("direction");
    }
  }

@Override
public void readFromNBT(NBTTagCompound tag)
  {
  super.readFromNBT(tag);
  controlPos = tag.hasKey("controllerPos") ? new BlockPosition(tag.getCompoundTag("controllerPos")) : null;
  isControl = tag.getBoolean("isControl");
  }

@Override
public void writeToNBT(NBTTagCompound tag)
  {
  super.writeToNBT(tag);
  tag.setBoolean("isControl", isControl);
  if(controlPos!=null)
    {
    tag.setTag("controllerPos", controlPos.writeToNBT(new NBTTagCompound()));
    }
  }

}
