package edgruberman.bukkit.borderpatrol;

import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public final class Main extends JavaPlugin {

    private static final String MINIMUM_VERSION_CONFIG = "2.0.0a0";
    private static final String MINIMUM_VERSION_SAFETY = "2.0.0a0";

    static MessageManager messageManager;

    private ConfigurationFile configurationFile;

    @Override
    public void onEnable() {
        this.configurationFile = new ConfigurationFile(this);
        this.configurationFile.setMinVersion(Main.MINIMUM_VERSION_CONFIG);
        this.configurationFile.load();
        this.setLoggingLevel();

        Main.messageManager = new MessageManager(this);

        this.configure();

        new ImmigrationInspector(this);
        new BorderAgent(this);
    }

    private void setLoggingLevel() {
        final String name = this.configurationFile.getConfig().getString("logLevel", "INFO");
        Level level = MessageLevel.parse(name);
        if (level == null) level = Level.INFO;

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().log(Level.CONFIG, "Logging level set to: " + this.getLogger().getLevel());
    }

    private void configure() {
        final ConfigurationFile safetyYml = new ConfigurationFile(this, "safety.yml");
        safetyYml.setMinVersion(Main.MINIMUM_VERSION_SAFETY);
        final FileConfiguration safety = safetyYml.load();

        this.loadMaterials(safety, "safeContainers", SafetyOfficer.safeContainers);
        this.loadMaterials(safety, "safeMaterials", SafetyOfficer.safeMaterials);
        SafetyOfficer.safeContainers.addAll(SafetyOfficer.safeMaterials);
        this.loadMaterials(safety, "unsafeSupports", SafetyOfficer.unsafeSupports);

        final FileConfiguration config = this.configurationFile.getConfig();
        BorderAgent.message = config.getString("message");
        BorderAgent.borders.clear();

        World world;
        for (final String worldName : config.getConfigurationSection("borders").getKeys(false)) {
            world = this.getServer().getWorld(worldName);
            if (world == null) {
                this.getLogger().log(Level.WARNING, "Unable to define border for [" + worldName + "]; World not found.");
                continue;
            }

            final ConfigurationSection worldBorder = config.getConfigurationSection("borders").getConfigurationSection(worldName);
            final Border border = new Border(
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
            this.getLogger().log(Level.CONFIG, border.description());
        }
    }

    private void loadMaterials(final FileConfiguration source, final String entry, final Set<Integer> materials) {
        for (final String name : source.getStringList(entry)) {
            final Material material = Material.valueOf(name);
            if (material == null) {
                this.getLogger().log(Level.WARNING, "Unable to determine " + entry + " material: " + name);
                continue;
            }

            materials.add(material.getId());
        }
    }

}
