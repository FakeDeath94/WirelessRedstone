package net.licks92.wirelessredstone;

import net.licks92.wirelessredstone.commands.Admin.AdminCommandManager;
import net.licks92.wirelessredstone.commands.CommandManager;
import net.licks92.wirelessredstone.compat.InternalProvider;
import net.licks92.wirelessredstone.compat.InternalWorldEditHooker;
import net.licks92.wirelessredstone.listeners.BlockListener;
import net.licks92.wirelessredstone.listeners.PlayerListener;
import net.licks92.wirelessredstone.listeners.WorldListener;
import net.licks92.wirelessredstone.materiallib.MaterialLib;
import net.licks92.wirelessredstone.storage.StorageConfiguration;
import net.licks92.wirelessredstone.storage.StorageManager;
import net.licks92.wirelessredstone.string.StringManager;
import net.licks92.wirelessredstone.string.Strings;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WirelessRedstone extends JavaPlugin {

    public static final String CHANNEL_FOLDER = "channels";

    private static WirelessRedstone instance;
    private static WRLogger WRLogger;
    private static StringManager stringManager;
    private static StorageManager storageManager;
    private static SignManager signManager;
    private static CommandManager commandManager;
    private static AdminCommandManager adminCommandManager;

    private ConfigManager config;
    private InternalWorldEditHooker worldEditHooker;
    private boolean storageLoaded = false;


    public static WirelessRedstone getInstance() {
        return instance;
    }

    public static WRLogger getWRLogger() {
        return WRLogger;
    }

    public static StringManager getStringManager() {
        return stringManager;
    }

    public static Strings getStrings() {
        return getStringManager().getStrings();
    }

    public static StorageManager getStorageManager() {
        return storageManager;
    }

    public static StorageConfiguration getStorage() {
        return getStorageManager().getStorage();
    }

    public static SignManager getSignManager() {
        return signManager;
    }

    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static AdminCommandManager getAdminCommandManager() {
        return adminCommandManager;
    }

    public InternalWorldEditHooker getWorldEditHooker() {
        return worldEditHooker;
    }

    public void setWorldEditHooker(InternalWorldEditHooker worldEditHooker) {
        this.worldEditHooker = worldEditHooker;
    }

    @Override
    public void onEnable() {
        instance = this;
        config = ConfigManager.getConfig();
        config.update(CHANNEL_FOLDER);
        WRLogger = new WRLogger("[WirelessRedstone]", getServer().getConsoleSender(), config.getDebugMode(), config.getColorLogging());

        new MaterialLib(this).initialize();

        stringManager = new StringManager(config.getLanguage());

        storageManager = new StorageManager(config.getStorageType(), CHANNEL_FOLDER);

        if (!storageManager.getStorage().initStorage()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        storageLoaded = true;

        signManager = new SignManager();
        commandManager = new CommandManager();
        adminCommandManager = new AdminCommandManager();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new PlayerListener(), this);

        getCommand("wirelessredstone").setExecutor(commandManager);
        getCommand("wr").setExecutor(commandManager);
        getCommand("wredstone").setExecutor(commandManager);
        getCommand("wifi").setExecutor(commandManager);

        getCommand("wirelessredstone").setTabCompleter(commandManager);
        getCommand("wr").setTabCompleter(commandManager);
        getCommand("wredstone").setTabCompleter(commandManager);
        getCommand("wifi").setTabCompleter(commandManager);

        getCommand("wradmin").setExecutor(adminCommandManager);
        getCommand("wra").setExecutor(adminCommandManager);

        getCommand("wradmin").setTabCompleter(adminCommandManager);
        getCommand("wra").setTabCompleter(adminCommandManager);

        if (pm.isPluginEnabled("WorldEdit")) {
            InternalProvider.getCompatWorldEditHooker().register();
            WirelessRedstone.getWRLogger().debug("Hooked into WorldEdit");
        }
    }

    @Override
    public void onDisable() {
        if (storageLoaded) {
            getStorage().close();
        }

        if (worldEditHooker != null) {
            worldEditHooker.unRegister();
        }

        storageLoaded = false;
        adminCommandManager = null;
        commandManager = null;
        signManager = null;
        storageManager = null;
        stringManager = null;
        config = null;
        WRLogger = null;
        instance = null;
    }

    /**
     * Re-initialize strings. This can be used to switch languages after a config change.
     * <p>
     * Removes reference to stringManager and place a new reference.
     */
    public void resetStrings() {
        stringManager = null;
        stringManager = new StringManager(config.getLanguage());
    }
}
