package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.core.ClockworkCore;

import java.security.Permission;

public class ClockworkSecurityManager extends SecurityManager {

    private final ClockworkCore core;

    public ClockworkSecurityManager(ClockworkCore core) {
        this.core = core;
    }

    // TODO implement this maybe at some point?

    @Override
    public void checkPermission(Permission perm) {
        super.checkPermission(perm);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        super.checkPermission(perm, context);
    }

}
