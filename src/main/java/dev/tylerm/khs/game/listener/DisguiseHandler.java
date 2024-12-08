package dev.tylerm.khs.game.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import dev.tylerm.khs.Main;
import dev.tylerm.khs.game.util.Disguise;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public class DisguiseHandler implements Listener {

    public DisguiseHandler() {
        PacketEvents.getAPI().getEventManager().registerListener(createProtocol(), PacketListenerPriority.NORMAL);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Disguise disguise = Main.getInstance().getDisguiser().getDisguise(player);
        if (disguise == null) return;
        if (event.getFrom().distance(event.getTo()) > .1) {
            disguise.setSolidify(false);
        }
        disguise.startSolidifying();
    }

    private PacketListener createProtocol() {
        return new PacketListener(){
            @Override
            public void onPacketSend(PacketSendEvent event) {
                var type = event.getPacketType();
                if(type != PacketType.Play.Server.ENTITY_EQUIPMENT) return;
                var _player = event.getPlayer();
                if(_player == null) return;
                var player = PacketEvents.getAPI().getPlayerManager().getUser(event.getPlayer());
                if(player == null) return;
                var packet = new WrapperPlayServerEntityEquipment(event);
                var uuid = Main.getInstance().getEntityIdToUUID().get(packet.getEntityId());
                var isHider = Main.getInstance().getBoard().isHider(uuid);
                if(isHider){
                   event.setCancelled(true);
                }
           }

            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                if(event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;
                var packet = new WrapperPlayClientInteractEntity(event);
                var action = packet.getAction();
                // only left click attacks
                //EnumWrappers.EntityUseAction action = packet.getEntityUseActions().getValues().stream().findFirst().orElse(null);
                if(action != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
                //noinspection ComparatorResultComparison
                // iceBear: Idk what is this but it works anyfucks
                if(action.compareTo(WrapperPlayClientInteractEntity.InteractAction.INTERACT) == 2) return;

                Player player = event.getPlayer();
                int id = packet.getEntityId();
                Disguise disguise = Main.getInstance().getDisguiser().getByEntityID(id);
                if (disguise == null) disguise = Main.getInstance().getDisguiser().getByHitBoxID(id);
                if (disguise == null) return;

                if (disguise.getPlayer().getGameMode() == GameMode.CREATIVE) return;
                event.setCancelled(true);
                handleAttack(disguise, player);
            }
        };
    }

    private final List<Player> debounce = new ArrayList<>();

    @SuppressWarnings("removal")
    private void handleAttack(Disguise disguise, Player seeker) {

        if (disguise.getPlayer() == seeker) return;

        double amount;
        amount = seeker.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        disguise.setSolidify(false);
        if (debounce.contains(disguise.getPlayer())) return;

        debounce.add(disguise.getPlayer());

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            EntityDamageByEntityEvent event =
                    new EntityDamageByEntityEvent(seeker, disguise.getPlayer(),
                            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                            DamageSource.builder(DamageType.PLAYER_ATTACK)
                                    .build(),
                            amount);
            event.setDamage(amount);
            disguise.getPlayer().setLastDamageCause(event);
            Main.getInstance().getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                disguise.getPlayer().damage(amount);
                disguise.getPlayer().setVelocity(seeker.getLocation().getDirection().setY(.2).multiply(1));
            }

        }, 0);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> debounce.remove(disguise.getPlayer()), 10);
    }

}
