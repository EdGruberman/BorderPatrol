package edgruberman.bukkit.borderpatrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static final String MINIMUM_VERSION_CONFIG = "2.0.0a0";
    private static final String MINIMUM_VERSION_SAFETY = "2.0.0a0";

    @Override
    public void onEnable() {
        final ConfigurationFile configurationFile = new ConfigurationFile(this);
        configurationFile.setMinVersion(Main.MINIMUM_VERSION_CONFIG);
        configurationFile.load();
        configurationFile.setLoggingLevel();

        final ConfigurationFile safetyYml = new ConfigurationFile(this, "safety.yml");
        safetyYml.setMinVersion(Main.MINIMUM_VERSION_SAFETY);
        safetyYml.load();
        this.loadSafety(safetyYml.getConfig());

        final List<Border> borders = this.loadBorders(configurationFile.getConfig().getConfigurationSection("borders"));
        final CivilEngineer engineer = new CivilEngineer(borders);
        new BorderAgent(this, engineer, configurationFile.getConfig().getString("message"));
        new ImmigrationInspector(this, engineer);
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
                this.getLogger().log(Level.WARNING, "Unable to determine material: " + name);
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
                this.getLogger().log(Level.WARNING, "Unable to define border; World not found: " +  worldName);
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
            this.getLogger().log(Level.CONFIG, border.description());
        }
        return borders;
    }

}
