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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeSet;

public class AnyVersion extends PluginBase implements Listener{
    private int serverProtocolVersion;
    private boolean showMessage;
    private boolean useClientFilter;
    private Set<Integer> clientFilter;

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOW)
    public void onLoginPacket(DataPacketReceiveEvent event){
        DataPacket packet = event.getPacket();
        if (packet.pid() != ProtocolInfo.LOGIN_PACKET) return;
        LoginPacket loginPacket = (LoginPacket) packet;

        if (loginPacket.getProtocol() == this.serverProtocolVersion) {
            log(loginPacket.username,"is using correct protocol version");
            return;
        }

        if (!checkClientFilter (loginPacket.getProtocol())) {
            log (loginPacket.username,"is using incorrect protocol version ("+loginPacket.getProtocol()+")");
            return;
        }

        log(TextFormat.YELLOW + loginPacket.username + " is using protocol version " + loginPacket.getProtocol() + " (server version: " + this.serverProtocolVersion + ")");
        log(TextFormat.RED + "Warning! " + TextFormat.YELLOW + "Using outdated/outrunning client could damage your server.");
        log(TextFormat.YELLOW + "Use it on your own risk");
        loginPacket.protocol = this.serverProtocolVersion;

    }

    private void log (String... msg){
        if (showMessage) this.getLogger().info(join(msg));
    }

    @Override
    public void onEnable(){
        this.serverProtocolVersion = getVersion();
        this.getLogger().info("Server protocol version: "+this.serverProtocolVersion);
        loadCfg();
        saveCfg();
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

    private boolean checkClientFilter(int clientProtocol){
        if (!useClientFilter) return true;
        return clientFilter.contains(clientProtocol);
    }

    private void loadCfg(){
        this.getDataFolder().mkdirs();
        File file = new File(this.getDataFolder()+File.separator+"config.yml");
        if (!file.exists()) try {
            file.createNewFile();
        } catch (IOException e) {
        }
        this.reloadConfig();
        this.showMessage = this.getConfig().getBoolean("write-messages-in-log", true);
        this.useClientFilter = this.getConfig().getBoolean("client-filter.enable", false);
        String[] ln = this.getConfig().getString("client-filter.allowed-protocols", (serverProtocolVersion-1)+", "+serverProtocolVersion+", "+(serverProtocolVersion+1)).split(",\\s+");
        this.clientFilter = new TreeSet<Integer>();
        for (String n : ln) if (n.matches("\\d+")) clientFilter.add(Integer.parseInt(n));
    }

    private void saveCfg(){
        this.getConfig().set ("write-messages-in-log", showMessage);
        this.getConfig().set ("client-filter.enable", useClientFilter);
        StringBuilder sb  = new StringBuilder();
        clientFilter.forEach(i -> {
            if (sb.length()>0) sb.append(", ");
            sb.append(i);
        });
        this.getConfig().set ("client-filter.allowed-protocols", sb.toString());
        this.saveConfig();
    }

    private String join (String... obj){
        StringBuilder sb = new StringBuilder();
        for (Object o: obj){
            if (sb.length()>0) sb.append(" ");
            sb.append(o.toString());
        }
        return sb.toString();
    }

}
