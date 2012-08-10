package edgruberman.bukkit.borderpatrol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.borderpatrol.commands.Reload;
import edgruberman.bukkit.borderpatrol.messaging.couriers.ConfigurationCourier;
import edgruberman.bukkit.borderpatrol.messaging.couriers.TimestampedConfigurationCourier;

public final class Main extends JavaPlugin {

    private static final Version MINIMUM_CONFIGURATION = new Version("2.2.0");
    private static final Version MINIMUM_SAFETY = new Version("2.2.0");

    public static ConfigurationCourier courier;

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = new TimestampedConfigurationCourier(this);

        this.loadSafety(this.loadConfig("safety.yml", Main.MINIMUM_SAFETY));
        final List<Border> borders = this.loadBorders(this.getConfig().getConfigurationSection("borders"));
        final CivilEngineer engineer = new CivilEngineer(borders);
        new BorderAgent(this, engineer);
        new ImmigrationInspector(this, engineer);

        this.getCommand("borderpatrol:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    private void loadSafety(final ConfigurationSection safety) {
        this.loadMaterials(safety, "safeContainers", SafetyOfficer.safeContainers);
        this.loadMaterials(safety, "safeMaterials", SafetyOfficer.safeMaterials);
        SafetyOfficer.safeContainers.addAll(SafetyOfficer.safeMaterials);
        this.loadMaterials(safety, "unsafeSupports", SafetyOfficer.unsafeSupports);
    }

    private void loadMaterials(final ConfigurationSection type, final String entry, final Set<Integer> materials) {
        if (!type.isSet(entry)) return;

        for (final String name : type.getStringList(entry)) {
            final Material material = Material.valueOf(name);
            if (material == null) {
                this.getLogger().warning("Unable to determine material: " + name);
                continue;
            }

            materials.add(material.getId());
        }
    }

    private List<Border> loadBorders(final ConfigurationSection sectionBorders) {
        if (sectionBorders == null) return Collections.emptyList();

        final List<Border> borders = new ArrayList<Border>();

        for (final String worldName : sectionBorders.getKeys(false)) {
            final World world = this.getServer().getWorld(worldName);
            if (world == null) {
                this.getLogger().warning("Unable to define border; World not found: " +  worldName);
                continue;
            }

            final ConfigurationSection worldBorder = sectionBorders.getConfigurationSection(worldName);
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
            borders.add(border);
            this.getLogger().config(border.description());
        }
        return borders;
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();
        this.setLogLevel(this.getConfig().getString("logLevel"));

        final Version version = new Version(this.getConfig().getString("version"));
        if (version.compareTo(Main.MINIMUM_CONFIGURATION) >= 0) return;

        this.archiveConfig("config.yml", version);
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    @Override
    public void saveDefaultConfig() {
        this.extractConfig("config.yml", false);
    }

    private void archiveConfig(final String resource, final Version version) {
        final String backupName = "%1$s - Archive version %2$s - %3$tY%3$tm%3$tdT%3$tH%3$tM%3$tS.yml";
        final File backup = new File(this.getDataFolder(), String.format(backupName, resource.replaceAll("(?i)\\.yml$", ""), version, new Date()));
        final File existing = new File(this.getDataFolder(), resource);

        if (!existing.renameTo(backup))
            throw new IllegalStateException("Unable to archive configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");

        this.getLogger().warning("Archived configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");
    }

    private void extractConfig(final String resource, final boolean replace) {
        final Charset source = Charset.forName("UTF-8");
        final Charset target = Charset.defaultCharset();
        if (target.equals(source)) {
            super.saveResource(resource, replace);
            return;
        }

        final File config = new File(this.getDataFolder(), resource);
        if (config.exists()) return;

        final char[] cbuf = new char[1024]; int read;
        try {
            final Reader in = new BufferedReader(new InputStreamReader(this.getResource(resource), source));
            final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config), target));
            while((read = in.read(cbuf)) > 0) out.write(cbuf, 0, read);
            out.close(); in.close();

        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not extract configuration file \"" + resource + "\" to " + config.getPath() + "\";" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private Configuration loadConfig(final String resource, final Version required) {
        // Extract default if not existing
        this.extractConfig(resource, false);

        final File existing = new File(this.getDataFolder(), resource);
        final Configuration config = YamlConfiguration.loadConfiguration(existing);
        if (required == null) return config;

        // Verify required or later version
        final Version version = new Version(config.getString("version"));
        if (version.compareTo(required) >= 0) return config;

        this.archiveConfig(resource, version);

        // Extract default and reload
        return this.loadConfig(resource, null);
    }

    private void setLogLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
    }

}
