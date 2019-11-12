package dev.m00nl1ght.clockwork.security.permissions;

import dev.m00nl1ght.clockwork.core.PluginContainer;

import java.io.File;
import java.io.FilePermission;
import java.security.Permission;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FilePermissionEntry implements PluginPermissionEntry {

    public static final Set<Action> ACTIONS_READ = Collections.unmodifiableSet(EnumSet.of(Action.READ));
    public static final Set<Action> ACTIONS_RWD = Collections.unmodifiableSet(EnumSet.of(Action.READ, Action.WRITE, Action.DELETE));
    public static final Set<Action> ACTIONS_RWDE = Collections.unmodifiableSet(EnumSet.of(Action.READ, Action.WRITE, Action.DELETE, Action.EXECUTE));
    public static final Set<Action> ACTIONS_ALL = Collections.unmodifiableSet(EnumSet.of(Action.READ, Action.WRITE, Action.DELETE, Action.EXECUTE, Action.READLINK));

    private final String defName;
    private final File basePath;
    private final String actions;

    public FilePermissionEntry(String defName, File basePath, Set<Action> actions) {
        this.defName = defName;
        this.basePath = basePath;
        this.actions = actions.stream().map(Enum::name).collect(Collectors.joining(",")).toLowerCase();
    }

    public FilePermissionEntry(String defName, Set<Action> actions) {
        this(defName, null, actions);
    }

    public FilePermissionEntry(File basePath, Set<Action> actions) {
        this(null, basePath, actions);
    }

    public FilePermissionEntry(Set<Action> actions) {
        this(null, null, actions);
    }

    @Override
    public void getPermissions(Consumer<Permission> permissions, PluginContainer plugin, String value) {
        if (basePath == null) {
            if (value.isEmpty()) value = "<<ALL FILES>>";
            permissions.accept(new FilePermission(value, actions));
        } else {
            final var base = basePath.getAbsolutePath().replace("$plugin-id$", plugin.getId());
            if (value.isEmpty()) {
                permissions.accept(new FilePermission(base, actions));
                permissions.accept(new FilePermission(base + File.separator + "-", actions));
            } else {
                permissions.accept(new FilePermission(base + File.separator + value, actions));
            }
        }
    }

    @Override
    public String getDefName() {
        return defName;
    }

    public enum Action {
        READ, WRITE, EXECUTE, DELETE, READLINK
    }

}
