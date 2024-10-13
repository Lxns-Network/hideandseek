package dev.tylerm.khs.util;

import com.cryptomorin.xseries.XItemStack;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class ItemUtil{
    public static ItemStack createItem(ConfigurationSection item) {
        ConfigurationSection config = new YamlConfiguration().createSection("temp");
        String material = item.getString("material").toUpperCase();
        boolean splash = false;
        if (material.contains("POTION")) {
            if(!item.contains("potionLevel")){
                config.set("level", 1);
            }else{
                config.set("level", item.getInt("potionLevel"));
            }
        }
        if (material.equalsIgnoreCase("SPLASH_POTION") || material.equalsIgnoreCase("LINGERING_POTION")) {
            material = "POTION";
            splash = true;
        }
        config.set("name", item.getString("name"));
        config.set("material", material);
        config.set("enchants", item.getConfigurationSection("enchantments"));
        config.set("unbreakable", item.getBoolean("unbreakable"));
        if (item.contains("model-data")) {
            config.set("model-data", item.getInt("model-data"));
        }
        if (item.isSet("lore"))
            config.set("lore", item.getStringList("lore"));
        if (material.equalsIgnoreCase("POTION") || material.equalsIgnoreCase("SPLASH_POTION") || material.equalsIgnoreCase("LINGERING_POTION"))
            config.set("base-effect", String.format("%s,%s,%s", item.getString("type"), false, splash));
        ItemStack stack = XItemStack.deserialize(config);
        int amt = item.getInt("amount");
        if (amt < 1) amt = 1;
        stack.setAmount(amt);
        if (stack.getData().getItemType() == Material.AIR) return null;
        return stack;
    }
}
