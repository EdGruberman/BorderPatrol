package edgruberman.bukkit.simpleborder;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Border {
    
    private World world;
    private int originX;
    private int originY;
    private int originZ;
    private int distance;
    
    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;
    
    public Border(World world, int originX, int originY, int originZ, int distance) {
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
    
    public Block getOrigin() {
        return this.world.getBlockAt(this.originX, this.originY, this.originZ);
    }
    
    /**
     * Determines if location is defined as being within the border or not.
     * 
     * @param X X Axis value to check if it is inside the border.
     * @param Z Z Axis value to check if it is inside the border.
     * @return true if Location is inside the border; Otherwise false.
     */
    public boolean contains(int X, int Z) {
        return !(X < this.minX || X > this.maxX || Z < this.minZ || Z > this.maxZ);
    }
    
    /**
     * Returns the closest block to the provided target just inside the border that is safe for a player to occupy.
     * 
     * @param target Location to get as close as possible to but still within border.
     * @return
     */    
    public Location getInside(Location target) {
        Location inside = target.clone();
        
        if (inside.getX() <= this.minX)
            inside.setX(this.minX + 3);
        else if (inside.getX() >= this.maxX)
            inside.setX(this.maxX - 3);
        
        if (inside.getZ() <= this.minZ)
            inside.setZ(this.minZ + 3);
        else if (inside.getZ() >= this.maxZ)
            inside.setZ(this.maxZ - 3);

        Block block = WorldUtility.getSafeY(inside.getBlock());
        if (block == null) return null;

        inside.setX(block.getX() + 0.5);
        inside.setY(block.getY());
        inside.setZ(block.getZ() + 0.5);
        return inside;
    }
}