package net.tylermurphy.hideAndSeek.command;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;

import net.tylermurphy.hideAndSeek.game.Board;
import net.tylermurphy.hideAndSeek.game.Game;
import net.tylermurphy.hideAndSeek.util.Status;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.tylermurphy.hideAndSeek.configuration.Localization.*;

public class Leave implements ICommand {

	public void execute(CommandSender sender, String[] args) {
		if(Game.isNotSetup()) {
			sender.sendMessage(errorPrefix + message("GAME_SETUP"));
			return;
		}
		Player player = Bukkit.getServer().getPlayer(sender.getName());
		if(player == null) {
			sender.sendMessage(errorPrefix + message("COMMAND_ERROR"));
			return;
		}
		if(!Board.isPlayer(player)) {
			sender.sendMessage(errorPrefix + message("GAME_NOT_INGAME"));
			return;
		}
		if(announceMessagesToNonPlayers) Bukkit.broadcastMessage(messagePrefix + message("GAME_LEAVE").addPlayer(player));
		else Game.broadcastMessage(messagePrefix + message("GAME_LEAVE").addPlayer(player));
		Board.removeBoard(player);
		Board.remove(player);
		player.teleport(new Location(Bukkit.getWorld(exitWorld), exitPosition.getX(), exitPosition.getY(), exitPosition.getZ()));
		if(Game.status == Status.STANDBY) {
			Board.reloadLobbyBoards();
		} else {
			Board.reloadGameBoards();
			Board.reloadBoardTeams();
		}
	}

	public String getLabel() {
		return "leave";
	}

	public String getUsage() {
		return "";
	}

	public String getDescription() {
		return "Leaves the lobby if game is set to manual join/leave";
	}

}