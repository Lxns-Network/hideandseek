package dev.tylerm.khs;

import com.cryptomorin.xseries.XItemStack;
import com.github.retrooper.packetevents.PacketEvents;
import dev.tylerm.khs.command.*;
import dev.tylerm.khs.command.map.Debug;
import dev.tylerm.khs.command.map.GoTo;
import dev.tylerm.khs.command.map.ListMap;
import dev.tylerm.khs.command.map.Save;
import dev.tylerm.khs.command.map.blockhunt.blocks.Add;
import dev.tylerm.khs.command.map.blockhunt.blocks.ListBlocks;
import dev.tylerm.khs.command.map.blockhunt.blocks.Remove;
import dev.tylerm.khs.command.map.set.*;
import dev.tylerm.khs.configuration.*;
import dev.tylerm.khs.configuration.skill.HiderSkill;
import dev.tylerm.khs.game.*;
import dev.tylerm.khs.game.listener.*;
import dev.tylerm.khs.game.util.Status;
import dev.tylerm.khs.gui.BlockPickerGUI;
import dev.tylerm.khs.gui.SkillSelectionGUI;
import dev.tylerm.khs.item.CustomItems;
import dev.tylerm.khs.task.AnnounceBlockTypeTask;
import dev.tylerm.khs.util.PAPIExpansion;
import dev.tylerm.khs.command.map.blockhunt.Enabled;
import dev.tylerm.khs.command.world.Create;
import dev.tylerm.khs.command.world.Delete;
import dev.tylerm.khs.command.world.Tp;
import dev.tylerm.khs.database.Database;
import dev.tylerm.khs.command.util.CommandGroup;
import dev.tylerm.khs.util.item.ItemStacks;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.ipvp.canvas.MenuFunctionListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.tylerm.khs.configuration.Config.*;
import static dev.tylerm.khs.configuration.Localization.message;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private Database database;
    private Board board;
    private Disguiser disguiser;
    private Game game;
    private CommandGroup commandGroup;
    private List<HiderSkill> hiderSkills;
    private boolean loaded;
    private Map<Integer, UUID> entityIdToUUID = new ConcurrentHashMap<>();


    public void onEnable() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        long start = System.currentTimeMillis();

        getLogger().info("Loading Kenshin's Hide and Seek");
        Main.instance = this;

        getLogger().info("Getting minecraft version...");
        this.updateVersion();
        ;

        try {
            getLogger().info("Loading config.yml...");
            loadConfig();
            getLogger().info("Loading maps.yml...");
            Maps.loadMaps();
            getLogger().info("Loading localization.yml...");
            Localization.loadLocalization();
            getLogger().info("Loading items.yml...");
            Items.loadItems();
            getLogger().info("Loading skills");
            hiderSkills = loadSkills();
            getLogger().info("Loading leaderboard.yml...");
            Leaderboard.loadLeaderboard();
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Creating internal scoreboard...");
        this.board = new Board();
        getLogger().info("Connecting to database...");
        this.database = new Database();
        getLogger().info("Loading disguises...");
        this.disguiser = new Disguiser();
        getLogger().info("Registering listeners...");
        this.registerListeners();
        PacketEvents.getAPI().init();

        getLogger().info("Registering commands...");
        this.commandGroup = new CommandGroup("hs",
                new Help(),
                new Reload(),
                new Join(),
                new Leave(),
                new Send(),
                new Start(),
                new Stop(),
                new CommandGroup("map",
                        new CommandGroup("blockhunt",
                                new CommandGroup("blocks",
                                        new Add(),
                                        new Remove(),
                                        new ListBlocks()
                                ),
                                new Enabled()
                        ),
                        new CommandGroup("set",
                                new Lobby(),
                                new Spawn(),
                                new SeekerLobby(),
                                new Border(),
                                new Bounds()
                        ),
                        new CommandGroup("unset",
                                new dev.tylerm.khs.command.map.unset.Border()
                        ),
                        new dev.tylerm.khs.command.map.Add(),
                        new dev.tylerm.khs.command.map.Remove(),
                        new ListMap(),
                        new dev.tylerm.khs.command.map.Status(),
                        new Save(),
                        new Debug(),
                        new GoTo()
                ),
                new CommandGroup("world",
                        new Create(),
                        new Delete(),
                        new dev.tylerm.khs.command.world.List(),
                        new Tp()
                ),
                new SetExitLocation(),
                new Top(),
                new Wins(),
                new Confirm()
        );

        getLogger().info("Loading game...");
        game = new Game(null, board);

        getLogger().info("Scheduling tick tasks...");
        getServer().getScheduler().runTaskTimer(this, this::onTick, 0, 1).getTaskId();

        getLogger().info("Registering outgoing bungeecord plugin channel...");
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getLogger().info("Checking for PlaceholderAPI...");
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI found...");
            getLogger().info("Registering PlaceholderAPI expansion...");
            new PAPIExpansion().register();
        }
        Bukkit.getPluginManager().registerEvents(new BlockPickerGUI.GuiHandler(), Main.getInstance());
        Bukkit.getPluginManager().registerEvents(new SkillSelectionGUI.InventoryHandler(), Main.getInstance());
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        long end = System.currentTimeMillis();
        if (!Bukkit.getServer().getAllowFlight()) {
            getLogger().severe("Allow flight is set to false. Players may be kicked if they stand on solidifed hiders.");
        }
        getLogger().info("Finished loading plugin (" + (end - start) + "ms)");
        loaded = true;
        new AnnounceBlockTypeTask().runTaskTimer(this, 0L, 60*20L);
    }

    public Map<Integer, UUID> getEntityIdToUUID() {
        return entityIdToUUID;
    }

    private List<HiderSkill> loadSkills() {
        var list = new ArrayList<HiderSkill>();
        // 动力小子
        {
            var spdyPotion = ItemStacks.builder(Material.SPLASH_POTION)
                    .customModelId(CustomItems.SPEED_POTION)
                    .displayName("&c肾上腺素")
                    .lore("&f速度 III 8s")
                    .build();
            spdyPotion.setAmount(2);
            var display = ItemStacks.of(
                    Material.SPLASH_POTION,
                    "&a动力小子",
                    "&c肾上腺素 &fx2 &8[速度 III 8s]"
            );
            list.add(new HiderSkill(display, List.of(spdyPotion)));
        }
        {
            var windCharge = new ItemStack(Material.WIND_CHARGE);
            windCharge.setAmount(2);
            var display = ItemStacks.of(
                    Material.WIND_CHARGE,
                    "&b弹弓",
                    "&f风弹 x2",
                    "&a砸中别人会很痛。"
            );
            list.add(new HiderSkill(display, List.of(windCharge)));
        }
        {
            var blockChanger = ItemStacks.builder(Material.BLAZE_ROD)
                    .customModelId(CustomItems.BLOCK_CHANGER)
                    .displayName("&d换装魔杖")
                    .lore("&a和在场的某个方块交换类型")
                    .build();
            var display = ItemStacks.of(
                    Material.BLAZE_ROD,
                    "&d换装秀",
                    "&f换装魔杖 x1",
                    "&a和在场的某个方块交换类型"
            );
            list.add(new HiderSkill(display, List.of(blockChanger)));
        }
        {
            var owl = ItemStacks.builder(Material.BOW)
                    .customModelId(CustomItems.OWL_BOW_MARKER)
                    .displayName("&b蜜鸟")
                    .lore("&a击中的生物将会留下印记。")
                    .build();
            var display = ItemStacks.of(
                    Material.BOW,
                    "&b蜜鸟",
                    "&f蜜鸟之弓 x1",
                    "&f箭矢 x8",
                    "&f射中目标时，在原地产生蜂鸣信号吸引其他人，不造成伤害。",
                    "&a蜜鸟通过发出鸣叫或其他信号，指引人类找到蜂巢。"
            );
            list.add(new HiderSkill(display, List.of(owl, new ItemStack(Material.ARROW, 8))));
        }
//        {
//            var seekerVisualizer = ItemStacks.builder(Material.SNOWBALL)
//                    .customModelId(CustomItems.SEEKER_VISUALIZER)
//                    .displayName("&e\"望眼欲穿\"")
//                    .lore("&f给所有在场猎人 3s 高亮，仅自己可见")
//                    .build();
//            seekerVisualizer.setAmount(2);
//            var display = ItemStacks.of(
//                    Material.SPYGLASS,
//                    "&e\"望眼欲穿\"",
//                    "&f看到所有在场猎人的位置 &8x2"
//            );
//            list.add(new HiderSkill(display, List.of(seekerVisualizer)));
//        }
        return list;
    }

    public void onDisable() {
        if (board != null) {
            board.getPlayers().forEach(player -> {
                board.removeBoard(player);
                PlayerLoader.unloadPlayer(player);
                exitPosition.teleport(player);
            });
            board.cleanup();
        }

        if (disguiser != null) {
            disguiser.cleanUp();
        }

        Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        PacketEvents.getAPI().terminate();
    }

    private void onTick() {
        if (game.getStatus() == Status.ENDED) game = new Game(game.getCurrentMap(), board);
        game.onTick();
        disguiser.check();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockedCommandHandler(), this);
        getServer().getPluginManager().registerEvents(new ChatHandler(), this);
        getServer().getPluginManager().registerEvents(new DamageHandler(), this);
        getServer().getPluginManager().registerEvents(new DisguiseHandler(), this);
        getServer().getPluginManager().registerEvents(new InteractHandler(), this);
        getServer().getPluginManager().registerEvents(new InventoryHandler(), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveHandler(), this);
        getServer().getPluginManager().registerEvents(new MovementHandler(), this);
        getServer().getPluginManager().registerEvents(new PlayerHandler(), this);
        getServer().getPluginManager().registerEvents(new RespawnHandler(), this);
        getServer().getPluginManager().registerEvents(new WorldInteractHandler(), this);
    }

    private void updateVersion() {
        Matcher matcher = Pattern.compile("MC: \\d\\.(\\d+).(\\d+)").matcher(Bukkit.getVersion());
        if (matcher.find()) {
            int version = Integer.parseInt(matcher.group(1));
            int sub_version = Integer.parseInt(matcher.group(2));
            getLogger().info("Identified server version: " + version);
            getLogger().info("Identified server sub version: " + sub_version);
            if (version < 18) {
                throw new IllegalStateException("Minecraft version below 1.18 is unsupported by this fork.");
            }
            return;
        }

        matcher = Pattern.compile("MC: \\d\\.(\\d+)").matcher(Bukkit.getVersion());
        if (matcher.find()) {
            int version = Integer.parseInt(matcher.group(1));
            getLogger().info("Identified server version: " + version);
            if (version < 18) {
                throw new IllegalStateException("Minecraft version below 1.18 is unsupported by this fork.");
            }
            return;
        }

        throw new IllegalArgumentException("Failed to parse server version from: " + Bukkit.getVersion());
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(errorPrefix + message("COMMAND_PLAYER_ONLY"));
            return true;
        }
        commandGroup.handleCommand((Player) sender, args);
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(errorPrefix + message("COMMAND_PLAYER_ONLY"));
            return new ArrayList<>();
        }
        return commandGroup.handleTabComplete((Player) sender, args);
    }

    public static Main getInstance() {
        return instance;
    }

    public File getWorldContainer() {
        return this.getServer().getWorldContainer();
    }

    public Database getDatabase() {
        return database;
    }

    public Board getBoard() {
        return board;
    }

    public Game getGame() {
        return game;
    }

    public Disguiser getDisguiser() {
        return disguiser;
    }

    public CommandGroup getCommandGroup() {
        return commandGroup;
    }

    public List<String> getWorlds() {
        List<String> worlds = new ArrayList<>();
        File[] containers = getWorldContainer().listFiles();
        if (containers != null) {
            Arrays.stream(containers).forEach(file -> {
                if (!file.isDirectory()) return;
                String[] files = file.list();
                if (files == null) return;
                if (!Arrays.asList(files).contains("session.lock") && !Arrays.asList(files).contains("level.dat"))
                    return;
                worlds.add(file.getName());
            });
        }
        return worlds;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void scheduleTask(Runnable task) {
        if (!isEnabled()) return;
        Bukkit.getServer().getScheduler().runTask(this, task);
    }

    public List<HiderSkill> getHiderSkills() {
        return hiderSkills;
    }
}
