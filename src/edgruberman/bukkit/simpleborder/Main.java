package edgruberman.bukkit.simpleborder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.bukkit.util.config.ConfigurationNode;

import edgruberman.bukkit.simpleborder.MessageManager;
import edgruberman.bukkit.simpleborder.MessageManager.MessageLevel;

public class Main extends org.bukkit.plugin.java.JavaPlugin {

    public static MessageManager messageManager = null;
    
    private static final String DEFAULT_LOG_LEVEL       = "INFO";
    private static final String DEFAULT_SEND_LEVEL      = "INFO";
    private static final String DEFAULT_BROADCAST_LEVEL = "INFO";
    
    private String message = "";
    private Map<String, Border> borders = Collections.synchronizedMap(new HashMap<String, Border>());
	
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Configuration.load(this);
        Main.messageManager.setLogLevel(MessageLevel.parse(      this.getConfiguration().getString("logLevel",       Main.DEFAULT_LOG_LEVEL)));
        Main.messageManager.setSendLevel(MessageLevel.parse(     this.getConfiguration().getString("sendLevel",      Main.DEFAULT_SEND_LEVEL)));
        Main.messageManager.setBroadcastLevel(MessageLevel.parse(this.getConfiguration().getString("broadcastLevel", Main.DEFAULT_BROADCAST_LEVEL)));
        
        this.message = this.getConfiguration().getString("message");
        this.loadBorders();
        
        // Only register events to listen for if a border is defined.
        if (this.getConfiguration().getNodes("borders").size() > 0) { this.registerEvents(); }
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    private void registerEvents() {
        PlayerListener playerListener = new PlayerListener(this);
        
        org.bukkit.plugin.PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE    , playerListener,  Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.PLAYER_JOIN    , playerListener,  Event.Priority.Normal, this);
    }
    
    private void loadBorders() {
        this.borders.clear();
        
        for (Map.Entry<String, ConfigurationNode> borderEntry : this.getConfiguration().getNodes("borders").entrySet()) {
            if (borderEntry.getValue().getInt("distance", 0) == 0) continue;
            Border border = new Border(
                  this.getServer().getWorld(borderEntry.getKey())
                , borderEntry.getValue().getDouble("origin.X", 0)
                , borderEntry.getValue().getDouble("origin.Y", 0)
                , borderEntry.getValue().getDouble("origin.Z", 0)
                , borderEntry.getValue().getInt("distance", 0)
            );
            this.borders.put(borderEntry.getKey(), border);
            Main.messageManager.log(MessageLevel.CONFIG, border.getDescription());
        }
    }
    
    public Border getBorder(String worldName) {
        return borders.get(worldName);
    }
    
    /**
     * Return player to location inside border.
     * @param player Player to teleport.
     * @param breachedAt Location where player breached the border.
     * @param returnTo Location to teleport player to.
     */
    public Location teleportBack(Player player, Location breachedAt, Location returnTo) {
        if (returnTo == null) {
            // For undefined return locations, use player's spawn.
            returnTo = player.getWorld().getSpawnLocation();
            
            // Use border origin if player's spawn is still outside border.
            if (!this.getBorder(returnTo.getWorld().getName()).contains(returnTo.getX(), returnTo.getZ())) {
                returnTo = this.getBorder(returnTo.getWorld().getName()).getOrigin();
            }
        }
        
        // Debug details for log.
        if (Main.messageManager.isLogLevel(MessageLevel.FINE)) {
            String breachedBy = player.getName();
            if (player.isInsideVehicle()) { breachedBy += " riding " + player.getVehicle().toString().substring("Craft".length()); }
            Main.messageManager.log(
                  MessageLevel.FINE
                , "Border reached in " + breachedAt.getWorld().getName() 
                    + " by " + breachedBy + " at X=" + breachedAt.getBlockX() + ",Y=" + breachedAt.getBlockY() + ",Z=" + breachedAt.getBlockZ()
                    + "; Returning to X=" + returnTo.getBlockX() + ",Y=" + returnTo.getBlockY() + ",Z=" + returnTo.getBlockZ() + "."
            );
            if (Main.messageManager.isLogLevel(MessageLevel.FINER)) {
                Main.messageManager.log(
                        MessageLevel.FINER
                      , "X=" + breachedAt.getX() + ",Y=" + breachedAt.getY() + ",Z=" + breachedAt.getZ()
                          + " > X=" + returnTo.getX() + ",Y=" + returnTo.getY() + ",Z=" + returnTo.getZ()
                );
            }
        }
        
        // Indicate to player that they have reached the border.
        if (this.message != "") Main.messageManager.send(player, MessageLevel.SEVERE, this.message);
        
        // Send player back to inside the border.
        if (player.isInsideVehicle()) {
            returnTo.setY(returnTo.getY() + 1);
            player.getVehicle().setVelocity(new Vector(0, 0, 0));
            player.getVehicle().teleport(returnTo);
        } else {
            player.teleport(returnTo);
        }
        
        return returnTo;
    }   
}
