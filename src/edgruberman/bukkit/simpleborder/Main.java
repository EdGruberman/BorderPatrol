package edgruberman.bukkit.simpleborder;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends JavaPlugin {
    
    static MessageManager messageManager;
    
    private static ConfigurationFile configurationFile;
    
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Main.configurationFile = new ConfigurationFile(this);
        Border.message = Main.configurationFile.getConfig().getString("message");
    }
	
    public void onEnable() {
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
        for (String worldName : Main.configurationFile.getConfig().getConfigurationSection("borders").getKeys(false)) {
            world = this.getServer().getWorld(worldName);
            if (world == null) {
                Main.messageManager.log("Unable to define border for [" + worldName + "]; World not found.", MessageLevel.WARNING);
                continue;
            }
            
            ConfigurationSection worldBorder = Main.configurationFile.getConfig().getConfigurationSection("borders").getConfigurationSection(worldName);
            Border border = new Border(
                      world
                    , worldBorder.getInt("minX", 0)
                    , worldBorder.getInt("maxX", 0)
                    , worldBorder.getInt("minZ", 0)
                    , worldBorder.getInt("maxZ", 0)
                    , worldBorder.getInt("default.x", 0)
                    , worldBorder.getInt("default.y", 0)
                    , worldBorder.getInt("default.z", 0)
            );
            Main.messageManager.log(border.description(), MessageLevel.CONFIG);
        }
    }
}