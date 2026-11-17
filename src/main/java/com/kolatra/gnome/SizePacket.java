package com.kolatra.gnome;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class SizePacket implements IMessage {

    private String playerName;
    private float scale;

    public SizePacket() {}

    public SizePacket(String playerName, float scale) {
        this.playerName = playerName;
        this.scale = scale;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        playerName = ByteBufUtils.readUTF8String(buffer);
        scale = buffer.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, playerName);
        buffer.writeFloat(scale);
    }

    public static class Handler implements IMessageHandler<SizePacket, IMessage> {

        @Override
        public IMessage onMessage(SizePacket message, MessageContext context) {
            if (context.side.isClient()) {
                // player/world updated on the next client tick
                SizeManager.setClientScale(message.playerName, message.scale);
            }
            return null;
        }
    }
}
