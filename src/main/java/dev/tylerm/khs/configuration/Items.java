package dev.tylerm.khs.configuration;

import dev.tylerm.khs.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Items {

    public static List<ItemStack> HIDER_ITEMS, SEEKER_ITEMS;
    public static ItemStack
            HIDER_HELM, SEEKER_HELM,
            HIDER_CHEST, SEEKER_CHEST,
            HIDER_LEGS, SEEKER_LEGS,
            HIDER_BOOTS, SEEKER_BOOTS;

    public static List<PotionEffect> HIDER_EFFECTS, SEEKER_EFFECTS;

    public static void loadItems() {

        ConfigManager manager = ConfigManager.create("items.yml");

        SEEKER_ITEMS = new ArrayList<>();
        SEEKER_HELM = null;
        SEEKER_CHEST = null;
        SEEKER_LEGS = null;
        SEEKER_BOOTS = null;

        ConfigurationSection SeekerItems = manager.getConfigurationSection("items.seeker");
        for (String key : SeekerItems.getKeys(false)) {
            ConfigurationSection section = SeekerItems.getConfigurationSection(key);
            if (section == null) {
                SEEKER_ITEMS.add(null);
                continue;
            }
            ItemStack item = ItemUtil.createItem(section);
            SEEKER_ITEMS.add(item);
        }

        ConfigurationSection SeekerHelmet = SeekerItems.getConfigurationSection("helmet");
        if (SeekerHelmet != null) {
            ItemStack item = ItemUtil.createItem(SeekerHelmet);
            if (item != null) {
                SEEKER_HELM = item;
            }
        }

        ConfigurationSection SeekerChestplate = SeekerItems.getConfigurationSection("chestplate");
        if (SeekerChestplate != null) {
            ItemStack item = ItemUtil.createItem(SeekerChestplate);
            if (item != null) {
                SEEKER_CHEST = item;
            }
        }

        ConfigurationSection SeekerLeggings = SeekerItems.getConfigurationSection("leggings");
        if (SeekerLeggings != null) {
            ItemStack item = ItemUtil.createItem(SeekerLeggings);
            if (item != null) {
                SEEKER_LEGS = item;
            }
        }

        ConfigurationSection SeekerBoots = SeekerItems.getConfigurationSection("boots");
        if (SeekerBoots != null) {
            ItemStack item = ItemUtil.createItem(SeekerBoots);
            if (item != null) {
                SEEKER_BOOTS = item;
            }
        }

        HIDER_ITEMS = new ArrayList<>();
        HIDER_HELM = null;
        HIDER_CHEST = null;
        HIDER_LEGS = null;
        HIDER_BOOTS = null;

        ConfigurationSection HiderItems = manager.getConfigurationSection("items.hider");
        for (String key : HiderItems.getKeys(false)) {
            ConfigurationSection section = HiderItems.getConfigurationSection(key);
            if (section == null) {
                HIDER_ITEMS.add(null);
                continue;
            }
            ItemStack item = ItemUtil.createItem(section);
            HIDER_ITEMS.add(item);
        }

        ConfigurationSection HiderHelmet = HiderItems.getConfigurationSection("helmet");
        if (HiderHelmet != null) {
            ItemStack item = ItemUtil.createItem(HiderHelmet);
            if (item != null) {
                HIDER_HELM = item;
            }
        }

        ConfigurationSection HiderChestplate = HiderItems.getConfigurationSection("chestplate");
        if (HiderChestplate != null) {
            ItemStack item = ItemUtil.createItem(HiderChestplate);
            if (item != null) {
                HIDER_CHEST = item;
            }
        }

        ConfigurationSection HiderLeggings = HiderItems.getConfigurationSection("leggings");
        if (HiderLeggings != null) {
            ItemStack item = ItemUtil.createItem(HiderLeggings);
            if (item != null) {
                HIDER_LEGS = item;
            }
        }

        ConfigurationSection HiderBoots = HiderItems.getConfigurationSection("boots");
        if (HiderBoots != null) {
            ItemStack item = ItemUtil.createItem(HiderBoots);
            if (item != null) {
                HIDER_BOOTS = item;
            }
        }

        SEEKER_EFFECTS = new ArrayList<>();
        ConfigurationSection SeekerEffects = manager.getConfigurationSection("effects.seeker");
        for (String key : SeekerEffects.getKeys(false)) {
            var section = SeekerEffects.getConfigurationSection(key);
            if (section == null) break;
            PotionEffect effect = getPotionEffect(section);
            if (effect != null) SEEKER_EFFECTS.add(effect);
        }

        HIDER_EFFECTS = new ArrayList<>();
        ConfigurationSection HiderEffects = manager.getConfigurationSection("effects.hider");
        for (String key : HiderEffects.getKeys(false)) {
            var section = HiderEffects.getConfigurationSection(key);
            if (section == null) break;
            PotionEffect effect = getPotionEffect(section);
            if (effect != null) HIDER_EFFECTS.add(effect);
        }
    }

    private static PotionEffect getPotionEffect(ConfigurationSection item) {
        String type = item.getString("type");
        if (type == null) return null;
        if (PotionEffectType.getByName(type.toUpperCase()) == null) return null;
        return new PotionEffect(
                PotionEffectType.getByName(type.toUpperCase()),
                item.getInt("duration"),
                item.getInt("amplifier"),
                item.getBoolean("ambient"),
                item.getBoolean("particles")
        );
    }

    public static boolean matchItem(ItemStack stack) {
        for (ItemStack check : HIDER_ITEMS)
            if (equals(stack, check)) return true;
        for (ItemStack check : SEEKER_ITEMS)
            if (equals(stack, check)) return true;
        return false;
    }

    private static boolean equals(ItemStack a, ItemStack b) {
        if (a == null || b == null) {
            return false;
        } else if (a == b) {
            return true;
        } else {
            return a.getType() == b.getType() && a.hasItemMeta() == b.hasItemMeta() && (!a.hasItemMeta() || Bukkit.getItemFactory().equals(a.getItemMeta(), b.getItemMeta()));
        }
    }

}
