package org.sugarcanemc.sugarcane.util;

import net.minecraft.world.entity.Entity;
import org.reflections.Reflections;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class ClassGenerators {
    private static final String cd = "generated_classes";

    public static void Generate() {
        System.out.println("Generating classes...");
        new File(cd).mkdir();
        //deprecated, use as template only:
        //GenerateMobSpawnTresholds();
    }

    public static void GenerateMobSpawnTresholds() {
        var stime = System.nanoTime();
        var fullBlacklist = Arrays.stream(("LightningBolt EndCrystal LivingEntity HangingEntity " +
                "AbstractMinecart EnderDragon EyeOfEnder Mob Player LeashFenceKnotEntity AbstractMinecartContainer " +
                "FireworkRocketEntity AbstractArrow ShulkerBullet FishingHook LlamaSpit AbstractHurtingProjectile " +
                "ThrowableProjectile FlyingMob EnderDragonPart Projectile EvokerFangs PathfinderMob AmbientCreature " +
                "ServerPlayer GlowItemFrame WaterAnimal Monster AbstractGolem AgeableMob AbstractPiglin AbstractSkeleton " +
                "PatrollingMonster AbstractFish Animal AbstractVillager AbstractSchoolingFish AbstractHorse TamableAnimal " +
                "AbstractIllager AbstractChestedHorse ShoulderRidingEntity AreaEffectCloud DragonFireball Marker ItemFrame " +
                "Painting Endermite").split(" ")).toList();
        var spawnBlacklist = Arrays.stream(("FallingBlockEntity ExperienceOrb PrimedTnt ItemEntity Boat Marker " +
                "ArmorStand Minecart MinecartFurnace MinecartTNT MinecartCommandBlock MinecartFurnace MinecartSpawner " +
                "ThrownTrident DragonFireball SpectralArrow Fireball ThrowableItemProjectile SmallFireball LargeFireball " +
                "ThrownPotion ThrownEggThrownEnderPearl ThrownExperienceBottle Giant WitherBoss Vex Silverfish CaveSpider " +
                "Villager ZombieHorse SkeletonHorse TraderLlama MinecartHopper MinecartChest Arrow ThrownEnderpearl" +
                "ThrownEgg ItemFrame ThrownEgg Snowball").split(" ")).toList();
        var tickBlacklist = Arrays.stream("".split(" ")).toList();
        ArrayList<String> configClass = new ArrayList<>();
        configClass.add("\n\t// Config class loading:");
        configClass.add("\tpublic static void loadMobTresholds() {");
        configClass.add("\t\tvar stime = System.nanoTime();");
        configClass.add("\t\tSystem.out.println(\"Loading mob tresholds...\");");

        var file = openFile(cd + "/MobTPSTresholds.java");
        try {
            //write header
            file.write("""
                    package org.sugarcanemc.sugarcane.config;

                    public class MobTPSTresholds {
                    """);
            //write vars
            Reflections reflections = new Reflections("net.minecraft");
            Set<Class<? extends Entity>> classes = reflections.getSubTypesOf(Entity.class);
            for (var i : classes) {
                System.out.println(i.getSimpleName());
                if (i.getSimpleName() != "") {
                    if (!fullBlacklist.contains(i.getSimpleName())) {
                        file.write(String.format("\t//Tresholds for %s:\n", i.getSimpleName()));
                        file.flush();
                        if (!spawnBlacklist.contains(i.getSimpleName())) {
                            file.write(String.format("\tpublic static int %sSpawnTreshold = 7;\n", i.getSimpleName()));
                            configClass.add("\t\t%SSpawnTreshold = SugarcaneConfig.getInt(\"performance.tps-treshold.%s.spawn\", %SSpawnTreshold);".replace("%s", i.getSimpleName().toLowerCase()).replace("%S", i.getSimpleName()));
                        }
                        if (!tickBlacklist.contains(i.getSimpleName())) {
                            file.write(String.format("\tpublic static int %sTickTreshold = 10;\n", i.getSimpleName()));
                            configClass.add("\t\t%STickTreshold = SugarcaneConfig.getInt(\"performance.tps-treshold.%s.tick\", %STickTreshold);".replace("%s", i.getSimpleName().toLowerCase()).replace("%S", i.getSimpleName()));
                        }
                    }
                }
            }
            configClass.add("\t\tSystem.out.printf(\"Loaded mob TPS tresholds in %s ms\\n\", (System.nanoTime() - stime) / 1000000d);");
            configClass.add("\t}\n");
            file.write(String.join("\n", configClass));
            file.write("}");
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Generated mob TPS treshold class in %s ms\n", (System.nanoTime() - stime) / 1000000d);
    }

    private static FileWriter openFile(String name) {
        var file = new File(name);
        file.delete();
        try {
            file.createNewFile();
            return new FileWriter(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Couldn't open %s for writing, exiting!");
        System.exit(0);
        return null;
    }
}
