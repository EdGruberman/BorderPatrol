package edgruberman.bukkit.simpleborder;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Border {
    
    private World world;
    private double originX;
    private double originY;
    private double originZ;
    private int distance;
    
    private double minX;
    private double maxX;
    private double minZ;
    private double maxZ;
    
    public Border(World world, double originX, double originY, double originZ, int distance) {
        this.world = world;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.distance = distance;

        this.minX = originX - distance;
        this.maxX = originX + distance;
        this.minZ = originZ - distance;
        this.maxZ = originZ + distance;
    }
    
    public String getDescription() {
        return "Border in \"" + this.world.getName() + "\""
            + " is " + this.distance
            + " from the origin of X=" + this.originX
            + ",Y=" + this.originY
            + ",Z=" + this.originZ;
    }
    
    public Location getOrigin() {
        return new Location(this.world, this.originX, this.originY, this.originZ);
    }
    
    /**
     * Determines if location is defined as being within the border or not.
     * @param X X Axis value to check if it is inside the border.
     * @param Z Z Axis value to check if it is inside the border.
     * @return true if Location is inside the border; Otherwise false.
     */
    public boolean isInside(double X, double Z) {
        return !(X < this.minX || X > this.maxX || Z < this.minZ || Z > this.maxZ);
    }
    
    /**
     * Returns the closest location to the provided location just inside the border that is safe for a player to occupy. 
     * @param location
     * @return
     */
    public Location getInside(Location location) {
        
        if (location.getX() <= this.minX)
            location.setX(this.minX + 3);
        else if (location.getX() >= this.maxX)
            location.setX(this.maxX - 3);
        
        if (location.getZ() <= this.minZ)
            location.setZ(this.minZ + 3);
        else if (location.getZ() >= this.maxZ)
            location.setZ(this.maxZ - 3);

        Block block = WorldUtility.getSafeY(location.getBlock());
        if (block == null) return null;

        location.setX(block.getX() + 0.5);
        location.setY(block.getY());
        location.setZ(block.getZ() + 0.5);
        return location;
    }
}