package edgruberman.bukkit.borderpatrol.craftbukkit;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.World;

public abstract class CraftBukkit {

    public static CraftBukkit create() throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Class<?> provider = Class.forName(CraftBukkit.class.getPackage().getName() + "." + CraftBukkit.class.getSimpleName() + "_" + CraftBukkit.version());
        return (CraftBukkit) provider.getConstructor().newInstance();
    }

    private static String version() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        if (version.equals("craftbukkit")) version = "pre";
        return version;
    }

    public abstract boolean getForceChunkLoad(World world);

    public abstract void setForceChunkLoad(World world, boolean force);

    public abstract boolean isBuildable(World world, int x, int y, int z);

}
