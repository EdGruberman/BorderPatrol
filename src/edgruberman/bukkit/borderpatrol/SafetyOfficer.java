package edgruberman.bukkit.borderpatrol;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Utility class for finding safe locations for players
 * TODO move safety related methods from Border into here and remove static nature
 */
final class SafetyOfficer {

    /**  Materials safe for a player to occupy */
    static Set<Integer> safeContainers = new HashSet<Integer>();

    /** Materials safe for a player to occupy and be placed above */
    static Set<Integer> safeMaterials = new HashSet<Integer>();

    /** Materials unsafe for a player to be placed above and not in safeContainers */
    static Set<Integer> unsafeSupports = new HashSet<Integer>();

    /**
     * Find the closest safe block for a player to occupy along the Y axis
     *
     * @param location initial start to search up and down from
     * @return closest safe spot for a player to occupy
     */
    static Location findSafeY(final Location location) {
        final Location safe = location.clone();
        final World world = safe.getWorld();
        final int x = safe.getBlockX();
        int y = safe.getBlockY();
        final int z = safe.getBlockZ();

        // Chunk must be loaded in order to return proper type IDs
        if (!world.isChunkLoaded(x >> 4, z >> 4)) world.loadChunk(x >> 4, z >> 4);

        if (SafetyOfficer.isSafe(world, x, y ,z)) return safe;

        final int bottom = 0, top = world.getMaxHeight() - 1;
        int below = y, above = y++;
        while ((below > bottom) || (above < top)) {

            if (below >= bottom) {
                if (SafetyOfficer.isSafe(world, x, below, z)) {
                    safe.setY(below);
                    return safe;
                }
                below--;
            }

            if (above <= top) {
                if (SafetyOfficer.isSafe(world, x, above, z)) {
                    safe.setY(above);
                    return safe;
                }
                above++;
            }

        }

        return null;
    }

    /**
     * Check if the block itself and the block above it are safe containers while the block below it is also safe support
     *
     * @param world where coordinates exist
     * @param x X axis coordinate
     * @param y Y axis coordinate
     * @param z Z axis coordinate
     * @return true if coordinates are safe for a player to be moved to; false otherwise
     */
    private static boolean isSafe(final World world, final int x, final int y, final int z) {
        final int below = world.getBlockTypeIdAt(x, y - 1, z);
        return (SafetyOfficer.safeMaterials.contains(below)                                    //     Below is safe to be placed above
                || (!SafetyOfficer.safeContainers.contains(below)                              //  or Below is solid ...
                        && !SafetyOfficer.unsafeSupports.contains(below)))                     //                ... and not unsafe to be placed above
                && SafetyOfficer.safeContainers.contains(world.getBlockTypeIdAt(x, y, z))      // and Lower torso safe to occupy
                && SafetyOfficer.safeContainers.contains(world.getBlockTypeIdAt(x, y + 1, z)); // and Upper torso safe to occupy
    }

}
