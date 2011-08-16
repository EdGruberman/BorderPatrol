package edgruberman.bukkit.simpleborder;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Channel;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    public PlayerListener(final Plugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Normal, plugin);
        pm.registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onPlayerJoin(final PlayerJoinEvent event) {
        // Ignore if no border defined for this world or player is still inside border.
        Border border = Border.defined.get(event.getPlayer().getWorld());
        if (border == null || border.isInside(event.getPlayer().getLocation())) return;
        
        PlayerListener.returnInside(event.getPlayer(), event.getPlayer().getLocation());
        return;
    }
    
    @Override
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        
        // Ignore if no border defined for this world or player is still inside border.
        Border border = Border.defined.get(event.getPlayer().getWorld());
        if (border == null || border.isInside(event.getTo())) return;
        
        Location inside = PlayerListener.returnInside(event.getPlayer(), event.getTo());
        event.setTo(inside);
        return;
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
        if (Border.message.length() != 0) Main.messageManager.send(player, Border.message, MessageLevel.SEVERE);
        
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