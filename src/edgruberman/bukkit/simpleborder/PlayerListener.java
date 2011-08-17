package edgruberman.bukkit.simpleborder;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Channel;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    public PlayerListener(final Plugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Normal, plugin);
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
        
        // Ensure destination chunks are loaded.
        TravelAgent pta = event.getPortalTravelAgent();
        Chunk maxC = event.getTo().clone().add(pta.getSearchRadius(), 0, pta.getSearchRadius()).getBlock().getChunk();
        Chunk minC = event.getTo().clone().subtract(pta.getSearchRadius(), 0, pta.getSearchRadius()).getBlock().getChunk();
        Chunk c;
        for (int x = minC.getX(); x <= maxC.getX(); x++)
            for (int z = minC.getZ(); z <= maxC.getZ(); z++) {
                c = event.getTo().getWorld().getChunkAt(x, z);
                if (!c.isLoaded() && border.isInside(c)) c.load();
            }
        
        // Check for existing portal inside border.
        Main.messageManager.log("trying to find portal at " + event.getTo());
        Location destination = pta.findPortal(event.getTo());
        Main.messageManager.log("portal found at " + destination);
        
        // If no existing portal or existing portal is not inside border, create new portal.
        if (destination == null || !border.isInside(destination)) {
            // Ensure target creation radius and portal size is fully contained in border.
            destination = border.findClosest(event.getTo(), pta.getCreationRadius() + 4);
            Main.messageManager.log("requsting portal creation at " + destination);
            pta.createPortal(destination);
        }
        
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
}