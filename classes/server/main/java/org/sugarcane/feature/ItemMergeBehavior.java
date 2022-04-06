package org.sugarcanemc.sugarcane.feature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.sugarcanemc.sugarcane.config.SugarcaneConfig;

public class ItemMergeBehavior {
    public static void TickItem(ItemEntity item) {
        if (SugarcaneConfig.SplitItems && item.getItem().getCount() > 1) {
            for (int i = 0; i < item.getItem().getCount(); i++) {
                //spawn item
                Vec3 pos = item.getEyePosition();
                var is = item.getItem().copy();
                is.setCount(1);
                ItemEntity a = item.spawnAtLocation(is);
                if (item.getDeltaMovement().normalize().length() == 0)
                    a.setDeltaMovement((Math.random() * 2 - 1) / 10, Math.random(), (Math.random() * 2 - 1) / 10);
                else
                    a.setDeltaMovement(item.getDeltaMovement().multiply(1 + (Math.random()) / 10, Math.random(), 1 + (Math.random()) / 10));
                a.setPickUpDelay(item.pickupDelay);
            }
            item.discard();
        }
    }

    public static boolean IsItemEntityMergable(ItemEntity item, ItemStack itemstack) {
        //vanilla
        if (SugarcaneConfig.ItemMergeBehavior == -1)
            return item.isAlive() && item.pickupDelay != 32767 && item.age != -32768 && item.age < item.getDespawnRate() && itemstack.getCount() < itemstack.getMaxStackSize();
            // Sugarcane - merge items infinitely
        else if (SugarcaneConfig.ItemMergeBehavior == 0)
            return item.isAlive() && item.pickupDelay != 32767 && item.age != -32768 && item.age < item.getDespawnRate();
            // Sugarcane - limit to 1 item per stack
        else return false; // Sugarcane - handled during tick
    }

    public static boolean AreItemEntitiesMergable(ItemStack stack1, ItemStack stack2) {
        // Sugarcane - item drop behavior - infinite merging
        if (SugarcaneConfig.ItemMergeBehavior == 0)
            return stack2.is(stack1.getItem()) && (!(stack2.hasTag() ^ stack1.hasTag()) && (!stack2.hasTag() || stack2.getTag().equals(stack1.getTag())));
        else
            return stack2.is(stack1.getItem()) && (stack2.getCount() + stack1.getCount() <= stack2.getMaxStackSize() && (!(stack2.hasTag() ^ stack1.hasTag()) && (!stack2.hasTag() || stack2.getTag().equals(stack1.getTag()))));
    }

    public static int GetMergeTargetCount(ItemStack stack1, ItemStack stack2, int maxCount) {
        if (SugarcaneConfig.ItemMergeBehavior == 0) maxCount = Integer.MAX_VALUE;
        return Math.min(maxCount - stack1.getCount(), stack2.getCount());
    }

    public static ItemStack MergeItemStacks(ItemEntity targetEntity, ItemStack stack1, ItemStack stack2) {
        int max = 64;
        if (SugarcaneConfig.ItemMergeBehavior == 0) max = Integer.MAX_VALUE;
        ItemStack itemstack2 = ItemEntity.merge(stack1, stack2, max);
        if (SugarcaneConfig.verbose) {
            System.out.println(String.format("Item merged: %s + %s -> %s/%s", stack1.getCount(), stack2.getCount(), itemstack2.getCount(), max));
        }
        return itemstack2;
    }

    public static void SplitItemStacksForSave(ItemEntity item) {
        while (item.getItem().getCount() > item.getItem().getMaxStackSize()) {
            ItemStack e = item.getItem().copy();
            e.setCount(item.getItem().getMaxStackSize());
            item.spawnAtLocation(e).save(new CompoundTag());
            item.getItem().shrink(item.getItem().getMaxStackSize());
        }
    }

    public static void HandlePlayerTouch(ItemEntity item, Player player){
        if (!item.level.isClientSide) {
            ItemStack itemstack = item.getItem();
            int count = itemstack.getCount();

            // CraftBukkit start - fire PlayerPickupItemEvent
            int canHold = player.getInventory().canHold(itemstack);
            int remaining = Math.max(count - canHold, 0);
            boolean flyAtPlayer = false; // Paper

            // Paper start
            if(item.pickupDelay <= 0) {
                // Sugarcane - Move paper event here
                PlayerAttemptPickupItemEvent attemptEvent = new PlayerAttemptPickupItemEvent((org.bukkit.entity.Player) player.getBukkitEntity(), (org.bukkit.entity.Item) item.getBukkitEntity(), remaining);
                item.level.getCraftServer().getPluginManager().callEvent(attemptEvent);

                if (canHold > 0) {
                    itemstack.setCount(canHold);
                    // Call legacy event
                    PlayerPickupItemEvent playerEvent = new PlayerPickupItemEvent((org.bukkit.entity.Player) player.getBukkitEntity(), (org.bukkit.entity.Item) item.getBukkitEntity(), remaining);
                    item.level.getCraftServer().getPluginManager().callEvent(playerEvent);
                    // Call newer event afterwards
                    EntityPickupItemEvent entityEvent = new EntityPickupItemEvent((org.bukkit.entity.Player) player.getBukkitEntity(), (org.bukkit.entity.Item) item.getBukkitEntity(), remaining);
                    item.level.getCraftServer().getPluginManager().callEvent(entityEvent);

                    if (playerEvent.isCancelled() || entityEvent.isCancelled() || attemptEvent.isCancelled()) {
                        itemstack.setCount(count); // SPIGOT-5294 - restore count
                        return;
                    }

                    flyAtPlayer = playerEvent.getFlyAtPlayer(); // Paper
                    // Update the ItemStack if it was changed in the event
                    ItemStack current = item.getItem();
                    if (!itemstack.equals(current)) {
                        itemstack = current;

                    }
                    boolean full = false;
                    int added = 0;
                    while(!full) {
                        if (item.pickupDelay == 0 && (item.getOwner() == null || item.getOwner().equals(player.getUUID()))) {
                            ItemStack is = new ItemStack(item.getItem().getItem(), 1);
                            is.setTag(item.getItem().getTag());

                            if(player.getInventory().add(is)) {
                                // Paper Start
                                if (flyAtPlayer) {
                                    player.take(item, 1);
                                    added++;
                                }
                                // Paper End
                                if (itemstack.isEmpty()) {
                                    item.discard();
                                    itemstack.setCount(count);
                                    player.awardStat(Stats.ITEM_PICKED_UP.get(itemstack.getItem()), added);
                                    player.onItemPickup(item);
                                    return;
                                }

                                itemstack.shrink(1);
                            } else {
                                full = true;
                            }
                        }
                    }
                    player.awardStat(Stats.ITEM_PICKED_UP.get(itemstack.getItem()), added);
                    player.onItemPickup(item);
                }
            }
        }
    }
}
