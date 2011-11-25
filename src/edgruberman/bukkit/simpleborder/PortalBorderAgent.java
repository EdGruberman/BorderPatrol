package edgruberman.bukkit.simpleborder;

import java.util.Random;

import net.minecraft.server.MathHelper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.world.PortalCreateEvent;

/**
 * Replacement for PortalTravelAgent that ensures portals are only found/created within borders.
 */
final class PortalBorderAgent {
    
    private Random random = new Random();
    
    private int searchRadius = 128;
    private int creationRadius = 14; // 16 -> 14
    
    Location findPortal(Location location) {
        World world = location.getWorld();
        Border border = Border.defined.get(world);
        
        if (world.getEnvironment() == Environment.THE_END) {
            int i = MathHelper.floor(location.getBlockX());
            int j = MathHelper.floor(location.getBlockY()) - 1;
            int k = MathHelper.floor(location.getBlockZ());
            byte b0 = 1;
            byte b1 = 0;
            
            for (int l = -2; l <= 2; ++l) {
                for (int i1 = -2; i1 <= 2; ++i1) {
                    for (int j1 = -1; j1 < 3; ++j1) {
                        int k1 = i + i1 * b0 + l * b1;
                        int l1 = j + j1;
                        int i2 = k + i1 * b1 - l * b0;
                        boolean flag = j1 < 0;

                        if (world.getBlockTypeIdAt(k1, l1, i2) != (flag ? Material.OBSIDIAN.getId() : 0)) {
                            return null;
                        }
                    }
                }
            }
            
            return location;
        }
        
        double distanceSquaredClosest = -1.0D;
        int foundX = 0;
        int foundY = 0;
        int foundZ = 0;
        int originalX = location.getBlockX();
        int originalZ = location.getBlockZ();

        for (int x = originalX - this.searchRadius; x <= originalX + this.searchRadius; ++x) {
            double dX = (double) x + 0.5D - location.getX();

            for (int z = originalZ - this.searchRadius; z <= originalZ + this.searchRadius; ++z) {
                
                if (!border.isInside(x, z)) continue;
                
                double dZ = (double) z + 0.5D - location.getZ();

                for (int y = world.getMaxHeight() - 1; y >= 0; --y) {
                    // Using (world.getBlock(x, y, z).getType() == Material.PORTAL) leaked memory
                    if (world.getBlockTypeIdAt(x, y, z) != Material.PORTAL.getId()) continue;
                    
                    while (world.getBlockTypeIdAt(x, y - 1, z) == Material.PORTAL.getId()) --y;

                    double dY = (double) y + 0.5D - location.getY();
                    double distanceSquared = dX * dX + dY * dY + dZ * dZ;

                    if (distanceSquaredClosest >= 0.0D && distanceSquared >= distanceSquaredClosest) continue;
                    
                    distanceSquaredClosest = distanceSquared;
                    foundX = x;
                    foundY = y;
                    foundZ = z;
                }
            }
        }

        if (distanceSquaredClosest >= 0.0D) {
            // Return the middle of the lower two portal blocks
            double portalX = (double) foundX + 0.5D;
            double portalY = (double) foundY + 0.5D;
            double portalZ = (double) foundZ + 0.5D;
            
            if (world.getBlockAt(foundX - 1, foundY, foundZ).getType() == Material.PORTAL) {
                portalX -= 0.5D;
            }

            if (world.getBlockAt(foundX + 1, foundY, foundZ).getType() == Material.PORTAL) {
                portalX += 0.5D;
            }

            if (world.getBlockAt(foundX, foundY, foundZ - 1).getType() == Material.PORTAL) {
                portalZ -= 0.5D;
            }

            if (world.getBlockAt(foundX, foundY, foundZ + 1).getType() == Material.PORTAL) {
                portalZ += 0.5D;
            }

            return new Location(location.getWorld(), portalX, portalY, portalZ, location.getYaw(), location.getPitch());
        } else {
            return null;
        }
    }

