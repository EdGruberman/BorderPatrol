package edgruberman.bukkit.simpleborder;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.config.ConfigurationNode;

import edgruberman.bukkit.simpleborder.Communicator.MessageLevel;

public class Main extends org.bukkit.plugin.java.JavaPlugin {

    public String message = "";
    public Communicator communicator = new Communicator(this);
	
    public void onEnable() {
        this.communicator.log("Version " + this.getDescription().getVersion());
        
        Configuration.load(this);
        this.communicator.setLogLevel(Level.parse(this.getConfiguration().getString("logLevel", "INFO")));
       
        this.communicator.setMessageLevel(Level.parse(this.getConfiguration().getString("messageLevel", "INFO")));
        this.message = this.getConfiguration().getString("message");
        
        // Additional entries in log showing interpretation of borders in configuration file.
        for (String key : this.getConfiguration().getKeys("borders")) {
            ConfigurationNode node = this.getConfiguration().getNode("borders." + key);
            this.communicator.log(Level.CONFIG, "Border defined for " + key
                + " as " + node.getInt("distance", 0)
                + " from origin of X=" + node.getDouble("origin.X", 0)
                + ",Y=" + node.getDouble("origin.Y", 0)
                + ",Z=" + node.getDouble("origin.Z", 0)
            );
        }
        
        // Only register events to listen for if a border is defined.
        if (this.getConfiguration().getNodes("borders").size() > 0) { this.registerEvents(); }
        
        this.communicator.log("Plugin Enabled");
    }
    
    public void onDisable() {
        this.communicator.log("Plugin Disabled");
    }
    
