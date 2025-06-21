package allayplugins.stompado.resolver.command;

import allayplugins.stompado.annotations.Default;
import allayplugins.stompado.resolver.ArgumentResolver;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandParameterResolver {

    private final Map<Class<?>, ArgumentResolver<?>> resolvers;

    public CommandParameterResolver(Map<Class<?>, ArgumentResolver<?>> resolvers) {
        this.resolvers = resolvers;
    }

    public Object[] buildParameters(Method method, CommandSender sender, String[] args) {
        Parameter[] parameters = method.getParameters();
        List<Object> result = new ArrayList<>();
        int argIndex = 0;

        for (Parameter param : parameters) {
            Class<?> type = param.getType();

            if (type == CommandSender.class) {
                result.add(sender);
                continue;
            }

            ArgumentResolver<?> resolver = resolvers.get(type);
            if (resolver == null) {
                throw new IllegalArgumentException("Sem resolver para tipo: " + type.getSimpleName());
            }

            String arg = (argIndex < args.length) ? args[argIndex] : null;

            if (arg == null) {
                if (param.isAnnotationPresent(Default.class)) {
                    arg = param.getAnnotation(Default.class).value();
                } else {
                    throw new IllegalArgumentException("Faltando argumento obrigatório: " + param.getName());
                }
            }

            Object resolved = resolver.resolve(sender, arg);
            result.add(resolved);
            argIndex++;
        }

        return result.toArray();
    }
}