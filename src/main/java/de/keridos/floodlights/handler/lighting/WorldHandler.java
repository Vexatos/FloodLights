package de.keridos.floodlights.handler.lighting;

import de.keridos.floodlights.handler.ConfigHandler;
import de.keridos.floodlights.init.ModBlocks;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;

/**
 * Created by Keridos on 03.10.14.
 * This Class stores every lighting block in its designated world and manages them.
 */
public class WorldHandler {
    private ConfigHandler configHandler = ConfigHandler.getInstance();
    private ArrayList<LightBlockHandle> lightBlocks = new ArrayList<LightBlockHandle>();
    private World world;
    private int lastPositionInList = 0;

    public WorldHandler(World worldinput) {
        this.world = worldinput;
        this.lastPositionInList = 0;
    }

    public int getDimensionID() {
        return this.world.provider.dimensionId;
    }

    public LightBlockHandle getFloodlightHandler(int x, int y, int z) {
        boolean added = false;
        if (lightBlocks.size() == 0) {
            added = true;
            lightBlocks.add(new LightBlockHandle(x, y, z));
        }
        for (LightBlockHandle f : lightBlocks) {
            int[] coords = {x, y, z};
            if (f.getCoords()[0] == coords[0] && f.getCoords()[1] == coords[1] && f.getCoords()[2] == coords[2]) {
                return f;
            }
        }
        if (!added) {
            lightBlocks.add(new LightBlockHandle(x, y, z));
        }
        return getFloodlightHandler(x, y, z);
    }

    public void addSource(int sourceX, int sourceY, int sourceZ, ForgeDirection direction, int sourcetype) {
        if (sourcetype == 0) {
            for (int i = 1; i <= configHandler.rangeStraightFloodlight; i++) {
                int x = sourceX + direction.offsetX * i;
                int y = sourceY + direction.offsetY * i;
                int z = sourceZ + direction.offsetZ * i;
                LightBlockHandle handler = getFloodlightHandler(x, y, z);
                if (world.getBlock(x, y, z).isAir(world, x, y, z)) {
                    lightBlocks.get(lightBlocks.indexOf(handler)).addSource(sourceX, sourceY, sourceZ);
                } else if (world.getBlock(x, y, z).isOpaqueCube()) {
                    break;
                }
            }
        }
    }

    public void removeSource(int sourceX, int sourceY, int sourceZ, ForgeDirection direction, int sourcetype) {
        if (sourcetype == 0) {
            for (int i = 1; i <= 64; i++) {
                int x = sourceX + direction.offsetX * i;
                int y = sourceY + direction.offsetY * i;
                int z = sourceZ + direction.offsetZ * i;
                getFloodlightHandler(x, y, z).removeSource(sourceX, sourceY, sourceZ);
            }
        }
    }

    public void updateRun() {
        World activeworld = DimensionManager.getWorld(world.provider.dimensionId);
        int j = lastPositionInList;
        for (int i = lastPositionInList; i < j + configHandler.refreshRate; i++) {
            if (i >= lightBlocks.size()) {
                lastPositionInList = 0;
                break;
            }
            lastPositionInList = i;
            LightBlockHandle f = (lightBlocks.get(i));
            int x = f.getCoords()[0];
            int y = f.getCoords()[1];
            int z = f.getCoords()[2];
            if (activeworld.getBlock(x, y, z).getUnlocalizedName().contains("blockLight") && f.sourceNumber() == 0) {
                activeworld.setBlockToAir(x, y, z);
                lightBlocks.remove(i);
                i--;
                j--;
            }
            if (f.sourceNumber() > 0 && activeworld.getBlock(x, y, z).isAir(activeworld, x, y, z)) {
                activeworld.setBlock(x, y, z, ModBlocks.blockFLLight);
            }
        }
    }

}