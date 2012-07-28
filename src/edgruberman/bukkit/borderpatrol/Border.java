package edgruberman.bukkit.borderpatrol;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

/** Square region defining a boundary in a world that players are not allowed to cross */
final class Border {

    private final World world;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;
    private final Location safe;
    int padding = 3;
    int search = 32; // Maximum distance to add to padding when searching for a safe location

    Border(final World world, final int minX, final int maxX, final int minZ, final int maxZ, final int safeX, final int safeY, final int safeZ) {
        this.world = world;
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;

        if (!this.contains(safeX, safeZ))
            new IllegalArgumentException("Default safe coordinates are not within border");

        this.safe = new Location(this.world, safeX, safeY, safeZ);
    }

    World getWorld() {
        return this.world;
    }

    /**
     * Generates text describing this border's configuration.
     *
     * @return border configuration in human readable format
     */
    String description() {
        return "Border in [" + this.world.getName() + "]"
            + " is x:{" + this.minX + " to " + this.maxX + "}"
            + " and z:{" + this.minZ + " to " + this.maxZ + "}"
            + " with the default safe block at x:" + this.safe.getBlockX() + " y:" + this.safe.getBlockY() + " z:" + this.safe.getBlockZ();
    }

    /**
     * Determines if chunk has any block that is found within or on the border.
     *
     * @param c chunk to check
     * @return true if chunk has a block inside border; otherwise false
     */
    boolean contains(final Chunk c) {
        // Check all four corners of chunk, ignoring depth.
        if (this.contains(c.getBlock(0, 0, 0).getLocation())) return true;
        if (this.contains(c.getBlock(15, 0, 0).getLocation())) return true;
        if (this.contains(c.getBlock(0, 0, 15).getLocation())) return true;
        if (this.contains(c.getBlock(15, 0, 15).getLocation())) return true;

        return false;
    }

    /**
     * Determines if location is found within or on the border.
     *
     * @param l location to check
     * @return true if location is inside border; otherwise false
     */
    boolean contains(final Location l) {
        return this.contains(l.getX(), l.getZ());
    }

    /**
     * Determines if coordinates are found within or on the border.
     *
     * @param x X axis value
     * @param z Z axis value
     * @return true if coordinates are inside border; otherwise false
     */
    boolean contains(final double x, final double z) {
        return !(x <= this.minX || x >= this.maxX || z <= this.minZ || z >= this.maxZ);
    }

    /**
     * Returns the closest location to the target inside the border.
     *
     * @param target location to try and get closest to
     * @param padding margin from border
     * @return new location inside border closest to target
     */
    Location findClosest(final Location target, final double padding) {
        final Location closest = target.clone();

        if (closest.getX() <= (this.minX + padding))
            closest.setX(this.minX + padding);
        else if (closest.getX() >= (this.maxX - padding))
            closest.setX(this.maxX - padding);

        if (closest.getZ() <= (this.minZ + padding))
            closest.setZ(this.minZ + padding);
        else if (closest.getZ() >= (this.maxZ - padding))
            closest.setZ(this.maxZ - padding);

        return closest;
    }

    /**
     * Attempts to find a player safe location inside the border closest to the
     * given target. If a safe location can not be found, the default safe
     * location for this border is used.
     *
     * @param target location to try and find the closest safe location to
     * @return safe location inside border closest to target
     */
    Location findSafe(final Location target) {
        int padding = this.padding;
        Location safe = null;
        while (safe == null && padding <= this.search) {
            final Location closest = this.findClosest(target, padding);
            safe = SafetyOfficer.findSafeY(closest);
            padding++;
        }
        // TODO log a warning when safe is used
        if (safe == null) safe = this.safe;
        return safe;
    }

}
