package de.keridos.floodlights.core.network.message;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import de.keridos.floodlights.tileentity.TileEntityCarbonFloodlight;
import de.keridos.floodlights.tileentity.TileEntityFL;
import de.keridos.floodlights.tileentity.TileEntityMetaFloodlight;
import de.keridos.floodlights.tileentity.TileEntitySmallFloodlight;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;


/**
 * Created by Keridos on 05.10.14.
 * This Class is the Message that the electric floodlights TileEntity uses.
 */
public class MessageTileEntityFL implements IMessage, IMessageHandler<MessageTileEntityFL, IMessage> {
    public int x, y, z, timeRemaining, color;
    public byte orientation, state;
    public boolean rotationState, wasActive;
    public String customName, owner;

    public MessageTileEntityFL() {
    }

    public MessageTileEntityFL(TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityFL) {
            TileEntityFL tileEntityFL = (TileEntityFL) tileEntity;
            this.x = tileEntityFL.xCoord;
            this.y = tileEntityFL.yCoord;
            this.z = tileEntityFL.zCoord;
            this.orientation = (byte) tileEntityFL.getOrientation().ordinal();
            this.state = (byte) tileEntityFL.getState();
            this.customName = tileEntityFL.getCustomName();
            this.owner = tileEntityFL.getOwner();
            this.color = tileEntityFL.getColor();
            if (tileEntity instanceof TileEntityCarbonFloodlight) {
                this.timeRemaining = ((TileEntityCarbonFloodlight) tileEntity).timeRemaining;
            } else {
                this.timeRemaining = 0;
            }
            if (tileEntity instanceof TileEntitySmallFloodlight) {
                this.rotationState = ((TileEntitySmallFloodlight) tileEntity).getRotationState();
            } else {
                this.rotationState = false;
            }
            if (tileEntity instanceof TileEntityMetaFloodlight) {
                this.wasActive = ((TileEntityMetaFloodlight) tileEntity).getWasActive();
            } else {
                this.wasActive = false;
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.timeRemaining = buf.readInt();
        this.orientation = buf.readByte();
        this.state = buf.readByte();
        int customNameLength = buf.readInt();
        this.customName = new String(buf.readBytes(customNameLength).array());
        int ownerLength = buf.readInt();
        this.owner = new String(buf.readBytes(ownerLength).array());
        this.rotationState = buf.readBoolean();
        this.color = buf.readInt();
        this.wasActive = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(timeRemaining);
        buf.writeByte(orientation);
        buf.writeByte(state);
        buf.writeInt(customName.length());
        buf.writeBytes(customName.getBytes());
        buf.writeInt(owner.length());
        buf.writeBytes(owner.getBytes());
        buf.writeBoolean(rotationState);
        buf.writeInt(color);
        buf.writeBoolean(wasActive);
    }

    @Override
    public IMessage onMessage(MessageTileEntityFL message, MessageContext ctx) {
        TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld.getTileEntity(message.x, message.y, message.z);
        if (tileEntity instanceof TileEntityFL) {
            ((TileEntityFL) tileEntity).setOrientation(message.orientation);
            ((TileEntityFL) tileEntity).setState(message.state);
            ((TileEntityFL) tileEntity).setCustomName(message.customName);
            ((TileEntityFL) tileEntity).setOwner(message.owner);
            ((TileEntityFL) tileEntity).setColor(message.color);
        }
        if (tileEntity instanceof TileEntityCarbonFloodlight) {
            ((TileEntityCarbonFloodlight) tileEntity).timeRemaining = message.timeRemaining;
        }
        if (tileEntity instanceof TileEntitySmallFloodlight) {
            ((TileEntitySmallFloodlight) tileEntity).setRotationState(message.rotationState);
        }
        if (tileEntity instanceof TileEntityMetaFloodlight) {
            ((TileEntityMetaFloodlight) tileEntity).setWasActive(message.wasActive);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("MessageTileEntityFL - x:%s, y:%s, z:%s, timeRemaining:%s, orientation:%s, state:%s, customName:%s, owner:%s", x, y, z, timeRemaining, orientation, state, customName, owner);
    }
}
