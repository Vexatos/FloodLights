package de.keridos.floodlights.tileentity;

import cpw.mods.fml.common.Optional;
import de.keridos.floodlights.compatability.ModCompatibility;
import de.keridos.floodlights.core.EventListener;
import de.keridos.floodlights.handler.ConfigHandler;
import de.keridos.floodlights.reference.Names;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by Keridos on 04.05.2015.
 * This Class is the tile entity for the small floodlight.
 */

@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2")
public class TileEntitySmallFloodlight extends TileEntityFLElectric {
    private boolean rotationState = false;

    public TileEntitySmallFloodlight() {
        super();
        this.mode = 3;
        this.rotationState = false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        if (nbtTagCompound.hasKey(Names.NBT.ROTATION_STATE)) {
            this.rotationState = nbtTagCompound.getBoolean(Names.NBT.ROTATION_STATE);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setBoolean(Names.NBT.ROTATION_STATE, rotationState);
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return (from.getOpposite().ordinal() == orientation.ordinal());
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    public void toggleRotationState() {
        rotationState = !rotationState;
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public boolean getRotationState() {
        return rotationState;
    }

    public void setRotationState(boolean rotationState) {
        this.rotationState = rotationState;
    }

    public void updateEntity() {
        World world = this.getWorldObj();
        if (ModCompatibility.IC2Loaded && !wasAddedToEnergyNet && !world.isRemote) {
            addToIc2EnergyNetwork();
            wasAddedToEnergyNet = true;
        }
        if (!world.isRemote) {
            ForgeDirection direction = this.getOrientation();
            int realEnergyUsage = ConfigHandler.energyUsageSmallFloodlight;
            if (active && (storage.getEnergyStored() >= realEnergyUsage || storageEU >= (double) realEnergyUsage / 8.0D)) {
                if (!wasActive || world.getTotalWorldTime() % timeout == 0) {
                    if (world.getTotalWorldTime() % timeout == 0) {
                        EventListener.lightHandler.removeSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
                        EventListener.lightHandler.addSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
                        world.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                    } else {
                        EventListener.lightHandler.addSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
                        world.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                    }
                }
                if (storageEU >= (double) realEnergyUsage / 8.0D) {
                    storageEU -= (double) realEnergyUsage / 8.0D;
                } else {
                    storage.modifyEnergyStored(-realEnergyUsage);
                }
                wasActive = true;
            } else {
                if (wasActive) {
                    EventListener.lightHandler.removeSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
                    world.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                }
                wasActive = false;
            }
        }
    }
}