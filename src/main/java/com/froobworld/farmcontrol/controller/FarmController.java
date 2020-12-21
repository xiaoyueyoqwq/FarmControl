package com.froobworld.farmcontrol.controller;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.controller.task.*;
import com.froobworld.farmcontrol.controller.trigger.Trigger;
import com.froobworld.farmcontrol.utils.Actioner;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FarmController {
    private final FarmControl farmControl;
    private final Map<World, Map<Trigger, Set<ActionProfile>>> worldTriggerProfilesMap = new HashMap<>();
    private Integer triggerTaskId = null;
    private TriggerCheckTask triggerCheckTask = null;
    private boolean registered;

    public FarmController(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    public void load() {
        for (World world : Bukkit.getWorlds()) {
            Map<Trigger, Set<ActionProfile>> triggerProfileMap = worldTriggerProfilesMap.computeIfAbsent(world, w -> new HashMap<>());
            Trigger proactiveTrigger = farmControl.getTriggerManager().getTrigger("proactive");
            for (String profileName : farmControl.getFcConfig().worldSettings.of(world).profiles.get()) {
                triggerProfileMap.computeIfAbsent(proactiveTrigger, trigger -> new HashSet<>()).add(farmControl.getProfileManager().getActionProfile(proactiveTrigger, profileName));
            }
        }
    }

    public void reload() {
        worldTriggerProfilesMap.clear();
        load();
    }

    public void register() {
        if (triggerTaskId != null) {
            throw new IllegalStateException("Already registered");
        }
        triggerCheckTask = new TriggerCheckTask(farmControl, this, worldTriggerProfilesMap);
        long cyclePeriod = farmControl.getFcConfig().cyclePeriod.get();
        registered = true;
        triggerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(farmControl, triggerCheckTask, cyclePeriod, cyclePeriod);
    }

    public void unRegister() {
        registered = false;
        Bukkit.getScheduler().cancelTask(triggerTaskId);
        triggerTaskId = null;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getLivingEntities()) {
                Actioner.undoAllActions(entity, farmControl);
            }
        }
        triggerCheckTask.stop();
        triggerCheckTask = null;
    }

    public void submitActionPerformTask(ActionPerformTask actionPerformTask) {
        if (registered) {
            Bukkit.getScheduler().runTask(farmControl, actionPerformTask);
        }
    }

    public void submitUntriggerPerformTask(UntriggerPerformTask untriggerPerformTask) {
        if (registered) {
            Bukkit.getScheduler().runTask(farmControl, untriggerPerformTask);
        }
    }

}