    boolean createPortal(Location location) {
        World world = location.getWorld();
        Border border = Border.defined.get(world);
        net.minecraft.server.World nmsWorld = ((org.bukkit.craftbukkit.CraftWorld) world).getHandle();
        
        if (location.getWorld().getEnvironment() == Environment.THE_END) {
            int i = MathHelper.floor(location.getBlockX());
            int j = MathHelper.floor(location.getBlockY()) - 1;
            int k = MathHelper.floor(location.getBlockZ());
            byte b0 = 1;
            byte b1 = 0;
            
            for (int l = -2; l <= 2; ++l) {
                for (int i1 = -2; i1 <= 2; ++i1) {
                    for (int j1 = -1; j1 < 3; ++j1) {
                        int k1 = i + i1 * b0 + l * b1;
                        int l1 = j + j1;
                        int i2 = k + i1 * b1 - l * b0;
                        boolean flag = j1 < 0;
                        
                        world.getBlockAt(k1, l1, i2).setTypeId(flag ? Material.OBSIDIAN.getId() : 0);
                    }
                }
            }

            return true;
        }
        
        double d0 = -1.0D;
        int i = location.getBlockX();
        int j = location.getBlockY();
        int k = location.getBlockZ();
        int l = i;
        int i1 = j;
        int j1 = k;
        int k1 = 0;
        int l1 = this.random.nextInt(4);

        int i2;
        double d1;
        int j2;
        double d2;
        int k2;
        int l2;
        int i3;
        int j3;
        int k3;
        int l3;
        int i4;
        int j4;
        int k4;
        double d3;
        double d4;

        for (i2 = i - this.creationRadius; i2 <= i + this.creationRadius; ++i2) {
            d1 = (double) i2 + 0.5D - location.getX();

            for (j2 = k - this.creationRadius; j2 <= k + this.creationRadius; ++j2) {
                if (!border.isInside(i2, j2)) continue;
                
                d2 = (double) j2 + 0.5D - location.getZ();

                label271:
                for (l2 = world.getMaxHeight() - 1; l2 >= 0; --l2) {
                    if (!world.getBlockAt(i2, l2, j2).isEmpty()) continue;
                    
                    while (l2 > 0 && world.getBlockAt(i2, l2 - 1, j2).isEmpty()) {
                        --l2;
                    }

                    for (k2 = l1; k2 < l1 + 4; ++k2) {
                        j3 = k2 % 2;
                        i3 = 1 - j3;
                        if (k2 % 4 >= 2) {
                            j3 = -j3;
                            i3 = -i3;
                        }

                        for (l3 = 0; l3 < 3; ++l3) {
                            for (k3 = 0; k3 < 4; ++k3) {
                                for (j4 = -1; j4 < 5; ++j4) {
                                    i4 = i2 + (k3 - 1) * j3 + l3 * i3;
                                    k4 = l2 + j4;
                                    int l4 = j2 + (k3 - 1) * i3 - l3 * j3;

                                    if (j4 < 0 && !nmsWorld.getMaterial(i4, k4, l4).isBuildable() || j4 >= 0 && !world.getBlockAt(i4, k4, l4).isEmpty()) {
                                        continue label271;
                                    }
                                }
                            }
                        }

                        d3 = (double) l2 + 0.5D - location.getY();
                        d4 = d1 * d1 + d3 * d3 + d2 * d2;
                        if (d0 < 0.0D || d4 < d0) {
                            d0 = d4;
                            l = i2;
                            i1 = l2 + 1;
                            j1 = j2;
                            k1 = k2 % 4;
                        }
                    }
                }
            }
        }

        if (d0 < 0.0D) {
            for (i2 = i - this.creationRadius; i2 <= i + this.creationRadius; ++i2) {
                d1 = (double) i2 + 0.5D - location.getX();

                for (j2 = k - this.creationRadius; j2 <= k + this.creationRadius; ++j2) {
                    if (!border.isInside(i2, j2)) continue;
                    
                    d2 = (double) j2 + 0.5D - location.getZ();

                    label219:
                    for (l2 = world.getMaxHeight() - 1; l2 >= 0; --l2) {                        
                        if (!world.getBlockAt(i2, l2, j2).isEmpty()) continue;
                        
                        while (l2 > 0 && world.getBlockAt(i2, l2 - 1, j2).isEmpty()) {
                            --l2;
                        }

                        for (k2 = l1; k2 < l1 + 2; ++k2) {
                            j3 = k2 % 2;
                            i3 = 1 - j3;

                            for (l3 = 0; l3 < 4; ++l3) {
                                for (k3 = -1; k3 < 5; ++k3) {
                                    j4 = i2 + (l3 - 1) * j3;
                                    i4 = l2 + k3;
                                    k4 = j2 + (l3 - 1) * i3;
                                    if (k3 < 0 && !nmsWorld.getMaterial(j4, i4, k4).isBuildable() || k3 >= 0 && !world.getBlockAt(j4, i4, k4).isEmpty()) {
                                        continue label219;
                                    }
                                }
                            }

                            d3 = (double) l2 + 0.5D - location.getY();
                            d4 = d1 * d1 + d3 * d3 + d2 * d2;
                            if (d0 < 0.0D || d4 < d0) {
                                d0 = d4;
                                l = i2;
                                i1 = l2 + 1;
                                j1 = j2;
                                k1 = k2 % 2;
                            }
                        }
                    }
                }
            }
        }
        
        // Final check to ensure coordinates to be used are inside border.
        if (!border.isInside(l, j1)) return false;

        int i5 = l;
        int j5 = i1;

        j2 = j1;
        int k5 = k1 % 2;
        int l5 = 1 - k5;

        if (k1 % 4 >= 2) {
            k5 = -k5;
            l5 = -l5;
        }

        boolean flag;

        // CraftBukkit start - portal create event
        java.util.ArrayList<org.bukkit.block.Block> blocks = new java.util.ArrayList<org.bukkit.block.Block>();
        // Find out what blocks the portal is going to modify, duplicated from below

        if (d0 < 0.0D) {
            if (i1 < 70) {
                i1 = 70;
            }

            if (i1 > world.getMaxHeight() - 10) {
                i1 = world.getMaxHeight() - 10;
            }

            j5 = i1;

            for (l2 = -1; l2 <= 1; ++l2) {
                for (k2 = 1; k2 < 3; ++k2) {
                    for (j3 = -1; j3 < 3; ++j3) {
                        i3 = i5 + (k2 - 1) * k5 + l2 * l5;
                        l3 = j5 + j3;
                        k3 = j2 + (k2 - 1) * l5 - l2 * k5;
                        Block b = world.getBlockAt(i3, l3, k3);
                        if (!blocks.contains(b)) {
                            blocks.add(b);
                        }
                    }
                }
            }
        }

        for (l2 = 0; l2 < 4; ++l2) {
            for (k2 = 0; k2 < 4; ++k2) {
                for (j3 = -1; j3 < 4; ++j3) {
                    i3 = i5 + (k2 - 1) * k5;
                    l3 = j5 + j3;
                    k3 = j2 + (k2 - 1) * l5;
                    Block b = world.getBlockAt(i3, l3, k3);
                    if (!blocks.contains(b)) {
                        blocks.add(b);
                    }
                }
            }
        }

        PortalCreateEvent event = new PortalCreateEvent(blocks, world);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        // CraftBukkit end

        // Build portal
        if (d0 < 0.0D) {
            if (i1 < 70) {
                i1 = 70;
            }

            if (i1 > world.getMaxHeight() - 10) {
                i1 = world.getMaxHeight() - 10;
            }

            j5 = i1;

            for (l2 = -1; l2 <= 1; ++l2) {
                for (k2 = 1; k2 < 3; ++k2) {
                    for (j3 = -1; j3 < 3; ++j3) {
                        i3 = i5 + (k2 - 1) * k5 + l2 * l5;
                        l3 = j5 + j3;
                        k3 = j2 + (k2 - 1) * l5 - l2 * k5;
                        flag = j3 < 0;
                        world.getBlockAt(i3, l3, k3).setTypeId((flag ? Material.OBSIDIAN.getId() : 0));
                    }
                }
            }
        }

        for (l2 = 0; l2 < 4; ++l2) {

            // Do not apply physics.
            for (k2 = 0; k2 < 4; ++k2) {
                for (j3 = -1; j3 < 4; ++j3) {
                    i3 = i5 + (k2 - 1) * k5;
                    l3 = j5 + j3;
                    k3 = j2 + (k2 - 1) * l5;
                    flag = k2 == 0 || k2 == 3 || j3 == -1 || j3 == 3;
                    world.getBlockAt(i3, l3, k3).setTypeId((flag ? Material.OBSIDIAN.getId() : Material.PORTAL.getId()), false);
                }
            }

            // Now apply physics.
            for (k2 = 0; k2 < 4; ++k2) {
                for (j3 = -1; j3 < 4; ++j3) {
                    i3 = i5 + (k2 - 1) * k5;
                    l3 = j5 + j3;
                    k3 = j2 + (k2 - 1) * l5;
                    world.getBlockAt(i3, l3, k3).setTypeId(world.getBlockTypeIdAt(i3, l3, k3));
                }
            }
        }

        return true;
    }
    
    public PortalBorderAgent setSearchRadius(int radius) {
        this.searchRadius = radius;
        return this;
    }

    public int getSearchRadius() {
        return this.searchRadius;
    }

    public PortalBorderAgent setCreationRadius(int radius) {
        this.creationRadius = radius < 2 ? 0 : radius - 2;
        return this;
    }

    public int getCreationRadius() {
        return this.creationRadius;
    }
}