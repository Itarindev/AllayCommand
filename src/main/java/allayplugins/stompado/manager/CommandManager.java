package allayplugins.stompado.manager;

import allayplugins.stompado.commands.CommandBase;
import allayplugins.stompado.method.CommandMethod;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.*;

public class CommandManager {

    private final JavaPlugin plugin;
    private final Map<String, CommandMethod> commandsRegister = new HashMap<>();

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
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
                    Object[] parameters = buildParameters(commandMethod.method, sender, newArgs);
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

    private Object[] buildParameters(Method method, CommandSender sender, String[] args) throws IllegalArgumentException {
        List<Object> params = new ArrayList<>();
        int argIndex = 0;

        for (Class<?> type : method.getParameterTypes()) {
            if (type == CommandSender.class) {
                params.add(sender);
            } else if (type == Player.class) {
                if (sender instanceof Player) {
                    params.add((Player) sender);
                } else {
                    throw new IllegalArgumentException("O sender não é um jogador.");
                }
            } else if (type == String[].class) {
                params.add(args);
            } else if (type == String.class) {
                if (argIndex >= args.length)
                    throw new IllegalArgumentException("Faltando argumento para String");
                params.add(args[argIndex++]);
            } else {
                throw new IllegalArgumentException("Tipo de parâmetro não suportado: " + type.getSimpleName());
            }
        }

        return params.toArray();
    }
}