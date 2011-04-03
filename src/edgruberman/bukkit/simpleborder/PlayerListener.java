package edgruberman.bukkit.simpleborder;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    private Main main;
    
    public PlayerListener(Main main) {
        this.main = main;
    }
    
    @Override
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        if (this.main.inBounds(event.getPlayer().getLocation())) return;
        
        this.main.teleportBack(event.getPlayer(), null, event.getPlayer().getLocation());
        return;
    }
    
    @Override
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (this.main.inBounds(event.getTo())) return;
        
        event.setCancelled(true);
        this.main.teleportBack(event.getPlayer(), event.getFrom(), event.getTo());
        return;
    }
    
    @Override
    public void onPlayerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        if (this.main.inBounds(event.getTo())) return;
        
        event.setCancelled(true);
        this.main.teleportBack(event.getPlayer(), event.getFrom(), event.getTo());
        return;
    }
}