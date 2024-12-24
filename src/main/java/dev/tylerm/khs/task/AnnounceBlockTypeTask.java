package dev.tylerm.khs.task;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.game.util.Status;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class AnnounceBlockTypeTask extends BukkitRunnable {
    private static BaseComponent EMPTY = TextComponent.fromLegacy("");
    @Override
    public void run() {
        if(Main.getInstance().getGame().getStatus() != Status.PLAYING) return;
        BaseComponent v;
        if(Main.getInstance().getBoard().getHiders().isEmpty()){
            v = EMPTY;
        }else{
            var first = Main.getInstance().getDisguiser().getDisguise(Main.getInstance().getBoard().getHiders().getFirst());
            v = new TranslatableComponent(first.getMaterial().getBlockTranslationKey());
        }
        var text = TextComponent.fromLegacy(ChatColor.GREEN+" TIP: "+ChatColor.WHITE+"场上有一个");
        text.addExtra(v);
        text.addExtra(" 方块!");
        Bukkit.spigot().broadcast(text);
    }
}
