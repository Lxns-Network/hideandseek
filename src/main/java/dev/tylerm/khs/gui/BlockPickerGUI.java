package dev.tylerm.khs.gui;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.configuration.Map;
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
import org.ipvp.canvas.type.ChestMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockPickerGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Map map;
    private final Runnable nextAction;
    private boolean selected;

    public BlockPickerGUI(Player player, Map map, Runnable nextAction) {
        this.map = map;
        this.nextAction = nextAction;
        var choices = map.getBlockHunt();
        this.inventory = Bukkit.createInventory(this, Math.ceilDiv(choices.size(), 9) * 9, "选择方块");
        for (int i = 0; i < choices.size(); i++) {
            inventory.setItem(i, new ItemStack(choices.get(i)));
        }
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public static class GuiHandler implements Listener {
        @EventHandler
        void onClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof BlockPickerGUI gui) || event.getCursor() == null) {
                return;
            }
            event.setCancelled(true);
            var mat = gui.map.getBlockHunt().get(event.getRawSlot());
            if (mat == null) return;
            Player player = (Player) event.getWhoClicked();
            Main.getInstance().getDisguiser().disguise(player, mat, gui.map);
            gui.selected = true;
            player.closeInventory();
            Bukkit.getScheduler().runTask(Main.getInstance(), gui.nextAction);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        void onClose(InventoryCloseEvent event) {
            if (!(event.getInventory().getHolder() instanceof BlockPickerGUI gui)) {
                return;
            }
            if(!gui.selected){

                var mat = gui.map.getBlockHunt().get(0);
                Player player = (Player) event.getPlayer();
                Main.getInstance().getDisguiser().disguise(player, mat, gui.map);
            }
            Bukkit.getScheduler().runTask(Main.getInstance(), gui.nextAction);
        }
    }
}
