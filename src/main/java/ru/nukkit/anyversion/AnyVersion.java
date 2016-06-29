package ru.nukkit.anyversion;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LoginPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

import java.lang.reflect.Field;

public class AnyVersion extends PluginBase implements Listener{
    private int serverProtocolVersion;

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOW)
    public void onLoginPacket(DataPacketReceiveEvent event){
        DataPacket packet = event.getPacket();
        if (packet.pid() != ProtocolInfo.LOGIN_PACKET) return;
        LoginPacket loginPacket = (LoginPacket) packet;
        if (loginPacket.getProtocol() == this.serverProtocolVersion){
            this.getLogger().info(loginPacket.username+ " is using correct protocol version");
        } else {
            this.getLogger().info(TextFormat.YELLOW+loginPacket.username+ " is using protocol version "+loginPacket.getProtocol()+" (server version: "+this.serverProtocolVersion+")");
            this.getLogger().info(TextFormat.RED+"Warning! "+TextFormat.YELLOW+"Using outdated/outrunning client could damage your server.");
            this.getLogger().info(TextFormat.YELLOW+"Use it on your own risk");
            loginPacket.protocol = this.serverProtocolVersion;
        }
    }

    @Override
    public void onEnable(){
        this.serverProtocolVersion =getVersion();
        this.getLogger().info("Server protocol version: "+this.serverProtocolVersion);
        this.getServer().getPluginManager().registerEvents(this,this);
    }


    private int getVersion(){
        try {
            Field f = ProtocolInfo.class.getDeclaredField("CURRENT_PROTOCOL");
            return f.getInt(null);
        } catch (Exception e) {
            this.getLogger().info("Failed to detect actual value of server protocol version. Will use precompiled value.");
            return ProtocolInfo.CURRENT_PROTOCOL;
        }
    }
}
