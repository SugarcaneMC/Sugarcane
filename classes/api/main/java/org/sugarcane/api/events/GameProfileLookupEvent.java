package org.sugarcanemc.sugarcane.api.events;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameProfileLookupEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private GameProfile gameProfile = null;
    private final UUID uuid;
    private final String name;

    public GameProfileLookupEvent(boolean async, @NotNull UUID uuid, @NotNull String name) {
        super(async);
        this.uuid = uuid;
        this.name = name;
    }

    @Nullable
    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(@Nullable GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}