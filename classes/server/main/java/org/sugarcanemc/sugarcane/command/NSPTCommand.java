package org.sugarcanemc.sugarcane.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class NSPTCommand extends Command {

    public NSPTCommand(String name) {
        super(name);
        this.description = "View server tick times in nanoseconds";
        this.usageMessage = "/nspt";
        this.setPermission("bukkit.command.nspt");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        MinecraftServer server = MinecraftServer.getServer();

        List<String> times = new ArrayList<>();
        times.addAll(eval(server.tickTimes5s.getTimes()));
        times.addAll(eval(server.tickTimes10s.getTimes()));
        times.addAll(eval(server.tickTimes60s.getTimes()));

        sender.sendMessage("§6Server tick NS times §e(§7avg§e/§7min§e/§7max§e)§6 from last 5s§7,§6 10s§7,§6 1m§e:");
        sender.sendMessage(String.format("§6◴ %s§7/%s§7/%s§e, %s§7/%s§7/%s§e, %s§7/%s§7/%s", times.toArray()));
        return true;
    }

    private static List<String> eval(long[] times) {
        long min = Integer.MAX_VALUE;
        long max = 0L;
        long total = 0L;
        for (long value : times) {
            if (value > 0L && value < min) min = value;
            if (value > max) max = value;
            total += value;
        }
        double avgD = ((double) total / (double) times.length);
        return Arrays.asList(getColor(avgD), getColor(min), getColor(max));
    }

    private static String getColor(double avg) {
        return ChatColor.COLOR_CHAR + (avg >= 5E+7 ? "c" : avg >= (4E+7) ? "e" : "a") + avg;
    }
}