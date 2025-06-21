package allayplugins.stompado.method;

import allayplugins.stompado.command.CommandBase;

import java.lang.reflect.Method;

public class CommandMethod {

    public final Object instance;
    public final Method method;
    public final CommandBase annotation;

    public CommandMethod(Object instance, Method method, CommandBase annotation) {
        this.instance = instance;
        this.method = method;
        this.annotation = annotation;
    }
}