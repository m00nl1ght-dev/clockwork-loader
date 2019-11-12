package dev.m00nl1ght.clockwork.security.permissions;

import dev.m00nl1ght.clockwork.core.PluginContainer;

import java.security.Permission;
import java.util.Collections;
import java.util.EnumSet;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PropertyPermissionEntry implements PluginPermissionEntry {

    public static final Set<Action> ACTIONS_READ = Collections.unmodifiableSet(EnumSet.of(Action.READ));
    public static final Set<Action> ACTIONS_WRITE = Collections.unmodifiableSet(EnumSet.of(Action.WRITE));
    public static final Set<Action> ACTIONS_ALL = Collections.unmodifiableSet(EnumSet.of(Action.READ, Action.WRITE));

    private final String defName;
    private final String actions;

    public PropertyPermissionEntry(String defName, Set<Action> actions) {
        this.defName = defName;
        this.actions = actions.stream().map(Enum::name).collect(Collectors.joining(",")).toLowerCase();
    }

    public PropertyPermissionEntry(Set<Action> actions) {
        this(null, actions);
    }

    @Override
    public void getPermissions(Consumer<Permission> permissions, PluginContainer plugin, String value) {
        if (value.isEmpty()) value = "*";
        permissions.accept(new PropertyPermission(value, actions));
    }

    @Override
    public String getDefName() {
        return defName;
    }

    public enum Action {
        READ, WRITE
    }

}
