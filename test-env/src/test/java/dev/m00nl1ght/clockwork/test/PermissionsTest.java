package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.event.EventDispatcher;
import dev.m00nl1ght.clockwork.event.impl.bus.EventBusImpl;
import dev.m00nl1ght.clockwork.extension.annotations.CWLAnnotationsExtension;
import dev.m00nl1ght.clockwork.extension.security.CWLSecurityExtension;
import dev.m00nl1ght.clockwork.extension.security.SecurityConfig;
import dev.m00nl1ght.clockwork.test.env.TestEnvironment;
import dev.m00nl1ght.clockwork.test.env.security.PermissionTestEvent;
import dev.m00nl1ght.clockwork.test.env.security.TestDynamicPermissionFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FilePermission;
import java.security.Permission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermissionsTest extends ClockworkTest {

    private EventBusImpl eventBus;
    private EventDispatcher<PermissionTestEvent, ClockworkCore> dispatcher;

    @Override
    protected void setupComplete() {
        CWLSecurityExtension.install(true);
        CWLSecurityExtension.registerContext(core(), buildSecurityConfig());
        eventBus = new EventBusImpl();
        CWLAnnotationsExtension.applyToEventBus(core(), eventBus);
        dispatcher = eventBus.getEventDispatcher(PermissionTestEvent.class, core().getCoreTargetType());
    }

    private SecurityConfig buildSecurityConfig() {
        return SecurityConfig.builder()
                .addSharedPermissions(filePerms(TestEnvironment.PLUGIN_SHARED_DIR))
                .addDeclarablePermission("protected-data-a", filePerms(TestEnvironment.PLUGIN_PROTECTED_DIR_A), true)
                .addDeclarablePermission("protected-data-b", filePerms(TestEnvironment.PLUGIN_PROTECTED_DIR_B), true)
                .addDeclarablePermission("plugin-data", new TestDynamicPermissionFactory())
                .build();
    }

    private Set<Permission> filePerms(File dir) {
        return Set.of(
                new FilePermission(dir.getAbsolutePath(), "read,write,delete"),
                new FilePermission(dir.getAbsolutePath() + File.separator + "-", "read,write,delete")
        );
    }

    @Test
    public void writeFileSharedGranted() {
        final var file = new File(TestEnvironment.PLUGIN_SHARED_DIR, "test.txt");
        final var event = new PermissionTestEvent(file);
        dispatcher.post(core(), event);
        assertTrue(file.isFile());
    }

    @Test
    public void writeFileSharedDenied() {
        final var file = new File(TestEnvironment.ENV_DIR, "test.txt");
        final var event = new PermissionTestEvent(file);
        final var exception = assertThrows(ExceptionInPlugin.class, () -> dispatcher.post(core(), event));
        assertTrue(exception.getCause() instanceof SecurityException);
    }

    @Test
    public void writeFileDeclaredGranted() {
        final var file = new File(TestEnvironment.PLUGIN_PROTECTED_DIR_A, "test.txt");
        final var event = new PermissionTestEvent(file);
        dispatcher.post(core(), event);
        assertTrue(file.isFile());
    }

    @Test
    public void writeFileDeclaredDenied() {
        final var file = new File(TestEnvironment.PLUGIN_PROTECTED_DIR_B, "test.txt");
        final var event = new PermissionTestEvent(file);
        final var exception = assertThrows(ExceptionInPlugin.class, () -> dispatcher.post(core(), event));
        assertTrue(exception.getCause() instanceof SecurityException);
    }

    @Test
    public void writeFileDynamicGranted() {
        final var file = new File(TestEnvironment.PLUGIN_DATA_DIR, "test-plugin-a/test.txt");
        final var event = new PermissionTestEvent(file);
        dispatcher.post(core(), event);
        assertTrue(file.isFile());
    }

    @Test
    public void writeFileDynamicDenied() {
        final var file = new File(TestEnvironment.PLUGIN_DATA_DIR, "test-plugin-b/test.txt");
        final var event = new PermissionTestEvent(file);
        final var exception = assertThrows(ExceptionInPlugin.class, () -> dispatcher.post(core(), event));
        assertTrue(exception.getCause() instanceof SecurityException);
    }

}
