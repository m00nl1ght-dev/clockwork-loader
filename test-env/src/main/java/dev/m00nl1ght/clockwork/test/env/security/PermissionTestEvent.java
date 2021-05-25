package dev.m00nl1ght.clockwork.test.env.security;

import dev.m00nl1ght.clockwork.event.impl.event.EventWithContext;

import java.io.File;

public class PermissionTestEvent extends EventWithContext {

    private final File testFile;

    public PermissionTestEvent(File testFile) {
        this.testFile = testFile;
    }

    public File getTestFile() {
        return testFile;
    }

}
