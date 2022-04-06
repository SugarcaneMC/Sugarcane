package org.sugarcanemc.sugarcane.command;

import java.util.Map;
import java.util.HashMap;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.commands.SpreadPlayersCommand;
import org.bukkit.command.Command;
import net.minecraft.server.MinecraftServer;

public class SugarcaneCommandsManager {
    private static Map<String, Command> commands;

    public static void registerCommands() {
        commands = new HashMap<>();
        commands.put("nspt", new NSPTCommand("nspt"));
        commands.put("stats", new StatsCommand("stats"));
        if (commands != null)
            for (var command : commands.entrySet()) {
                MinecraftServer.getServer().server.getCommandMap().register(command.getKey(), "Sugarcane", command.getValue());
            }
    }
    public static void registerWithDispatcher(CommandDispatcher dispatcher){
        SpreadPlayersCommand.register(dispatcher);
    }
}
