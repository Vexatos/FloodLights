package de.keridos.floodlights.tileentity;

import de.keridos.floodlights.compatability.ModCompatibility;
import de.keridos.floodlights.core.EventListener;
import de.keridos.floodlights.handler.ConfigHandler;
import de.keridos.floodlights.reference.Names;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import static de.keridos.floodlights.util.GeneralUtil.safeLocalize;

/**
 * Created by Keridos on 01.10.14.
 * This Class is the electric floodlight TileEntity.
 */

public class TileEntityElectricFloodlight extends TileEntityFLElectric {

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        World world = this.getWorldObj();
        if (ModCompatibility.IC2Loaded && !wasAddedToEnergyNet && !world.isRemote) {
            addToIc2EnergyNetwork();
            wasAddedToEnergyNet = true;
        }
        if (!world.isRemote) {
            ForgeDirection direction = this.getOrientation();
            int realEnergyUsage = ConfigHandler.energyUsage / (mode == 0 ? 1 : 2);
            if (active && (storage.getEnergyStored() >= realEnergyUsage || storageEU >= (double) realEnergyUsage / 8.0D)) {
                if (!wasActive || world.getTotalWorldTime() % timeout == 0) {
                    if (world.getTotalWorldTime() % timeout == 0) {
                        EventListener.lightHandler.removeSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
                        EventListener.lightHandler.addSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
                        world.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, this.getOrientation().ordinal() + 6, 2);
                    } else {
                        EventListener.lightHandler.addSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
                        world.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, world.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) + 6, 2);
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
                    world.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, world.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) - 6, 2);
                }
                wasActive = false;
            }
        }
    }

    public void changeMode(EntityPlayer player) {
        World world = this.getWorldObj();
        if (!world.isRemote) {
            ForgeDirection direction = this.getOrientation();
            int realEnergyUsage = ConfigHandler.energyUsage / (mode == 0 ? 1 : 4);
            EventListener.lightHandler.removeSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
            mode = (mode == 2 ? 0 : mode + 1);
            if (active && (storage.getEnergyStored() >= realEnergyUsage || storageEU >= realEnergyUsage / 8)) {
                EventListener.lightHandler.addSource(world, this.xCoord, this.yCoord, this.zCoord, direction, this.mode);
            }
            String modeString = (mode == 0 ? Names.Localizations.STRAIGHT : mode == 1 ? Names.Localizations.NARROW_CONE : Names.Localizations.WIDE_CONE);
            player.addChatMessage(new ChatComponentText(safeLocalize(Names.Localizations.MODE) + ": " + safeLocalize(modeString)));
        }
    }
}
