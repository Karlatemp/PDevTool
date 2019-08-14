package cn.mcres.karlatemp.pdevtool;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class PDevTool extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("PDevTool was enabled.");
        getLogger().info("Author: Karlatemp QQ: 3279826484.");
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
            }
        } catch (Throwable thr) {
            throw new CommandException(thr.getMessage(), thr);
        }
        return true;
    }
}
