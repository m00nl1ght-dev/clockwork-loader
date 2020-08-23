package dev.m00nl1ght.clockwork.security.permissions;

import dev.m00nl1ght.clockwork.core.LoadedPlugin;

import java.net.SocketPermission;
import java.security.Permission;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NetworkPermissionEntry implements PluginPermissionEntry {

    public static final Set<Action> ACTIONS_RESOLVE = Collections.unmodifiableSet(EnumSet.of(Action.RESOLVE));
    public static final Set<Action> ACTIONS_CONNECT = Collections.unmodifiableSet(EnumSet.of(Action.CONNECT));
    public static final Set<Action> ACTIONS_CONNECT_ACCEPT = Collections.unmodifiableSet(EnumSet.of(Action.CONNECT, Action.ACCEPT));
    public static final Set<Action> ACTIONS_ALL = Collections.unmodifiableSet(EnumSet.of(Action.CONNECT, Action.ACCEPT, Action.LISTEN));

    private final String defName;
    private final String portRange;
    private final String actions;

    public NetworkPermissionEntry(String defName, String portRange, Set<Action> actions) {
        this.defName = defName;
        this.portRange = portRange;
        this.actions = actions.stream().map(Enum::name).collect(Collectors.joining(",")).toLowerCase();
    }

    public NetworkPermissionEntry(String defName, Set<Action> actions) {
        this(defName, null, actions);
    }

    public NetworkPermissionEntry(Set<Action> actions) {
        this(null, null, actions);
    }

    @Override
    public void getPermissions(Consumer<Permission> permissions, LoadedPlugin plugin, String value) {
        if (value.isEmpty()) value = "*";
        permissions.accept(new SocketPermission(portRange == null ? value : value + ":" + portRange, actions));
    }

    @Override
    public String getDefName() {
        return defName;
    }

    public enum Action {
        CONNECT, LISTEN, ACCEPT, RESOLVE
    }

}
