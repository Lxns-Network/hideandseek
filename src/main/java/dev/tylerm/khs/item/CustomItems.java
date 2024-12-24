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
    public static final int OWL_BOW_MARKER = 14;
    public static final int BLINDNESS_WAND = 13;
    public static final String OWL_BOW_METADATA_KEY = "owl_bow";

    public static int getId(ItemStack stack){
        if(stack.getItemMeta() == null || !stack.getItemMeta().hasCustomModelData()) return -1;
        return stack.getItemMeta().getCustomModelData();
//        var result = stack.getItemMeta().getPersistentDataContainer().get(CUSTOM_ITEM, PersistentDataType.INTEGER);
//        return result == null ? -1 : result;
    }
}
