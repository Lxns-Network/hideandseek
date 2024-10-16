package dev.tylerm.khs.gui;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.configuration.skill.HiderSkill;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SkillSelectionGUI implements InventoryHolder {
    private static final HiderSkill NONE;
    private final Inventory inventory;
    private final Runnable nextAction;
    private final List<HiderSkill> skills;
    private boolean selected;

    static {
        var display = new ItemStack(Material.BARRIER);
        var meta = display.getItemMeta();
        meta.setDisplayName("No skills :(");
        meta.setLore(List.of("You won't get anything."));
        display.setItemMeta(meta);
        NONE = new HiderSkill(display, List.of());
    }

    public SkillSelectionGUI(Player player, List<HiderSkill> skills, Runnable nextAction) {
        this.nextAction = nextAction;
        this.inventory = Bukkit.createInventory(this, Math.ceilDiv(skills.size(), 9) * 9);
        this.skills = skills.isEmpty() ? Collections.singletonList(NONE) : skills;
        for (HiderSkill skill : skills) {
            inventory.addItem(skill.display());
        }
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public static class InventoryHandler implements Listener {
        @EventHandler
        void onClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof SkillSelectionGUI gui) || event.getCursor() == null) {
                return;
            }
            event.setCancelled(true);
            if(event.getRawSlot() < 0 || event.getRawSlot() > gui.skills.size())return;
            var skill = gui.skills.get(event.getRawSlot());
            if (skill == null) {
                return;
            }
            var player = event.getWhoClicked();
            Main.getInstance().getBoard().getHiderSelectedSkills().put(player.getUniqueId(), skill);
            gui.selected = true;
            player.closeInventory();
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        void onClose(InventoryCloseEvent event) {
            if (!(event.getInventory().getHolder() instanceof SkillSelectionGUI gui)) {
                return;
            }
            var player = event.getPlayer();
            if (!gui.selected) {
                Main.getInstance().getBoard().getHiderSelectedSkills().put(player.getUniqueId(), gui.skills.getFirst());
            }
            Bukkit.getScheduler().runTask(Main.getInstance(), gui.nextAction);
        }
    }
}
