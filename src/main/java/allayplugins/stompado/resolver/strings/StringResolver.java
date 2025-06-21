package allayplugins.stompado.resolver.strings;

import allayplugins.stompado.resolver.ArgumentResolver;
import org.bukkit.command.CommandSender;

public class StringResolver implements ArgumentResolver<String> {

    @Override
    public String resolve(CommandSender sender, String arg) {
        return arg;
    }
}