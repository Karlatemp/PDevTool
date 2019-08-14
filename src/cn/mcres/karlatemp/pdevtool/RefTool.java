package cn.mcres.karlatemp.pdevtool;

//import org.bukkit.entity.Entity;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.permissions.Permissible;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RefTool {
    public static final String fname = "perm";
    private static final Map<Class, Field> cache = new HashMap<>();

    public static CommandMap getCommandMap(Command cmd) {
        try {
            return (CommandMap) getCommandMapField().get(cmd);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Field getCommandMapField() {
        Field f = cache.get(Command.class);
        if (f != null) return f;
        for (Field fx : Command.class.getDeclaredFields()) {
            if (CommandMap.class.isAssignableFrom(fx.getType())) {
                cache.put(Command.class, fx);
                fx.setAccessible(true);
                return fx;
            }
        }
        return null;
    }

    public static Field getPermField(Permissible pa) {
        Class c = pa.getClass();
        Class owner = null;
        try {
            final Method hp = c.getMethod("hasPermission", String.class);
            owner = hp.getDeclaringClass();
            Field cac = cache.get(owner);
            if (cac != null) {
                return cac;
            }
            Field f = owner.getDeclaredField(fname);
            f.setAccessible(true);
            cache.put(owner, f);
            return f;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (NoSuchFieldException ne) {
            throw new RuntimeException("Field not found in class: " + owner, ne);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
