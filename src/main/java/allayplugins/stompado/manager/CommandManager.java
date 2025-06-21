package allayplugins.stompado.manager;

import allayplugins.stompado.command.CommandBase;
import allayplugins.stompado.resolver.ArgumentResolver;
import allayplugins.stompado.method.CommandMethod;
import allayplugins.stompado.resolver.booleans.BooleanResolver;
import allayplugins.stompado.resolver.command.CommandParameterResolver;
import allayplugins.stompado.resolver.numbers.DoubleResolver;
import allayplugins.stompado.resolver.numbers.IntegerResolver;
import allayplugins.stompado.resolver.player.PlayerResolver;
import allayplugins.stompado.resolver.strings.StringResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.*;

public class CommandManager {

    private final JavaPlugin plugin;
    private final Map<String, CommandMethod> commandsRegister = new HashMap<>();

    private final Map<Class<?>, ArgumentResolver<?>> resolvers = new HashMap<>();

    private final CommandParameterResolver parameterResolver;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
        registerResolvers();
        parameterResolver = new CommandParameterResolver(resolvers);
    }

    public void registerCommands(Object... objects) {
        for (Object obj : objects) {
            for (Method method : obj.getClass().getDeclaredMethods()) {
                if (!method.isAnnotationPresent(CommandBase.class)) continue;

                CommandBase annotation = method.getAnnotation(CommandBase.class);
                String name = annotation.name().toLowerCase().trim();

                CommandMethod commandMethod = new CommandMethod(obj, method, annotation);
                commandsRegister.put(name, commandMethod);

                for (String alias : annotation.aliases()) {
                    commandsRegister.put(alias.toLowerCase().trim(), commandMethod);
                }
            }
        }

        Set<String> rootCommands = new HashSet<>();
        for (String cmd : commandsRegister.keySet()) {
            String root = cmd.split(" ")[0];
            rootCommands.add(root);
        }

        for (String root : rootCommands) {
            PluginCommand pluginCommand = plugin.getCommand(root);
            if (pluginCommand == null) {
                plugin.getLogger().warning("Comando raiz não registrado no plugin.yml: " + root);
                continue;
            }

            pluginCommand.setExecutor((sender, command, label, args) -> {
                CommandMethod commandMethod = null;
                int argsUsed = 0;

                for (int i = args.length; i >= 0; i--) {
                    String key = buildKey(label, args, i);
                    if (commandsRegister.containsKey(key)) {
                        commandMethod = commandsRegister.get(key);
                        argsUsed = i;
                        break;
                    }
                }

                if (commandMethod == null) {
                    sender.sendMessage("§c[ ! ] Comando não encontrado. Use /" + label + " help para ajuda.");
                    return true;
                }

                if (!hasPermission(sender, commandMethod)) return true;
                if (!isAllowedConsole(sender, commandMethod)) return true;

                try {
                    String[] newArgs = Arrays.copyOfRange(args, argsUsed, args.length);
                    Object[] parameters = parameterResolver.buildParameters(commandMethod.method, sender, newArgs);
                    commandMethod.method.invoke(commandMethod.instance, parameters);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§c[ ! ] Erro nos parâmetros: " + e.getMessage());
                } catch (Exception e) {
                    plugin.getLogger().severe("Erro ao executar comando '" + label + "': " + e.getMessage());
                    e.printStackTrace();
                    sender.sendMessage("§c[ ! ] Ocorreu um erro interno ao executar o comando.");
                }

                return true;
            });
        }
    }

    private String buildKey(String label, String[] args, int depth) {
        StringBuilder key = new StringBuilder(label.toLowerCase());
        for (int i = 0; i < depth; i++) {
            key.append(" ").append(args[i].toLowerCase());
        }
        return key.toString().trim();
    }

    private boolean hasPermission(CommandSender sender, CommandMethod commandMethod) {
        String perm = commandMethod.annotation.permission();
        if (!perm.isEmpty() && !sender.hasPermission(perm)) {
            sender.sendMessage("§c[ ! ] Você não tem permissão para isso.");
            return false;
        }
        return true;
    }

    private boolean isAllowedConsole(CommandSender sender, CommandMethod commandMethod) {
        if (!commandMethod.annotation.allowedConsole() && !(sender instanceof Player)) {
            sender.sendMessage("§c[ ! ] Esse comando só pode ser usado por jogadores.");
            return false;
        }
        return true;
    }

    private void registerResolvers() {
        resolvers.put(String.class, new StringResolver());

        resolvers.put(boolean.class, new BooleanResolver());
        resolvers.put(Boolean.class, new BooleanResolver());

        resolvers.put(int.class, new IntegerResolver());
        resolvers.put(Integer.class, new IntegerResolver());

        resolvers.put(double.class, new DoubleResolver());
        resolvers.put(Double.class, new DoubleResolver());

        resolvers.put(Player.class, new PlayerResolver());
    }
}