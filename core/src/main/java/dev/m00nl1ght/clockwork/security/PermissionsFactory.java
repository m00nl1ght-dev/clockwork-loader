package dev.m00nl1ght.clockwork.security;

import java.security.Permission;
import java.util.Set;

public interface PermissionsFactory {

    Set<Permission> buildPermission(String params);

}
