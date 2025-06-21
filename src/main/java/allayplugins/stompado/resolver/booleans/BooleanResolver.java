package allayplugins.stompado.resolver.booleans;

import allayplugins.stompado.resolver.ArgumentResolver;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class BooleanResolver implements ArgumentResolver<Boolean> {

    private final List<String> trueValues = Arrays.asList("true", "yes", "on", "sim", "1");
    private final List<String> falseValues = Arrays.asList("false", "no", "off", "nao", "não", "0");

    @Override
    public Boolean resolve(CommandSender sender, String arg) throws IllegalArgumentException {
        String lower = arg.toLowerCase();

        if (trueValues.contains(lower)) return true;
        if (falseValues.contains(lower)) return false;

        throw new IllegalArgumentException("§cValor booleano inválido: " + arg + " (use sim/não, true/false)");
    }
}