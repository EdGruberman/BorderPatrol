package edgruberman.bukkit.simpleborder;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Channel;

final class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    public PlayerListener(final Plugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
        pm.registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_PORTAL, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        // Ignore if no border defined for this world or player is still inside border.
        Border border = Border.defined.get(event.getPlayer().getWorld());
        if (border == null || border.isInside(event.getPlayer().getLocation())) return;
        
        PlayerListener.returnInside(event.getPlayer(), event.getPlayer().getLocation());
    }
    
    @Override
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        
        // Ignore if no border defined for this world or player is still inside border.
        Border border = Border.defined.get(event.getPlayer().getWorld());
        if (border == null || border.isInside(event.getTo())) return;
        
        Location inside = PlayerListener.returnInside(event.getPlayer(), event.getTo());
        event.setTo(inside);
    }
    
    @Override
    public void onPlayerPortal(final PlayerPortalEvent event) {
        if (event.isCancelled()) return;
        
        // Ignore if no border defined for target world.
        Border border = Border.defined.get(event.getTo().getWorld());
        if (border == null) return;
        
        PortalBorderAgent pba = new PortalBorderAgent();
        
        Main.messageManager.log(event.getPlayer().getName() + " entered a portal at " + PlayerListener.describeLocation(event.getFrom()), MessageLevel.FINEST);
        Location destination = event.getTo();
        
        // Ensure destination chunks are loaded.
        border.loadChunks(destination, pba.getSearchRadius());
        
        // Search for existing portal within border.
        Main.messageManager.log("Attempting to locate an existing portal for " + event.getPlayer().getName() + " near " + PlayerListener.describeLocation(event.getTo()), MessageLevel.FINEST);
        destination = pba.findPortal(event.getTo());
        Main.messageManager.log("Existing portal found for " + event.getPlayer().getName() + " at " + PlayerListener.describeLocation(destination), MessageLevel.FINEST);
        
        // If no existing portal found, create new portal.
        if (destination == null) {
            destination = event.getTo();
            
            // Ensure destination chunks are loaded.
            border.loadChunks(destination, pba.getCreationRadius());
            
            // Create portal within border.
            Main.messageManager.log("Requesting portal creation for " + event.getPlayer().getName() + " at " + PlayerListener.describeLocation(destination), MessageLevel.FINEST);
            pba.createPortal(destination);
            
            // Find the newly created portal.
            destination = pba.findPortal(destination);
            Main.messageManager.log("Identified newly created portal for " + event.getPlayer().getName() + " at " + PlayerListener.describeLocation(destination), MessageLevel.FINEST);
        }
        
        // Fallback to original location if no portal was able to be created, not sure how/why this would ever happen.
        if (destination == null) destination = event.getTo();
        
        event.useTravelAgent(false);
        event.setTo(destination);
    }
    
    /**
     * Return player to inside border. Notify player with message and log.
     * 
     * @param player player to return inside
     * @param breached where player moved outside the border
     * @return location player was returned to
     */
    private static Location returnInside(final Player player, final Location breached) {
        // Notify player of breaching border.
        if (Border.message.length() != 0) Main.messageManager.send(player, Border.message, MessageLevel.SEVERE, false);
        
        // Return player to inside border.
        Location inside = Border.defined.get(player.getWorld()).returnInside(player, breached);
        
        // Log details for debug if desired.
        if (Main.messageManager.isLevel(Channel.Type.LOG, MessageLevel.FINE)) {
            Main.messageManager.log(
                    "Border reached in [" + breached.getWorld().getName() + "]"
                        + " by " + PlayerListener.describePlayer(player) + " at x: " + breached.getBlockX() + " y: " + breached.getBlockY() + " z: " + breached.getBlockZ()
                        + "; Returned to x: " + inside.getBlockX() + " y: " + inside.getBlockY() + " z: " + inside.getBlockZ()
                    , MessageLevel.FINE
            );
        }
        
        return inside;
    }
    
    /**
     * Generate human readable status of player.
     * 
     * @param player player to describe
     * @return textual representation of player
     */
    private static String describePlayer(final Player player) {
        String description = player.getName();
        
        if (player.isInsideVehicle())
            description += " riding a " + player.getVehicle().toString().substring("Craft".length());
    
        return description;
    }
    
    /**
     * Generate a human readable location reference that shows translated
     * coordinates for portal calculations also.
     * 
     * @param l location to describe
     * @return textual representation of location
     */
    private static String describeLocation(Location l) {
        if (l == null) return null;
        
        boolean normal = l.getWorld().getEnvironment().equals(Environment.NORMAL);
        double toNether = 1D / 8D;
        double toOverworld = 8D;
        double translation = ( normal ? toNether : toOverworld);
        
        return "[" + l.getWorld().getName() + "]"
            + " x:" + l.getBlockX()
            + " y:" + l.getBlockY()
            + " z:" + l.getBlockZ()
            + " | " + ( normal ? "Nether" : "Overworld") + " match is"
            + " x:" + Math.round(l.getX() * translation)
            + " y:" + l.getBlockY()
            + " z:" + Math.round(l.getZ() * translation)
            + " ( "
            + l.getX() + "," + l.getY() + "," + l.getZ()
            + " | "
            + l.getX() * translation
            + "," + l.getY()
            + "," + l.getZ() * translation
            + " )";
    }
}