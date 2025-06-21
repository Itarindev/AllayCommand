package allayplugins.stompado.resolver.numbers;

import allayplugins.stompado.resolver.ArgumentResolver;
import org.bukkit.command.CommandSender;

public class IntegerResolver implements ArgumentResolver<Integer> {

    @Override
    public Integer resolve(CommandSender sender, String arg) throws IllegalArgumentException {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("§cNúmero inválido: " + arg);
        }
    }
}