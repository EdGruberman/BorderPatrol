package edgruberman.bukkit.simpleborder;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Centralized and standardized logging and player communication class.
 */
public class Communicator {
    
    // Applied to all messages sent directly to a player.
    private static final String MESSAGE_PREFIX = "-> ";
    
    private Main main;
    
    public Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
    private Level messageLevel = Level.INFO;
    
    public Communicator(Main main) {
        this.main = main;
    }
    
    /**
     * Configures logging to display more or less than the default of INFO.</br>
     * <b>Known Bug:</b> Logging output to file in Minecraft does not include level prefix despite it displaying in the console.
     * 
     * @param level Minimum logging level to show.
     */
    public void setLogLevel(Level level) {
        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (Handler h : this.logger.getParent().getHandlers()) {
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);
        }
        this.logger.setLevel(level);
    }
    
    /**
     * Determines if current logging level will display log entries of the specified level or higher.
     * 
     * @param level = Logging level to determine if it will be displayed in the log or not.
     * @return = true if current logging level will display this level; false otherwise.
     */
    public Boolean isLogLevel(Level level) {
        if (this.logger.getLevel().intValue() > level.intValue()) { return false; }
        return true;
    }
    
    /**
     * Generate a normal information log entry.
     * 
     * @param message = Text to display in log entry. Time and level will be prefixed automatically by Minecraft.
     */
    public void log(String message) {
        this.log(Level.INFO, message, null);
    }
    
    /**
     * Generate a log entry of the specified level. Useful for warnings, errors, and debug entries.
     * 
     * @param level = Logging level of log entry. Standard Java logging levels used.
     * @param message = Text to display in log entry. Time and level will be prefixed automatically by Minecraft.
     */
    public void log(Level level, String message) {
        this.log(level, message, null);
    }
    
    /**
     * Generate a log entry that has an associated error to display at the same time.
     * 
     * @param level = Logging level of log entry. Standard Java logging levels used.
     * @param message = Text to display in log entry. Time and level will be prefixed automatically by Minecraft.
     * @param e = Related error message to output along with log entry.
     */
    public void log(Level level, String message, Throwable e) {
        if (e != null) message = message.replaceAll("\n", "   ");
        for (String messageLine : message.split("\n")) {
            this.logger.log(level, "[" + this.main.getDescription().getName() + "] " + messageLine, e);
        }
    }
    
    /**
     * Messages to players will only be displayed if equal to or higher than the defined level.
     * Useful for removing player messages if feedback is not needed.
     * 
     * @param level = Minimum level of messages to forward to player.
     */
    public void setMessageLevel(Level level) {
        this.messageLevel = level;
    }
    
    /**
     * Forward a normal information message to the player's client interface similar to chat messages.
     * 
     * @param player = Player to target message to.
     * @param message = Text to display on player's client interface.
     */
    public void sendMessage(Player player, String message) {
        this.sendMessage(player, message, MessageLevel.INFO);
    }
    
    public void sendMessage(String playerName, String message, MessageLevel level) {
        this.sendMessage(this.main.getServer().getPlayer(playerName), message, level);
    }
 
    /**
     * Forward a message to the player's client interface similar to chat messages.
     * 
     * @param player = Player to target message to.
     * @param message = Text to display on player's client interface.
     * @param level = Importance level of message. Custom enum to standardize coloring for common message types.
     */
    public void sendMessage(Player player, String message, MessageLevel level) {
        // Disable user messages according to configuration.
        if (level.level.intValue() < this.messageLevel.intValue()) { return; }
        
        // Shift color to dark for private messages.
        ChatColor color = ChatColor.getByCode(level.color.getCode() - 8);
        
        for (String messageLine : message.split("\n")) {
            // Indicate targeted message (not broadcasted).
            messageLine = MESSAGE_PREFIX + messageLine;
            
            this.log(level.level, "[Message->" + player.getName() + "] " + messageLine);
            this.sendMessage(player, messageLine, color);
        }
    }
    
    /**
     * Forward a message to the player's client interface similar to chat messages.
     * 
     * @param player = Player to target message to.
     * @param message = Text to display on player's client interface.
     * @param color = Color to display the message on the client interface as.
     * 
     * @deprecated Use <b>sendMessage(Player player, String message, MessageLevel level)</b> whenever possible instead to leverage the standardized level enum.
     */
    public void sendMessage(Player player, String message, ChatColor color) {
            player.sendMessage("�" + color.getCode() + message);
    }
    
    /**
     * Forward a normal information message to the all players' client interface similar to chat messages.
     * 
     * @param message = Text to display players' client interface.
     */
    public void broadcastMessage(String message) {
       this.broadcastMessage(MessageLevel.INFO, message);
    }
    
    /**
     * Forward a message to the all players' client interface similar to chat messages.
     * 
     * @param message = Text to display on player's client interface.
     * @param level = Importance level of message. Custom enum to standardize coloring for common message types.
     */
    public void broadcastMessage(MessageLevel level, String message) {
        // Disable user messages according to configuration.
        if (level.level.intValue() < this.messageLevel.intValue()) { return; }
        
        this.log(level.level, "[Broadcast] " + message);
        this.broadcastMessage(ChatColor.getByCode(level.color.getCode()), message);
    }
    
    /**
     * Forward a message to all players' client interface similar to chat messages.
     * 
     * @param message = Text to display on player's client interface.
     * @param color = Color to display the message on the client interface as.
     * 
     * @deprecated Use <b>broadcastMessage(String message, MessageLevel level)</b> whenever possible instead to leverage the standardized level enum.
     */
    public void broadcastMessage(ChatColor color, String message) {
        for (String messageLine : message.split("\n")) {
            this.main.getServer().broadcastMessage("�" + color.getCode() + messageLine);
        }
    }
    
    /**
     * Standardization for coloring of common messages.
     */
    public enum MessageLevel {
          SEVERE  (ChatColor.RED         , Level.SEVERE)        // Corrective actions
        , WARNING (ChatColor.YELLOW      , Level.WARNING)       // Impending actions
        , NOTICE  (ChatColor.LIGHT_PURPLE, Level.parse("850"))  // Instructions, requirements
        , INFO    (ChatColor.WHITE       , Level.INFO)          // Standard data
        , STATUS  (ChatColor.GREEN       , Level.parse("775"))  // Directly related to player's actions
        , EVENT   (ChatColor.GRAY        , Level.parse("750"))  // External to player's actions
        , CONFIG  (ChatColor.BLUE        , Level.CONFIG)        // Current settings
        ;

        public ChatColor color;
        public Level level;
        
        private MessageLevel (ChatColor color, Level level) {
            this.color = color;
            this.level = level;
        }
    }
}