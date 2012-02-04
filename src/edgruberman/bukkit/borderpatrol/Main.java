package edgruberman.bukkit.borderpatrol;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public final class Main extends JavaPlugin {

    static MessageManager messageManager;

    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }

    public void onEnable() {
        this.loadConfiguration();

        new ImmigrationInspector(this);
        new BorderAgent(this);

        Main.messageManager.log("Plugin Enabled");
    }

    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }

    private void loadConfiguration() {
        FileConfiguration safety = (new ConfigurationFile(this, "safety.yml")).load();
        Main.loadMaterials(safety, "safeContainers", SafetyOfficer.safeContainers);
        Main.loadMaterials(safety, "safeMaterials", SafetyOfficer.safeMaterials);
        SafetyOfficer.safeContainers.addAll(SafetyOfficer.safeMaterials);
        Main.loadMaterials(safety, "unsafeSupports", SafetyOfficer.unsafeSupports);

        FileConfiguration config = (new ConfigurationFile(this)).load();
        BorderAgent.message = config.getString("message");
        BorderAgent.borders.clear();

        World world;
        for (String worldName : config.getConfigurationSection("borders").getKeys(false)) {
            world = this.getServer().getWorld(worldName);
            if (world == null) {
                Main.messageManager.log("Unable to define border for [" + worldName + "]; World not found.", MessageLevel.WARNING);
                continue;
            }

            ConfigurationSection worldBorder = config.getConfigurationSection("borders").getConfigurationSection(worldName);
            Border border = new Border(
                      world
                    , worldBorder.getInt("minX", 0)
                    , worldBorder.getInt("maxX", 0)
                    , worldBorder.getInt("minZ", 0)
                    , worldBorder.getInt("maxZ", 0)
                    , worldBorder.getInt("safe.x", 0)
                    , worldBorder.getInt("safe.y", 0)
                    , worldBorder.getInt("safe.z", 0)
            );
            BorderAgent.borders.put(world, border);
            Main.messageManager.log(border.description(), MessageLevel.CONFIG);
        }
    }

    private static void loadMaterials(final FileConfiguration source, final String entry, Set<Integer> materials) {
        for (String name : source.getStringList(entry)) {
            Material material = Material.valueOf(name);
            if (material == null) {
                Main.messageManager.log("Unable to determine " + entry + " material: " + name, MessageLevel.WARNING);
                continue;
            }

            materials.add(material.getId());
        }
    }

}