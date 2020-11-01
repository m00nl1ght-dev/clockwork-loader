package dev.m00nl1ght.clockwork.test.env.security;

import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;

import java.io.File;

public class PermissionTestEvent extends ContextAwareEvent {

    private final File testFile;

    public PermissionTestEvent(File testFile) {
        this.testFile = testFile;
    }

    public File getTestFile() {
        return testFile;
    }

}
