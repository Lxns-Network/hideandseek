package dev.tylerm.khs.configuration.skill;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record HiderSkill(
        ItemStack display,
        List<ItemStack> initialKits
) {
}
