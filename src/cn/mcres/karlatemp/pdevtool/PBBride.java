package cn.mcres.karlatemp.pdevtool;

import org.bukkit.permissions.*;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class PBBride extends PermissibleBase {
    private final PermissibleBase parent;
    public boolean sudo = false;
    public Collection<String> permissions;

    public PermissibleBase getParent() {
        return parent;
    }

    public PBBride(ServerOperator opable, PermissibleBase parent) {
        super(opable);
        this.parent = parent;
    }

    @Override
    public boolean isOp() {
        return sudo || super.isOp();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return parent.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return parent.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String inName) {
        if (permissions != null && inName != null) {
            permissions.add(inName);
        }
        return sudo || parent.hasPermission(inName);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        if (permissions != null && perm != null) {
            permissions.add(perm.getName().toLowerCase());
        }
        return sudo || parent.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        if (parent == null) return null;
        return parent.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        if (parent == null) return null;
        return parent.addAttachment(plugin);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        if (parent != null)
            parent.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        if (parent != null)
            parent.recalculatePermissions();
    }

    @Override
    public synchronized void clearPermissions() {
        if (parent != null)
            parent.clearPermissions();
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        if (parent == null) return null;
        return parent.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        if (parent == null) return null;
        return parent.addAttachment(plugin, ticks);
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        if (parent == null) return Collections.EMPTY_SET;
        return parent.getEffectivePermissions();
    }
}
