package org.sugarcanemc.sugarcane.config;

import java.util.HashMap;

public class MobTPSThresholds {
    private static DynamicConfig Config = new DynamicConfig("config/tps-thresholds.yml");
    public static int CurrentTPS = 20;
    private static HashMap<Class<?>, Integer> Thresholds = new HashMap<>();
    public static int getTickThreshold(Class<?> type){
        if(MobTPSThresholds.Thresholds.containsKey(type)){
            return MobTPSThresholds.Thresholds.get(type);
        }
        else {
            System.out.printf("Could not get TPS threshold for %s, saving default!\n", type.getSimpleName());
            var val = Config.getInt("tps-tresholds.tick."+type.getSimpleName(), -1);
            if(val == -1) val = getDefault(type);
            if(val != -1) Config.set("tps-tresholds.tick."+type.getSimpleName(), val);
            MobTPSThresholds.Thresholds.put(type, val);
            Config.Save();
            return val;
        }
    }

    //per entity type defaults go here
    private static int getDefault(Class<?> type) {
        return 10;
    }

    // Config class loading:
    public static void loadMobThresholds() {
        var stime = System.nanoTime();
        System.out.println("Loading mob thresholds...");
        Config.Load();
        System.out.printf("Loaded mob TPS thresholds in %s ms\n", (System.nanoTime() - stime) / 1000000d);
    }
}