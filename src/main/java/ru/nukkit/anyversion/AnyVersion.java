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

public class AnyVersion extends PluginBase implements Listener{

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOW)
    public void onLoginPacket(DataPacketReceiveEvent event){
        DataPacket packet = event.getPacket();
        if (packet.pid() != ProtocolInfo.LOGIN_PACKET) return;
        LoginPacket loginPacket = (LoginPacket) packet;
        if (loginPacket.protocol1 == ProtocolInfo.CURRENT_PROTOCOL){
            this.getLogger().info(loginPacket.username+ " is using correct protocol version");
        } else {
            this.getLogger().info(TextFormat.YELLOW+loginPacket.username+ " is using protocol version "+loginPacket.protocol1+" (server version: "+ProtocolInfo.CURRENT_PROTOCOL+")");
            this.getLogger().info(TextFormat.RED+"Warning! "+TextFormat.YELLOW+"Using outdated/outrunning client could damage your server.");
            this.getLogger().info(TextFormat.YELLOW+"Use it on your own risk");
            loginPacket.protocol1 = ProtocolInfo.CURRENT_PROTOCOL;
        }
    }

    @Override
    public void onEnable(){
        this.getServer().getPluginManager().registerEvents(this,this);
    }

}
