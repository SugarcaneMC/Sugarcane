package org.sugarcanemc.sugarcane.command;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.sun.management.OperatingSystemMXBean;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.scheduler.MinecraftInternalPlugin;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.sugarcanemc.sugarcane.config.SugarcaneConfig;
import org.sugarcanemc.sugarcane.util.Util;

import java.lang.management.ManagementFactory;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

public class StatsCommand extends Command {
    private final static int ConsoleBarWidth = 78; //keeping the 80 column standard in mind

    byte cpusamples = 100;
    DecimalFormat df = new DecimalFormat("00.00");
    BossBar memoryBar = Bukkit.createBossBar("Memory...", BarColor.BLUE, BarStyle.SEGMENTED_20);
    BossBar tpsBar = Bukkit.createBossBar("TPS...", BarColor.PINK, BarStyle.SEGMENTED_20);
    BossBar entityBar = Bukkit.createBossBar("Entities...", BarColor.PINK, BarStyle.SEGMENTED_20);
    BossBar cpuBar = Bukkit.createBossBar("CPU...", BarColor.GREEN, BarStyle.SEGMENTED_20);
    OperatingSystemMXBean systemInfo = ManagementFactory.getPlatformMXBean(
            OperatingSystemMXBean.class);
    Double current_memory, current_tps;
    int entities;
    Long MemUsed, MemMax, MemAlloc;
    int items = 0, players = 0, minecarts = 0, peaceful = 0, hostile = 0, others = 0;
    ArrayList<Double> process_cpu = new ArrayList<>(), system_cpu = new ArrayList<>();
    ConcurrentMap<String, String> entTypes = new ConcurrentHashMap<>();
    public StatsCommand(String name) {
        super(name);
        this.description = "Toggle stats bars";
        this.usageMessage = "/stats";
        this.setPermission("sugarcane.command.stats");

        df.setRoundingMode(RoundingMode.CEILING);
        MemMax = Runtime.getRuntime().maxMemory() / 1024;

        entTypes.put("Unknown", "");
        entTypes.put("Hostile", "");
        entTypes.put("Minecart", "");
        entTypes.put("Peaceful", "");
        entTypes.put("Items", "");
        entTypes.put("Other", "");

        new BukkitRunnable() {
            @Override
            public void run() {
                if(Bukkit.isStopping() || MinecraftServer.getServer().hasStopped()) cancel();
                if (Bukkit.getOnlinePlayers().size() == 0 || entityBar.getPlayers().size() == 0) return;
                updateCpuBar();
            }
        }.runTaskTimerAsynchronously(new MinecraftInternalPlugin(), 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(Bukkit.isStopping() || MinecraftServer.getServer().hasStopped()) cancel();
                if (Bukkit.getOnlinePlayers().size() == 0 || entityBar.getPlayers().size() == 0) return;
                updateTpsMemoryBars();
            }
            public void stop() {
                this.cancel();
            }
        }.runTaskTimer(new MinecraftInternalPlugin(), 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(Bukkit.isStopping() || MinecraftServer.getServer().hasStopped()) cancel();
                if (Bukkit.getOnlinePlayers().size() == 0 || entityBar.getPlayers().size() == 0) return;
                updateEntityBar();
            }
        }.runTaskTimer(new MinecraftInternalPlugin(), 0L, 1L);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if(sender instanceof ConsoleCommandSender) {
            for(BossBar bb : new BossBar[]{cpuBar, memoryBar, tpsBar, entityBar}){
                updateCpuBar();
                updateTpsMemoryBars();
                updateEntityBar();
                sender.sendMessage(bb.getTitle()+"\n"+Util.getTextProgressBar(bb.getProgress()));
            }
            return true;
        }
        Player p = Bukkit.getPlayer(sender.getName());
        if (args.length == 0) {
            if (memoryBar.getPlayers().contains(p)) {
                cpuBar.removePlayer(p);
                memoryBar.removePlayer(p);
                tpsBar.removePlayer(p);
                entityBar.removePlayer(p);
            } else {
                cpuBar.addPlayer(p);
                memoryBar.addPlayer(p);
                tpsBar.addPlayer(p);
                entityBar.addPlayer(p);
                Util.logDebug("Added player " + p.getName());
            }
            return true;
        }
        return false;
    }

    private double calculateAverage(List<Double> list) {
        List<Double> _list = new ArrayList<>(list);
        return _list.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private void updateCpuBar(){
        double _proc = systemInfo.getProcessCpuLoad();
        double _sys = systemInfo.getSystemCpuLoad();
        if(_proc != -1) process_cpu.add(_proc* 100);
        if(_sys != -1) system_cpu.add(_sys * 100);
        if (process_cpu.size() > cpusamples) {
            process_cpu.remove(0);
        }
        if (system_cpu.size() > cpusamples) {
            system_cpu.remove(0);
        }
        double _proccpu = calculateAverage(process_cpu);
        double _syscpu = calculateAverage(system_cpu);
        cpuBar.setTitle(ChatColor.GREEN + "ProcCPU: " + df.format(_proccpu) + "% | SysCPU: " + df.format(_syscpu) + "%");

        if (_syscpu >= 90) cpuBar.setColor(BarColor.RED);
        else if (_syscpu >= 75) cpuBar.setColor(BarColor.YELLOW);
        else cpuBar.setColor(BarColor.GREEN);
        double cpuAverage = _syscpu / 100;
        if(cpuAverage < 0) cpuAverage = 0;
        else if(cpuAverage > 1) cpuAverage = 1;
        cpuBar.setProgress(cpuAverage);
    }
    private void updateTpsMemoryBars(){
        MemUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        MemAlloc = Runtime.getRuntime().totalMemory() / 1048576;
        current_memory = (MemUsed.doubleValue() / MemMax.doubleValue()) / 1024;
        current_tps = Arrays.stream(Bukkit.getTPS()).average().orElse(-1);
        memoryBar.setProgress(current_memory);
        memoryBar.setTitle(ChatColor.BLUE + "Mem: " + (MemUsed / 1048576) + " MB/" + MemMax / 1024 + " MB (" + MemAlloc + " MB alloc)");
        tpsBar.setTitle(ChatColor.LIGHT_PURPLE + "TPS: " + df.format(current_tps) + " (" + df.format(Bukkit.getAverageTickTime()) + " MSPT -> "+df.format(1000/Bukkit.getAverageTickTime())+" TPS)");
        tpsBar.setColor(BarColor.PINK);
        if (current_tps > 21) tpsBar.setColor(BarColor.YELLOW);
        if (current_tps > 20) {
            current_tps = 20D;
            tpsBar.setColor(BarColor.PURPLE);
        } else if (current_tps < 18) {
            tpsBar.setColor(BarColor.YELLOW);
            if (current_tps < 15)
                tpsBar.setColor(BarColor.RED);
        }
        tpsBar.setProgress(current_tps / 20);
    }
    private void updateEntityBar(){
        items = players = minecarts = peaceful = hostile = others = 0;
        for (World w : Bukkit.getServer().getWorlds())
            for (Entity e : w.getEntities()) {
                if (e instanceof Player) {
                    players++;
                } else if (e instanceof Monster || e instanceof Slime
                        || e instanceof Boss || e instanceof Ghast || e instanceof Shulker
                        || e instanceof Phantom) {
                    hostile++;
                } else if (e instanceof Minecart || e instanceof Boat) {
                    minecarts++;
                } else if (e instanceof Animals || e instanceof Golem || e instanceof Ambient
                        || e instanceof WaterMob) {
                    peaceful++;
                } else if (e instanceof Projectile || e instanceof ArmorStand || e instanceof Villager
                        || e instanceof EvokerFangs || e instanceof AreaEffectCloud
                        || e instanceof WanderingTrader || e instanceof EnderSignal
                        || e instanceof EnderCrystal) {
                    others++;
                } else if (e instanceof Item || e instanceof ExperienceOrb || e instanceof Hanging) {
                    items++;
                } else if (e instanceof FallingBlock || e instanceof Explosive) {
                    items++;
                } else {
                    if (SugarcaneConfig.debug)
                        Util.logDebug("Unknown ent type: " + e.getType().name());
                }

            }
//                for(String key : entTypes.keySet()) {
//                    if(key.equals("Unknown")) System.out.println(key+": "+entTypes.get(key));
//                }
//                Bukkit.getServer().broadcastMessage("New hostile: "+ hostile);

        entities = items + players + minecarts + peaceful + hostile + others;
        String entTitle = "Entities: " + entities + " (";
        if (players > 0) entTitle += "Player " + players + " ";
        if (items > 0) entTitle += "Item " + items + " ";
        if (minecarts > 0) entTitle += "Cart " + minecarts + " ";
        if (peaceful > 0) entTitle += "Peace " + peaceful + " ";
        if (hostile > 0) entTitle += "Hostile " + hostile + (others == 0 ? "" : " ");
        if (others > 0) entTitle += "+" + others;

        entTitle += ")";
        entityBar.setTitle(entTitle);
        if (entities > 5000)
            entities = 5000;
        entityBar.setProgress((double) entities / 5000);
    }
}