package allayplugins.stompado.resolver;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface ArgumentResolver<T> {

    T resolve(CommandSender sender, String arg) throws IllegalArgumentException;

}