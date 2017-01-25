package net.elseland.xikage.MythicMobsKillObjective;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import me.blackvein.quests.CustomObjective;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import net.elseland.xikage.MythicMobs.API.Bukkit.Events.MythicMobDeathEvent;
import net.elseland.xikage.MythicMobs.MythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MythicMobKillObjective extends CustomObjective implements Listener {
    private ArrayList<UUID> registeredMobs = new ArrayList();
    private Quests q;

    public MythicMobKillObjective() {
        this.setName("Kill MythicMobs");
        this.setAuthor("Xikage");
        this.setEnableCount(true);
        this.setShowCount(true);
        this.addData("Objective Name");
        this.addDescription("Objective Name", "Enter an overall name for this objective (i.e. Minions of the Skeleton King)");
        this.addData("Killable Types");
        this.addDescription("Killable Types", "Enter a comma-separated list of MythicMobs the player can kill for this objective (i.e. Name1,Name2,Name3). This can also be a regex (for advanced users):");
        this.setCountPrompt("Enter the amount of the mob(s) that the player must kill:");
        this.setDisplay("Kill %Objective Name%: %count%");
        this.q = (Quests)Bukkit.getPluginManager().getPlugin("Quests");
    }

    @EventHandler(
            priority = EventPriority.NORMAL
    )
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        MythicMobs.debug(2, "MythicMobs-Quests event fired!");
        if(this.registeredMobs.contains(event.getEntity().getUniqueId())) {
            MythicMobs.debug(2, "MythicMobs-Quests event is a duplicate! Ignoring.");
        } else {
            Player killer = null;
            if(!(event.getKiller() instanceof Player)) {
                MythicMobs.debug(2, "MythicMobs-Quests event fired by non-player! Ignoring.");
            } else {
                killer = (Player)event.getKiller();
                Quester quester = this.q.getQuester(killer.getUniqueId());
                if(!quester.currentQuests.isEmpty()) {
                    Iterator questIterator = quester.currentQuests.keySet().iterator();

                    while(questIterator.hasNext()) {
                        Quest quest = (Quest)questIterator.next();
                        Map map = getDatamap(killer, this, quest);
                        if (map == null) continue;

                        String string = (String)map.get("Killable Types");
                        String[] split = string.split(",");
                        String[] var12 = split;
                        int var11 = split.length;

                        for(int i = 0; i < var11; ++i) {
                            String s = var12[i];

                            if(event.getMobType().getInternalName().matches(s)) {
                                incrementObjective(killer, this, 1, quest);
                                this.registeredMobs.add(event.getEntity().getUniqueId());
                                class Clean implements Runnable {
                                    UUID u;

                                    Clean(UUID u) {
                                        this.u = u;
                                    }

                                    public void run() {
                                        MythicMobKillObjective.this.registeredMobs.remove(this.u);
                                    }
                                }

                                Bukkit.getScheduler().scheduleSyncDelayedTask(MythicMobs.plugin, new Clean(event.getEntity().getUniqueId()), 40L);
                                MythicMobs.debug(2, "MythicMobs-Quests kill objective incremented.");
                            }
                        }
                    }

                }
            }
        }
    }
}
