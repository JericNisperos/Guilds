package me.glaremasters.guilds;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.commands.*;
import me.glaremasters.guilds.commands.base.CommandHandler;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.databases.json.JSON;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.listeners.GuildPerks;
import me.glaremasters.guilds.listeners.Players;
import me.glaremasters.guilds.updater.SpigotUpdater;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.stream.Stream;

public final class Guilds extends JavaPlugin {

    private static Guilds guilds;
    private DatabaseProvider database;
    private GuildHandler guildHandler;
    private CommandHandler commandHandler;
    private static TaskChainFactory taskChainFactory;
    private File language;
    public YamlConfiguration languageConfig;
    private GuildsAPI api;

    @Override
    public void onEnable() {
        guilds = this;
        api = new GuildsAPI();
        setupEconomy();
        setupPermissions();
        initData();
        saveData();

        taskChainFactory = BukkitTaskChainFactory.create(this);

        database = new JSON(this);
        database.initialize();

        guildHandler = new GuildHandler();
        guildHandler.enable();

        commandHandler = new CommandHandler();
        commandHandler.enable();

        SpigotUpdater updater = new SpigotUpdater(this, 48920);
        updateCheck(updater);

        getCommand("guild").setExecutor(commandHandler);

        Stream.of(new CommandCreate(guilds), new CommandPrefix(guilds), new CommandStatus(guilds),
                new CommandBank(guilds), new CommandSetHome(guilds), new CommandHome(guilds),
                new CommandVersion(guilds), new CommandRename(guilds), new CommandBoot(guilds),
                new CommandCancel(guilds), new CommandConfirm(guilds), new CommandDecline(guilds),
                new CommandDelete(guilds), new CommandTransfer(guilds), new CommandHelp(guilds),
                new CommandUpgrade(guilds), new CommandReload(guilds)).forEach(commandHandler::register);
        Stream.of(new GuildPerks(), new Players(this)).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
    }

    @Override
    public void onDisable() {
        guildHandler.disable();
        commandHandler.disable();
    }

    /**
     * Grabs an instance of the plugin
     * @return instance of plugin
     */
    public static Guilds getGuilds() {
        return guilds;
    }

    /**
     * Implement Vault's Economy API
     * @return the value of the method
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        Economy econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Implement Vault's Permission API
     * @return the value of the method
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        Permission perms = rsp.getProvider();
        return perms != null;
    }

    /**
     * Initiate plugin data
     */
    private void initData() {
        saveDefaultConfig();
        File languageFolder = new File(getDataFolder(), "languages");
        if (!languageFolder.exists()) languageFolder.mkdirs();
        this.language = new File(languageFolder, getConfig().getString("lang") + ".yml");
        this.languageConfig = YamlConfiguration.loadConfiguration(language);
    }

    /**
     * Save and handle new files if needed
     */
    private void saveData() {
        if (!this.language.exists()) Stream.of("english").forEach(l -> this.saveResource("languages/" + l + ".yml", false));
    }

    /**
     * Get the database we are using to store data
     * @return the database currently being used
     */
    public DatabaseProvider getDatabase() {
        return database;
    }

    /**
     * Get the guild handler in the plugin
     * @return the guild handler being used
     */
    public GuildHandler getGuildHandler() {
        return guildHandler;
    }

    /**
     * Get the command handler in the plugin
     * @return the command handler being used
     */
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    /**
     * Create a new chain for async
     * @param <T> taskchain
     * @return the new chain created for data modification
     */
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    /**
     * Execute the update checker
     * @param updater the SpigotUpdater
     */
    private void updateCheck(SpigotUpdater updater) {
        try {
            if (updater.checkForUpdates()) {
                getLogger().info("You appear to be running a version other than our latest stable release." + " You can download our newest version at: " + updater.getResourceURL());
            }
        } catch (Exception ex) {
            getLogger().info("Could not check for updates! Stacktrace:");
            ex.printStackTrace();
        }
    }

    /**
     * Get a holder of the API
     * @return API holder
     */
    public GuildsAPI getApi() {
        return api;
    }
}
