package edgruberman.bukkit.simpleborder;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    private Main main;
    
    public PlayerListener(Main main) {
        this.main = main;
    }
    
    @Override
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        if (!this.main.isEnabled()) return;
        
        Border border = this.main.getBorder(event.getPlayer().getWorld().getName());
        if (border == null || border.contains(event.getPlayer().getLocation().getX(), event.getPlayer().getLocation().getZ())) return;
        
        this.main.teleportBack(event.getPlayer(), event.getPlayer().getLocation(), border.getInside(event.getPlayer().getLocation()));
        return;
    }
    
    @Override
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        
        if (!this.main.isEnabled()) return;
        
        Border border = this.main.getBorder(event.getPlayer().getWorld().getName());
        if (border == null || border.contains(event.getTo().getX(), event.getTo().getZ())) return;
        
        event.setTo(this.main.teleportBack(event.getPlayer(), event.getTo().clone(), border.getInside(event.getTo())));
        return;
    }
}