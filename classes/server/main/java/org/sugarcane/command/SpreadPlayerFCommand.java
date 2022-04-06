package org.sugarcanemc.sugarcane.command;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;

import java.util.*;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.scheduler.MinecraftInternalPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SpreadPlayerFCommand {
    private static final Queue<Entity> queue = new PriorityQueue<Entity>();
    private static final int MAX_ITERATION_COUNT = 1000;
    private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType((object, object1, object2, object3) -> {
        return new TranslatableComponent("commands.spreadplayers.failed.teams", object, object1, object2, object3);
    });
    private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType((object, object1, object2, object3) -> {
        return new TranslatableComponent("commands.spreadplayers.failed.entities", object, object1, object2, object3);
    });
    static Random random = new Random();
    private static CommandSourceStack lsource;
    private static Vec2 lcenter;
    private static float lspreadDistance;
    private static float lmaxRange;
    private static int lmaxY;
    private static boolean lrespectTeams;

    public SpreadPlayerFCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.Commands.literal("spreadplayersf").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.Commands.argument("center", Vec2Argument.vec2()).then(net.minecraft.commands.Commands.argument("spreadDistance", FloatArgumentType.floatArg(0.0F)).then(((RequiredArgumentBuilder) net.minecraft.commands.Commands.argument("maxRange", FloatArgumentType.floatArg(1.0F)).then(net.minecraft.commands.Commands.argument("respectTeams", BoolArgumentType.bool()).then(net.minecraft.commands.Commands.argument("targets", EntityArgument.entities()).executes((commandcontext) -> {
            return SpreadPlayerFCommand.spreadPlayers(commandcontext.getSource(), Vec2Argument.getVec2(commandcontext, "center"), FloatArgumentType.getFloat(commandcontext, "spreadDistance"), FloatArgumentType.getFloat(commandcontext, "maxRange"), commandcontext.getSource().getLevel().getMaxBuildHeight(), BoolArgumentType.getBool(commandcontext, "respectTeams"), EntityArgument.getEntities(commandcontext, "targets"));
        })))).then(net.minecraft.commands.Commands.literal("under").then(net.minecraft.commands.Commands.argument("maxHeight", IntegerArgumentType.integer(0)).then(net.minecraft.commands.Commands.argument("respectTeams", BoolArgumentType.bool()).then(net.minecraft.commands.Commands.argument("targets", EntityArgument.entities()).executes((commandcontext) -> {
            return SpreadPlayerFCommand.spreadPlayers(commandcontext.getSource(), Vec2Argument.getVec2(commandcontext, "center"), FloatArgumentType.getFloat(commandcontext, "spreadDistance"), FloatArgumentType.getFloat(commandcontext, "maxRange"), IntegerArgumentType.getInteger(commandcontext, "maxHeight"), BoolArgumentType.getBool(commandcontext, "respectTeams"), EntityArgument.getEntities(commandcontext, "targets"));
        })))))))));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.isStopping() || MinecraftServer.getServer().hasStopped()) cancel();
                if (!queue.isEmpty()) {
                    List<Entity> ents = new ArrayList<>();
                    for (int i = 0; i < Math.min(queue.size(), MAX_ITERATION_COUNT); i++) {
                        ents.add(queue.remove());
                    }
                    try {
                        runSpreadPlayers(lsource, lcenter, lspreadDistance, lmaxRange, lmaxY, lrespectTeams, ents);
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimerAsynchronously(new MinecraftInternalPlugin(), 0L, 1L);
    }

    private static int spreadPlayers(CommandSourceStack source, Vec2 center, float spreadDistance, float maxRange, int maxY, boolean respectTeams, Collection<? extends Entity> players) throws CommandSyntaxException {
        lsource = source;
        lcenter = center;
        lspreadDistance = spreadDistance;
        lmaxRange = maxRange;
        lmaxY = maxY;
        lrespectTeams = respectTeams;
        queue.addAll(players);
        return players.size();
    }

    private static int runSpreadPlayers(CommandSourceStack source, Vec2 center, float spreadDistance, float maxRange, int maxY, boolean respectTeams, Collection<? extends Entity> players) throws CommandSyntaxException {
        double d0 = center.x - maxRange;
        double d1 = center.y - maxRange;
        double d2 = center.x + maxRange;
        double d3 = center.y + maxRange;
        SpreadPlayerFCommand.Position[] acommandspreadplayers_a = SpreadPlayerFCommand.createInitialPositions(random, respectTeams ? SpreadPlayerFCommand.getNumberOfTeams(players) : players.size(), d0, d1, d2, d3);

        SpreadPlayerFCommand.spreadPositions(center, spreadDistance, source.getLevel(), random, d0, d1, d2, d3, maxY, acommandspreadplayers_a, respectTeams);
        double d4 = SpreadPlayerFCommand.setPlayerPositions(players, source.getLevel(), acommandspreadplayers_a, maxY, respectTeams);

        source.sendSuccess(new TranslatableComponent("commands.spreadplayers.success." + (respectTeams ? "teams" : "entities"), acommandspreadplayers_a.length, center.x, center.y, String.format(Locale.ROOT, "%.2f", d4)), true);
        return acommandspreadplayers_a.length;
    }

    private static int getNumberOfTeams(Collection<? extends Entity> entities) {
        Set<Team> set = Sets.newHashSet();
        Iterator iterator = entities.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();

            if (entity instanceof Player) {
                set.add(entity.getTeam());
            } else {
                set.add(null); // CraftBukkit - decompile error
            }
        }

        return set.size();
    }

    private static void spreadPositions(Vec2 center, double spreadDistance, ServerLevel world, Random random, double minX, double minZ, double maxX, double maxZ, int maxY, SpreadPlayerFCommand.Position[] piles, boolean respectTeams) throws CommandSyntaxException {
        boolean flag1 = true;
        double d5 = 3.4028234663852886E38D;

        int j;

        for (j = 0; j < 10000 && flag1; ++j) {
            flag1 = false;
            d5 = 3.4028234663852886E38D;

            int k;
            SpreadPlayerFCommand.Position commandspreadplayers_a;

            for (int l = 0; l < piles.length; ++l) {
                SpreadPlayerFCommand.Position commandspreadplayers_a1 = piles[l];

                k = 0;
                commandspreadplayers_a = new SpreadPlayerFCommand.Position();

                for (int i1 = 0; i1 < piles.length; ++i1) {
                    if (l != i1) {
                        SpreadPlayerFCommand.Position commandspreadplayers_a2 = piles[i1];
                        double d6 = commandspreadplayers_a1.dist(commandspreadplayers_a2);

                        d5 = Math.min(d6, d5);
                        if (d6 < spreadDistance) {
                            ++k;
                            commandspreadplayers_a.x += commandspreadplayers_a2.x - commandspreadplayers_a1.x;
                            commandspreadplayers_a.z += commandspreadplayers_a2.z - commandspreadplayers_a1.z;
                        }
                    }
                }

                if (k > 0) {
                    commandspreadplayers_a.x /= k;
                    commandspreadplayers_a.z /= k;
                    double d7 = commandspreadplayers_a.getLength();

                    if (d7 > 0.0D) {
                        commandspreadplayers_a.normalize();
                        commandspreadplayers_a1.moveAway(commandspreadplayers_a);
                    } else {
                        commandspreadplayers_a1.randomize(random, minX, minZ, maxX, maxZ);
                    }

                    flag1 = true;
                }

                if (commandspreadplayers_a1.clamp(minX, minZ, maxX, maxZ)) {
                    flag1 = true;
                }
            }

            if (!flag1) {
                SpreadPlayerFCommand.Position[] acommandspreadplayers_a1 = piles;
                int j1 = piles.length;

                for (k = 0; k < j1; ++k) {
                    commandspreadplayers_a = acommandspreadplayers_a1[k];
                    if (!commandspreadplayers_a.isSafe(world, maxY)) {
                        commandspreadplayers_a.randomize(random, minX, minZ, maxX, maxZ);
                        flag1 = true;
                    }
                }
            }
        }

        if (d5 == 3.4028234663852886E38D) {
            d5 = 0.0D;
        }

        if (j >= 10000) {
            if (respectTeams) {
                throw SpreadPlayerFCommand.ERROR_FAILED_TO_SPREAD_TEAMS.create(piles.length, center.x, center.y, String.format(Locale.ROOT, "%.2f", d5));
            } else {
                throw SpreadPlayerFCommand.ERROR_FAILED_TO_SPREAD_ENTITIES.create(piles.length, center.x, center.y, String.format(Locale.ROOT, "%.2f", d5));
            }
        }
    }

    private static double setPlayerPositions(Collection<? extends Entity> entities, ServerLevel world, SpreadPlayerFCommand.Position[] piles, int maxY, boolean respectTeams) {
        double d0 = 0.0D;
        int j = 0;
        Map<Team, SpreadPlayerFCommand.Position> map = Maps.newHashMap();

        double d1;

        for (Iterator iterator = entities.iterator(); iterator.hasNext(); d0 += d1) {
            Entity entity = (Entity) iterator.next();
            SpreadPlayerFCommand.Position commandspreadplayers_a;

            if (respectTeams) {
                Team scoreboardteambase = entity instanceof Player ? entity.getTeam() : null;

                if (!map.containsKey(scoreboardteambase)) {
                    map.put(scoreboardteambase, piles[j++]);
                }

                commandspreadplayers_a = map.get(scoreboardteambase);
            } else {
                commandspreadplayers_a = piles[j++];
            }

            entity.teleportToWithTicket((double) Mth.floor(commandspreadplayers_a.x) + 0.5D, commandspreadplayers_a.getSpawnY(world, maxY), (double) Mth.floor(commandspreadplayers_a.z) + 0.5D);
            d1 = Double.MAX_VALUE;
            SpreadPlayerFCommand.Position[] acommandspreadplayers_a1 = piles;
            int k = piles.length;

            for (int l = 0; l < k; ++l) {
                SpreadPlayerFCommand.Position commandspreadplayers_a1 = acommandspreadplayers_a1[l];

                if (commandspreadplayers_a != commandspreadplayers_a1) {
                    double d2 = commandspreadplayers_a.dist(commandspreadplayers_a1);

                    d1 = Math.min(d2, d1);
                }
            }
        }

        if (entities.size() < 2) {
            return 0.0D;
        } else {
            d0 /= entities.size();
            return d0;
        }
    }

    private static SpreadPlayerFCommand.Position[] createInitialPositions(Random random, int count, double minX, double minZ, double maxX, double maxZ) {
        SpreadPlayerFCommand.Position[] acommandspreadplayers_a = new SpreadPlayerFCommand.Position[count];

        for (int j = 0; j < acommandspreadplayers_a.length; ++j) {
            SpreadPlayerFCommand.Position commandspreadplayers_a = new SpreadPlayerFCommand.Position();

            commandspreadplayers_a.randomize(random, minX, minZ, maxX, maxZ);
            acommandspreadplayers_a[j] = commandspreadplayers_a;
        }

        return acommandspreadplayers_a;
    }

    private static class Position {

        double x;
        double z;

        Position() {
        }

        // CraftBukkit start - add a version of getBlockState which force loads chunks
        private static BlockState getBlockState(BlockGetter iblockaccess, BlockPos position) {
            ((ServerLevel) iblockaccess).getChunkSource().getChunk(position.getX() >> 4, position.getZ() >> 4, true);
            return iblockaccess.getBlockState(position);
        }

        double dist(SpreadPlayerFCommand.Position other) {
            double d0 = this.x - other.x;
            double d1 = this.z - other.z;

            return Math.sqrt(d0 * d0 + d1 * d1);
        }

        void normalize() {
            double d0 = this.getLength();

            this.x /= d0;
            this.z /= d0;
        }

        double getLength() {
            return Math.sqrt(this.x * this.x + this.z * this.z);
        }

        public void moveAway(SpreadPlayerFCommand.Position other) {
            this.x -= other.x;
            this.z -= other.z;
        }

        public boolean clamp(double minX, double minZ, double maxX, double maxZ) {
            boolean flag = false;

            if (this.x < minX) {
                this.x = minX;
                flag = true;
            } else if (this.x > maxX) {
                this.x = maxX;
                flag = true;
            }

            if (this.z < minZ) {
                this.z = minZ;
                flag = true;
            } else if (this.z > maxZ) {
                this.z = maxZ;
                flag = true;
            }

            return flag;
        }

        public int getSpawnY(BlockGetter blockView, int maxY) {
            BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos(this.x, maxY + 1, this.z);
            boolean flag = blockView.getBlockState(blockposition_mutableblockposition).isAir();

            blockposition_mutableblockposition.move(Direction.DOWN);

            boolean flag1;

            for (boolean flag2 = blockView.getBlockState(blockposition_mutableblockposition).isAir(); blockposition_mutableblockposition.getY() > blockView.getMinBuildHeight(); flag2 = flag1) {
                blockposition_mutableblockposition.move(Direction.DOWN);
                flag1 = Position.getBlockState(blockView, blockposition_mutableblockposition).isAir(); // CraftBukkit
                if (!flag1 && flag2 && flag) {
                    return blockposition_mutableblockposition.getY() + 1;
                }

                flag = flag2;
            }

            return maxY + 1;
        }

        public boolean isSafe(BlockGetter world, int maxY) {
            BlockPos blockposition = new BlockPos(this.x, this.getSpawnY(world, maxY) - 1, this.z);
            BlockState iblockdata = Position.getBlockState(world, blockposition); // CraftBukkit
            Material material = iblockdata.getMaterial();

            return blockposition.getY() < maxY && !material.isLiquid() && material != Material.FIRE;
        }

        public void randomize(Random random, double minX, double minZ, double maxX, double maxZ) {
            this.x = Mth.nextDouble(random, minX, maxX);
            this.z = Mth.nextDouble(random, minZ, maxZ);
        }
        // CraftBukkit end
    }
}
