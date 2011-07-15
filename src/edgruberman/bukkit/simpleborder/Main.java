package edgruberman.bukkit.simpleborder;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.bukkit.util.config.ConfigurationNode;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;
import edgruberman.bukkit.messagemanager.channels.Channel;

public class Main extends org.bukkit.plugin.java.JavaPlugin {

    static ConfigurationFile configurationFile;
    static MessageManager messageManager;
    
    private String message = "";
    private Map<String, Border> borders = new HashMap<String, Border>();
    
    public void onLoad() {
        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.load();
        
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }
	
    public void onEnable() {
        this.message = this.getConfiguration().getString("message");
        this.loadBorders();
        
        this.registerEvents();
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
        Main.messageManager = null;
    }
    
    private void registerEvents() {
        PlayerListener playerListener = new PlayerListener(this);
        
        org.bukkit.plugin.PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Highest, this);
        pluginManager.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
    }
    
    private void loadBorders() {
        this.borders.clear();
        
        World world;
        for (Map.Entry<String, ConfigurationNode> borderEntry : this.getConfiguration().getNodes("borders").entrySet()) {
            if (borderEntry.getValue().getInt("distance", 0) == 0) continue;
            
            world = this.getServer().getWorld(borderEntry.getKey());
            if (world == null) {
                Main.messageManager.log("Unable to define border for \"" + borderEntry.getKey() + "\"; World not found.", MessageLevel.WARNING);
                continue;
            }
            
            Border border = new Border(
                      world
                    , borderEntry.getValue().getInt("origin.x", 0)
                    , borderEntry.getValue().getInt("origin.y", 0)
                    , borderEntry.getValue().getInt("origin.z", 0)
                    , borderEntry.getValue().getInt("distance", 0)
            );
            this.borders.put(borderEntry.getKey(), border);
            Main.messageManager.log(border.getDescription(), MessageLevel.CONFIG);
        }
    }
    
    public Border getBorder(String worldName) {
        return borders.get(worldName);
    }
    
    /**
     * Return player to inside border.
     * @param player Player to teleport.
     * @param breachedAt Block where player breached the border.
     * @param returnTo Block to teleport player to center of.
     */
    public Location teleportBack(Player player, Location breachedAt, Location returnTo) {
        if (returnTo == null) {
            // For undefined return locations, use player's spawn.
            returnTo = player.getWorld().getSpawnLocation();
            
            // Use border origin if player's spawn is still outside border.
            if (!this.getBorder(returnTo.getWorld().getName()).contains(returnTo.getBlockX(), returnTo.getBlockZ())) {
                returnTo = this.getBorder(returnTo.getWorld().getName()).getOrigin().getLocation();
            }
        }
        
        // Set return location to middle of identified block.
        if (player.isInsideVehicle()) returnTo.setY(returnTo.getY() + 1);
        
        // Debug details for log.
        if (Main.messageManager.isLevel(Channel.Type.LOG, MessageLevel.FINE)) {
            String breachedBy = player.getName();
            if (player.isInsideVehicle()) { breachedBy += " riding " + player.getVehicle().toString().substring("Craft".length()); }
            Main.messageManager.log(
                    "Border reached in " + breachedAt.getWorld().getName() 
                        + " by " + breachedBy + " at x: " + breachedAt.getBlockX() + " y: " + breachedAt.getBlockY() + " z: " + breachedAt.getBlockZ()
                        + "; Returning to x: " + returnTo.getBlockX() + " y: " + returnTo.getBlockY() + " z: " + returnTo.getBlockZ()
                    , MessageLevel.FINE
            );
        }
        
        // Indicate to player that they have reached the border.
        if (!this.message.equals("")) Main.messageManager.send(player, this.message, MessageLevel.SEVERE);
        
        // Send player back to inside the border.
        if (player.isInsideVehicle()) {
            player.getVehicle().setVelocity(new Vector(0, 0, 0));
            player.getVehicle().teleport(returnTo);
        } else {
            player.teleport(returnTo);
        }
        
        return returnTo;
    }   
}
