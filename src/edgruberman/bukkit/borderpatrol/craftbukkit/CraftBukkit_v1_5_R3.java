package edgruberman.bukkit.borderpatrol.craftbukkit;

import net.minecraft.server.v1_5_R3.WorldServer;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;

public class CraftBukkit_v1_5_R3 extends CraftBukkit {

    @Override
    public boolean getForceChunkLoad(final World world) {
        final WorldServer worldServer = ((CraftWorld) world).getHandle();
        return worldServer.chunkProviderServer.forceChunkLoad;
    }

    @Override
    public void setForceChunkLoad(final World world, final boolean force) {
        final WorldServer worldServer = ((CraftWorld) world).getHandle();
        worldServer.chunkProviderServer.forceChunkLoad = force;
    }

    @Override
    public boolean isBuildable(final World world, final int x, final int y, final int z) {
        final net.minecraft.server.v1_5_R3.World nmsWorld = ((CraftWorld) world).getHandle();
        return nmsWorld.getMaterial(x, y, z).isBuildable();
    }

}
