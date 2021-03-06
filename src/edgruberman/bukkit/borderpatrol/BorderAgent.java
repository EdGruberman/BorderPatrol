package edgruberman.bukkit.borderpatrol;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/** Ensures players stay within the defined borders */
final class BorderAgent implements Listener {

    private final Plugin plugin;
    private final CivilEngineer engineer;
    private final boolean netherRoof;

    BorderAgent(final Plugin plugin, final CivilEngineer engineer, final boolean netherRoof) {
        this.plugin = plugin;
        this.engineer = engineer;
        this.netherRoof = netherRoof;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        // Ignore if no border defined for this world or player is still inside border
        final Border border = this.engineer.getBorder(event.getPlayer().getWorld());
        if (border == null || border.contains(event.getPlayer().getLocation())) return;

        this.enforce(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        // Ignore if no border defined for this world or player is still inside border
        final Border border = this.engineer.getBorder(event.getRespawnLocation().getWorld());
        if (border == null || border.contains(event.getRespawnLocation())) return;

        final Location inside = this.enforce(event.getPlayer(), event.getRespawnLocation());
        event.setRespawnLocation(inside);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent move) {
        if (!this.netherRoof
                && move.getTo().getWorld().getEnvironment() == Environment.NETHER
                && move.getTo().getBlockY() >= 128
                && move.getPlayer().getFireTicks() <= 0)
            move.getTo().getBlock().setType(Material.FIRE);

        // Ignore if no border defined for destination world or player will still be inside border
        final Border border = this.engineer.getBorder(move.getTo().getWorld());
        if (border == null || border.contains(move.getTo())) return;

        final Location inside = this.enforce(move.getPlayer(), move.getTo());
        move.setTo(inside);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent teleport) {
        // Ignore if no border defined for destination world or player will still be inside border
        final Border border = this.engineer.getBorder(teleport.getTo().getWorld());
        if (border == null || border.contains(teleport.getTo())) return;

        final Location inside = this.enforce(teleport.getPlayer(), teleport.getTo());
        teleport.setTo(inside);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(final PortalCreateEvent create) {
        if (this.netherRoof || create.getWorld().getEnvironment() != Environment.NETHER) return;
        for (final Block block : create.getBlocks())
            if (block.getY() > 128)
                create.setCancelled(true);
    }

    /**
     * Return player to inside border, notify player with message and log
     *
     * @param suspect who to return
     * @param breached where border was crossed
     * @return where suspect was sent
     */
    private Location enforce(final Player suspect, final Location breached) {
        // Notify player of breaching border
        Main.courier.send(suspect, "return");

        // Find a safe location to return player to
        final Border border = this.engineer.getBorder(suspect.getWorld());
        final Location returned = border.findSafe(breached);

        // Return player to the middle of the block and players in vehicles need to be shifted up
        returned.setX(Math.floor(returned.getX()) + 0.5);
        if (suspect.isInsideVehicle()) returned.add(0, 1, 0);
        returned.setZ(Math.floor(returned.getZ()) + 0.5);

        // Send player back to inside the border
        if (suspect.isInsideVehicle()) {
            suspect.getVehicle().setVelocity(new Vector(0, 0, 0));
            suspect.getVehicle().teleport(returned);
        } else {
            suspect.teleport(returned);
        }

        // Log details for debug if configured
        if (this.plugin.getLogger().isLoggable(Level.FINE))
            this.plugin.getLogger().fine(BorderAgent.report(suspect, breached, returned));

        return returned;
    }

    /**
     * Generate human readable status of player
     *
     * @param suspect who breached the border
     * @param breached where border was crossed
     * @param returned where suspect was sent
     * @return textual representation of player
     */
    private static String report(final Player suspect, final Location breached, final Location returned) {
        String suspectDescription = suspect.getName();
        if (suspect.isInsideVehicle())
            suspectDescription += " riding a " + suspect.getVehicle().toString().substring("Craft".length());

        return "Border breached in [" + breached.getWorld().getName() + "]"
                + " by " + suspectDescription + " at x: " + breached.getBlockX() + " y: " + breached.getBlockY() + " z: " + breached.getBlockZ()
                + "; Returned to x: " + returned.getBlockX() + " y: " + returned.getBlockY() + " z: " + returned.getBlockZ();
    }

}
