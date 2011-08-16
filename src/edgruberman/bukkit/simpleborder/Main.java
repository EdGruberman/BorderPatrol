package edgruberman.bukkit.simpleborder;

import java.util.Map;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.ConfigurationNode;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends JavaPlugin {
    
    static MessageManager messageManager;
    
    private static ConfigurationFile configurationFile;
    
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Main.configurationFile = new ConfigurationFile(this);
    }
	
    public void onEnable() {
        Border.message = Main.configurationFile.getConfiguration().getString("message");
        this.loadBorders();
        
        new PlayerListener(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Border.defined.clear();
        
        Main.messageManager.log("Plugin Disabled");
    }
    
    private void loadBorders() {
        Border.defined.clear();
        
        World world;
        for (Map.Entry<String, ConfigurationNode> borderEntry : Main.configurationFile.getConfiguration().getNodes("borders").entrySet()) {
            world = this.getServer().getWorld(borderEntry.getKey());
            if (world == null) {
                Main.messageManager.log("Unable to define border for [" + borderEntry.getKey() + "]; World not found.", MessageLevel.WARNING);
                continue;
            }
            
            Border border = new Border(
                      world
                    , borderEntry.getValue().getInt("minX", 0)
                    , borderEntry.getValue().getInt("maxX", 0)
                    , borderEntry.getValue().getInt("minZ", 0)
                    , borderEntry.getValue().getInt("maxZ", 0)
                    , borderEntry.getValue().getInt("default.x", 0)
                    , borderEntry.getValue().getInt("default.y", 0)
                    , borderEntry.getValue().getInt("default.z", 0)
            );
            Main.messageManager.log(border.description(), MessageLevel.CONFIG);
        }
    }
}