    private void registerEvents() {
        PlayerListener playerListener = new PlayerListener(this);
        VehicleListener vehicleListener = new VehicleListener(this);
        
        org.bukkit.plugin.PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE    , playerListener,  Event.Priority.Highest, this);
        pluginManager.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener,  Event.Priority.Highest, this);
        pluginManager.registerEvent(Event.Type.PLAYER_JOIN    , playerListener,  Event.Priority.Highest, this);
        pluginManager.registerEvent(Event.Type.VEHICLE_MOVE   , vehicleListener, Event.Priority.Highest, this);
    }
    
    /**
     * Returns location for origin of defined border.
     * 
     * @param world = World to return origin of defined border in.
     * @return = Location of origin.
     */
    public Location getOrigin(World world) {
        ConfigurationNode worldBorder = this.getConfiguration().getNode("borders." + world.getName());
        return new Location(world, worldBorder.getInt("origin.X", 0), worldBorder.getInt("origin.Y", 0), worldBorder.getInt("origin.Z", 0));
    }
    
    /**
     * Determines if location is defined as being within the border or not.
     * 
     * @param location = Location to check if it is within the border.
     * @return = true if Location is within the border; false otherwise.
     */
    public boolean inBounds(org.bukkit.Location location) {
        // If the world player is in has no border defined, they should be allowed to move anywhere.
        ConfigurationNode worldBorder = this.getConfiguration().getNode("borders." + location.getWorld().getName());
        if (worldBorder == null) {
            return true;
        }
        
        // If the border does not have a positive value defined for distance, allow the player to move anywhere.
        int distance = worldBorder.getInt("distance", 0);
        if (distance <= 0) {
            return true;
        }
        
        // Ensure the player's distance from the origin on either the X or the Z axis is within the defined distance.
        double originX = worldBorder.getDouble("origin.X", 0);
        double originZ = worldBorder.getDouble("origin.Z", 0);
        if ( (Math.abs(location.getX() - originX) <= distance)
                && (Math.abs(location.getZ() - originZ) <= distance)
            ) {
            return true;
        }
        
        // Getting this far in the logic means the player is outside the defined border.
        return false;
    }
    
    /**
     * Return player to location inside border.
     * 
     * @param player = Player to teleport.
     * @param returnTo = Location to teleport player to.
     * @param breachedAt = Location where player breached the border.
     */
    public void teleportBack(Player player, Location returnTo, Location breachedAt) {
        if (returnTo == null) {
            // For undefined return locations, use player's default spawn.
            returnTo = player.getWorld().getSpawnLocation();
            // Use border origin if spawn is also outside border.
            if (!this.inBounds(returnTo)) { returnTo = this.getOrigin(player.getWorld()); }
        } else {
            // Determine axis that was breached and move player back to first safe location 3 blocks back.
            ConfigurationNode worldBorder = this.getConfiguration().getNode("borders." + breachedAt.getWorld().getName());
            int distance = worldBorder.getInt("distance", 0);
            double originX = worldBorder.getDouble("origin.X", 0);
            if (Math.abs(breachedAt.getBlockX() - originX) > distance) {
                returnTo.setX(
                    (Math.abs(breachedAt.getX()) - 3)
                    * Math.signum(breachedAt.getX())
                );
            }
            else {
                returnTo.setZ(
                        (Math.abs(breachedAt.getZ()) - 3)
                        * Math.signum(breachedAt.getZ())
                    );
            }
        }
        
        // Debug details for log.
        if (this.communicator.isLogLevel(Level.FINE)) {
            String breachedBy = player.getName();
            if (player.getVehicle() != null) { breachedBy += " riding " + player.getVehicle().toString(); }
            this.communicator.log(
                  Level.FINE
                , "Border reached in " + breachedAt.getWorld().getName() 
                    + " by " + breachedBy + " at X=" + breachedAt.getBlockX() + ",Y=" + breachedAt.getBlockY() + ",Z=" + breachedAt.getBlockZ()
                    + "; Returning to X=" + returnTo.getBlockX() + ",Y=" + returnTo.getBlockY() + ",Z=" + returnTo.getBlockZ() + "."
            );
            if (this.communicator.isLogLevel(Level.FINER)) {
                this.communicator.log(
                        Level.FINER
                      , "X=" + breachedAt.getX() + ",Y=" + breachedAt.getY() + ",Z=" + breachedAt.getZ()
                          + " > X=" + returnTo.getX() + ",Y=" + returnTo.getY() + ",Z=" + returnTo.getZ()
                );
            }
        }
        
        // Indicate to player that they have reached the border.
        if (this.message != "") this.communicator.sendMessage(player, this.message, MessageLevel.WARNING);
        
        // Send player back to within the border.
        Location safe = this.firstSafe(returnTo);
        if (safe == null) { safe = this.getOrigin(player.getWorld()); }
        if (player.getVehicle() != null) { player.getVehicle().teleport(safe); }
        else { player.teleport(safe); }
    }
    
    /**
     * Determines the first location of a player safe (2 blocks high) position at or above current location.
     * 
     * @param current = Current location to determine if it is safe for a player.
     * @return = Location of safe position.
     */
    public Location firstSafe(Location current) {
        World world = current.getWorld();
        int x = current.getBlockX();
        int y = Math.max(0, current.getBlockY());
        int z = current.getBlockZ();

        byte safe = 0;
        while (y <= 129) {
            if (this.isSafe(world.getBlockAt(x, y, z))) {
                safe++;
            } else {
                safe = 0;
            }

            if (safe == 2) {
                return new Location(world, x + 0.5, y - 1, z + 0.5);
            }
            y++;
        }
        
        return null;
    }
    
    /**
     * Determines if block is made of material that is safe for a player to occupy.
     * 
     * @param block = Block to determine if material is player safe.
     * @return = If block is safe for player to be in, then true; Otherwise false.
     */
    public boolean isSafe(Block block) {
             if (block.getType() == Material.AIR) { return true; }
        else if (block.getType() == Material.WATER) { return true; }
        else if (block.getType() == Material.SAPLING) { return true; }
        else if (block.getType() == Material.YELLOW_FLOWER) { return true; }
        else if (block.getType() == Material.RED_ROSE) { return true; }
        else if (block.getType() == Material.BROWN_MUSHROOM) { return true; }
        else if (block.getType() == Material.RED_MUSHROOM) { return true; }
        else if (block.getType() == Material.TORCH) { return true; }
        else if (block.getType() == Material.REDSTONE_WIRE) { return true; }
        else if (block.getType() == Material.CROPS) { return true; }
        else if (block.getType() == Material.SIGN_POST) { return true; }
        else if (block.getType() == Material.LADDER) { return true; }
        else if (block.getType() == Material.RAILS) { return true; }
        else if (block.getType() == Material.WALL_SIGN) { return true; }
        else if (block.getType() == Material.LEVER) { return true; }
        else if (block.getType() == Material.STONE_PLATE) { return true; }
        else if (block.getType() == Material.WOOD_PLATE) { return true; }
        else if (block.getType() == Material.REDSTONE_TORCH_OFF) { return true; }
        else if (block.getType() == Material.REDSTONE_TORCH_ON) { return true; }
        else if (block.getType() == Material.STONE_BUTTON) { return true; }
        else if (block.getType() == Material.SNOW) { return true; }
        else if (block.getType() == Material.SUGAR_CANE_BLOCK) { return true; }

        return false;
    }
    
}
