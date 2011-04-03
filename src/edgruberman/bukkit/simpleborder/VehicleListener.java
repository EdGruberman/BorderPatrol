package edgruberman.bukkit.simpleborder;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simpleborder.Communicator.MessageLevel;

public class VehicleListener extends org.bukkit.event.vehicle.VehicleListener{
    
    private Main main;
    
    public VehicleListener(Main main) {
        this.main = main;
    }
    
    @Override
    public void onVehicleMove(org.bukkit.event.vehicle.VehicleMoveEvent event){
        if (this.main.inBounds(event.getTo())) return;
        
        Entity passenger = event.getVehicle().getPassenger();
        if (!(passenger instanceof Player)) { return; }

        if (this.main.message != "") this.main.communicator.sendMessage((Player) passenger, this.main.message, MessageLevel.WARNING);
        this.main.teleportBack((Player) passenger, event.getFrom(), event.getTo());
        return;
    }
    
}