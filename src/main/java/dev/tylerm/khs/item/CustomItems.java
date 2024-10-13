package dev.tylerm.khs.item;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.util.item.ItemStacks;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CustomItems {
    public static final NamespacedKey CUSTOM_ITEM = new NamespacedKey(Main.getInstance(),"custom_item");
    public static final int SPEED_POTION = 10;
    public static final int BLOCK_CHANGER = 11;
    public static final int SEEKER_VISUALIZER = 12;

    public static int getId(ItemStack stack){
        if(stack.getItemMeta() == null) return -1;
        var result = stack.getItemMeta().getPersistentDataContainer().get(CUSTOM_ITEM, PersistentDataType.INTEGER);
        return result == null ? -1 : result;
    }
}
