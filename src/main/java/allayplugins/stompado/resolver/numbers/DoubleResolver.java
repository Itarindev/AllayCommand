package allayplugins.stompado.resolver.numbers;

import allayplugins.stompado.resolver.ArgumentResolver;
import org.bukkit.command.CommandSender;

public class DoubleResolver implements ArgumentResolver<Double> {

    @Override
    public Double resolve(CommandSender sender, String arg) throws IllegalArgumentException {
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("§cNúmero decimal inválido: " + arg);
        }
    }
}