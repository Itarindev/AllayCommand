package allayplugins.stompado.resolver.player;

import allayplugins.stompado.resolver.ArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerResolver implements ArgumentResolver<Player> {

    @Override
    public Player resolve(CommandSender sender, String arg) throws IllegalArgumentException {
        Player player = Bukkit.getPlayer(arg);
        if (player == null) {
            throw new IllegalArgumentException("§cJogador não encontrado: " + arg);
        }
        return player;
    }
}