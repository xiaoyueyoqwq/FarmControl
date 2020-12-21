package com.froobworld.farmcontrol;

import com.froobworld.farmcontrol.command.FarmControlCommand;
import com.froobworld.farmcontrol.config.FcConfig;
import com.froobworld.farmcontrol.controller.*;
import com.froobworld.farmcontrol.listener.CompatibilityListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class FarmControl extends JavaPlugin {
    private FcConfig fcConfig;
    private ActionManager actionManager;
    private TriggerManager triggerManager;
    private ProfileManager profileManager;
    private ExclusionManager exclusionManager;
    private FarmController farmController;

    public void onEnable() {
        this.fcConfig = new FcConfig(this);
        try {
            fcConfig.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        actionManager = new ActionManager();
        actionManager.addDefaults(this);
        triggerManager = new TriggerManager();
        triggerManager.addDefaults(this);
        profileManager = new ProfileManager(this);
        try {
            profileManager.load();
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        exclusionManager = new ExclusionManager(this);
        farmController = new FarmController(this);
        farmController.load();
        farmController.register();
        Bukkit.getPluginManager().registerEvents(new CompatibilityListener(this), this);

        registerCommands();

        new Metrics(this, 9692);
    }

    public void reload() {
        try {
            fcConfig.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        farmController.unRegister();
        try {
            profileManager.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
        farmController.reload();
        farmController.register();

    }

    public void onDisable() {
        farmController.unRegister();
    }

    public FcConfig getFcConfig() {
        return fcConfig;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public TriggerManager getTriggerManager() {
        return triggerManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public ExclusionManager getExclusionManager() {
        return exclusionManager;
    }

    public void registerCommands() {
        FarmControlCommand farmControlCommand = new FarmControlCommand(this);
        getCommand("farmcontrol").setExecutor(farmControlCommand);
        getCommand("farmcontrol").setTabCompleter(farmControlCommand.getTabCompleter());
        getCommand("farmcontrol").setPermission("farmcontrol.command.farmcontrol");
        getCommand("farmcontrol").setPermissionMessage(FarmControlCommand.NO_PERMISSION_MESSAGE);
    }

}
