package cn.mcres.karlatemp.pdevtool;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.event.*;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.PhantomReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class PDevTool extends JavaPlugin implements Listener, EventExecutor {
    public static final Map<ClassLoader, Object> pluginMap = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("PDevTool was enabled.");
        getLogger().info("Author: Karlatemp QQ: 3279826484.");
        for (Plugin p : getServer().getPluginManager().getPlugins()) load(p);
        PluginEnableEvent.getHandlerList().register(new RegisteredListener(this, this, EventPriority.MONITOR, this, false));
    }

    private static void load(Plugin p) {
        pluginMap.put(p.getClass().getClassLoader(), p);
    }

    private void show(Collection<String> a, CommandSender sender) {
        if (a.isEmpty()) {
            sender.sendMessage("\u00a76Invoking command was not checkup any permission.");
        } else {
            sender.sendMessage("\u00a7bThe executed command checks these permissions:");
            int col = 10;
            StringBuilder sb = new StringBuilder();
            for (String perm : a) {
                sb.append("\u00a7b").append(perm).append("\u00a76, ");
                if (col-- < 0) {
                    sender.sendMessage(sb.toString());
                    sb.delete(0, sb.length());
                    col = 10;
                }
            }
            if (sb.length() > 0) {
                sender.sendMessage(sb.toString());
            }
        }
    }

    private CommandSender getRealSender(CommandSender cs) {
        if (cs instanceof ProxiedCommandSender) {
            return getRealSender(((ProxiedCommandSender) cs).getCaller());
        }
        return cs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            String cline = String.join(" ", args);
            CommandSender perser = getRealSender(sender);
            Field fe = RefTool.getPermField(perser);
            PermissibleBase pb = (PermissibleBase) fe.get(perser);
            switch (command.getName().toLowerCase()) {
                case "plog": {
                    if (sender != perser) {
                        sender.sendMessage("\u00a7cThis command cannot run with /minecraft:execute");
                        return true;
                    }
                    if (pb instanceof PBBride) {
                        ((PBBride) pb).rt = sender;
                    } else {
                        PBBride pbg = new PBBride(sender, pb);
                        pbg.rt = sender;
                        fe.set(sender, pbg);
                    }
                    sender.sendMessage("§bEnable Permission real-time check mode.");
                    return true;
                }
                case "psudo": {
                    if (sender != perser) {
                        sender.sendMessage("\u00a7cThis command cannot run with /minecraft:execute");
                        return true;
                    }
                    if (args.length > 0) {
                        if (args[0].equals("-i")) {
                            if (pb instanceof PBBride) {
                                ((PBBride) pb).sudo = true;
                            } else {
                                PBBride pbg = new PBBride(sender, pb);
                                pbg.sudo = true;
                                fe.set(sender, pbg);
                            }
                            sender.sendMessage("\u00a7bWelcome to SUDO mode. Exit with §6/pexit");
                        } else {
                            if (pb instanceof PBBride) {
                                PBBride pbb = (PBBride) pb;
                                boolean set = !pbb.sudo;
                                if (set) {
                                    pbb.sudo = true;
                                }
                                try {
                                    boolean ref = Bukkit.dispatchCommand(sender, cline);
                                    if (!ref) {
                                        sender.sendMessage("\u00a7cFailed to invoke command: " + cline);
                                    }
                                } finally {
                                    if (set) {
                                        pbb.sudo = false;
                                    }
                                }
                            } else {
                                PBBride pbg = new PBBride(sender, pb);
                                pbg.sudo = true;
                                try {
                                    fe.set(sender, pbg);
                                    boolean ref = Bukkit.dispatchCommand(sender, cline);
                                    if (!ref) {
                                        sender.sendMessage("\u00a7cFailed to invoke command: " + cline);
                                    }
                                } finally {
                                    fe.set(perser, pb);
                                }
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
                case "pexit": {
                    if (sender != perser) {
                        sender.sendMessage("\u00a7cThis command cannot run with /minecraft:execute");
                        return true;
                    }
                    if (pb instanceof PBBride) {
                        PBBride pbb = (PBBride) pb;
                        if (!pbb.sudo) {
                            sender.sendMessage("\u00a7cYou are not in SUDO Mode.");
                        }
                        pbb.sudo = false;
                        if (pbb.rt != null) {
                            sender.sendMessage("§6Disable Permission real-time check mode.");
                            pbb.rt = null;
                        }
                        if (pbb.permissions != null) {
                            sender.sendMessage("\u00a7cI am very sorry, but it cannot be closed due to the permission record status, but we have already exited SUDO mode for you.");
                            return true;
                        }
                        fe.set(sender, pbb.getParent());
                        sender.sendMessage("\u00a7bSuccessfully exited SUDO mode.");
                    } else {
                        sender.sendMessage("\u00a7cI am very sorry, but you are not in sudo mode or permission record status.");
                    }
                    return true;
                }
                case "pshow": {
                    if (sender != perser) {
                        sender.sendMessage("\u00a7cThis command cannot run with /minecraft:execute");
                        return true;
                    }
                    if (args.length == 0) return false;
                    if (pb instanceof PBBride) {
                        PBBride pbb = (PBBride) pb;
                        if (pbb.permissions != null) {
                            sender.sendMessage("\u00a7cI am very sorry, but you have entered the permission record mode.");
                            return true;
                        }
                        LinkedHashSet<String> permissions = new LinkedHashSet<>();
                        pbb.permissions = permissions;
                        try {
                            boolean ref = Bukkit.dispatchCommand(sender, cline);
                            if (!ref) {
                                sender.sendMessage("\u00a7cFailed to invoke command: " + cline);
                            }
                        } finally {
                            pbb.permissions = null;
                            show(permissions, sender);

                        }
                    } else {
                        PBBride pbb = new PBBride(sender, pb);
                        LinkedHashSet<String> permissions = new LinkedHashSet<>();
                        pbb.permissions = permissions;
                        try {
                            fe.set(sender, pbb);
                            boolean ref = Bukkit.dispatchCommand(sender, cline);
                            if (!ref) {
                                sender.sendMessage("\u00a7cFailed to invoke command: " + cline);
                            }
                        } finally {
                            fe.set(sender, pb);
                            show(permissions, sender);
                        }
                    }
                    return true;
                }
                case "pinfo": {
                    if (args.length == 0) {
                        return false;
                    }
                    Command cmd = RefTool.getCommandMap(command).getCommand(args[0].toLowerCase());
                    if (cmd == null) {
                        sender.sendMessage("\u00a7cCannot found command: " + cmd);
                        return true;
                    }
                    sender.sendMessage("\u00a76========= " + cmd.getName() + " =========");
                    final List<String> aliases = cmd.getAliases();
                    if (aliases != null) {
                        sender.sendMessage("\u00a7bAliases: \u00a76" + aliases);
                    }
                    sender.sendMessage("\u00a7bLabel: \u00a76" + cmd.getLabel());
                    sender.sendMessage("\u00a7bDescription: \u00a76" + cmd.getDescription());
                    sender.sendMessage("\u00a7bPermission: \u00a76" + cmd.getPermission());
                    sender.sendMessage("\u00a7bUsage: \u00a76" + cmd.getUsage());
                    if (cmd instanceof PluginCommand) {
                        PluginCommand pc = (PluginCommand) cmd;
                        sender.sendMessage("\u00a7bOwner: " + pc.getPlugin());
                    }
                    sender.sendMessage("\u00a76========= " + cmd.getName() + " =========");
                }
                case "pclass": {
                    if (args.length == 0) return false;
                    final Class<?> clazz = Class.forName(String.join(" ", args));
                    sender.sendMessage(clazz + "'s owner is " + pluginMap.get(clazz.getClassLoader()));
                    break;
                }
                case "pevent": {
                    if (args.length == 0) return false;
                    final Class<?> search = Class.forName(String.join(" ", args));
                    if (Event.class.isAssignableFrom(search)) {
                        HandlerList list;
                        try {
                            list = (HandlerList) search.getMethod("getHandlerList").invoke(null);
                        } catch (Throwable thr) {
                            final Method met = search.getDeclaredMethod("getHandlerList");
                            met.setAccessible(true);
                            list = (HandlerList) met.invoke(null);
                        }
                        final RegisteredListener[] listeners = list.getRegisteredListeners();
                        EventPriority last = null;
                        sender.sendMessage(search + "'s event listeners:");
                        for (RegisteredListener listener : listeners) {
                            final EventPriority priority = listener.getPriority();
                            if (last != priority) {
                                sender.sendMessage("§6Priority: " + priority);
                                last = priority;
                            }
                            sender.sendMessage("§b  " + listener.getPlugin() + "§f$§6" + listener.getListener().getClass());
                        }
                    } else {
                        sender.sendMessage(search + " is not a event class.");
                    }
                }
            }
        } catch (ClassNotFoundException cnfe) {
            sender.sendMessage(cnfe.toString());
        } catch (Throwable thr) {
            sender.sendMessage("§cError: " + thr.toString() + "\n§cMore in console.");
            throw new CommandException(thr.getLocalizedMessage(), thr);
        }
        return true;
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        if (event instanceof PluginEnableEvent) {
            load(((PluginEnableEvent) event).getPlugin());
        }
    }
}
