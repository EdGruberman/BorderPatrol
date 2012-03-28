package edgruberman.bukkit.borderpatrol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;

/**
 * Border identification.
 */
public class CivilEngineer {

    private final Map<World, Border> borders = new HashMap<World, Border>();

    // TODO duplicate border detection/logging
    CivilEngineer(final List<Border> borders) {
        for(final Border border : borders) this.borders.put(border.getWorld(), border);
    }

    Border getBorder(final World world) {
        return this.borders.get(world);
    }

}
