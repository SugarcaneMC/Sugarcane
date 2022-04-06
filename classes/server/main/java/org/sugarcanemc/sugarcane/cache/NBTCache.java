package org.sugarcanemc.sugarcane.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.nbt.CompoundTag;

import java.io.File;

public class NBTCache extends Object2ObjectLinkedOpenCustomHashMap<File, CompoundTag> {

    public NBTCache() {
        super(100, 0.75F, new Strategy<File>() {
            @Override
            public int hashCode(File k) {
                return k.hashCode();
            }

            @Override
            public boolean equals(File k, File k1) {
                return k.equals(k1);
            }
        });
    }

    @Override
    public CompoundTag put(File k, CompoundTag v) {
        if (this.size() > MinecraftServer.getServer().getPlayerCount()) {
            this.removeLast();
        }
        return super.putAndMoveToFirst(k, v);
    }
}
