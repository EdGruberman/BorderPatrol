package edgruberman.bukkit.simpleborder;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import edgruberman.bukkit.simpleborder.MessageManager.MessageLevel;

public class Configuration {
    
    // Name of configuration file. (Used for both default supplied in JAR and the active one in the file system.)
    private static final String CONFIGURATION_FILE = "config.yml";
    
    // Path to default configuration file supplied in JAR.
    private static final String DEFAULT_CONFIGURATION_PATH = "/defaults/" + CONFIGURATION_FILE;
    
    public static void load(Main main) {
        File dataFolder = main.getDataFolder();
        
        // Use default configuration file supplied in JAR if active file system configuration file does not already exist.
        File fileConfig = new File(dataFolder, CONFIGURATION_FILE);
        if (!fileConfig.exists()) {
            java.net.URL defaultConfig = main.getClass().getResource(DEFAULT_CONFIGURATION_PATH);
            byte[] buf = new byte[1024];
            int len;
            try {
                dataFolder.mkdir();
                OutputStream out = new FileOutputStream(fileConfig);
                InputStream in = defaultConfig.openStream();
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (java.io.IOException e) {
                Main.messageManager.log(MessageLevel.SEVERE, "Unable to create default configuration file.", e);
            }
        }
        
        main.getConfiguration().load();
    }
    
}