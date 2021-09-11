package dev.m00nl1ght.clockwork.utils.profiler;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ProfilerUtils {

    public static String printProfilerInfo(@NotNull ProfilerGroup group) {
        final var builder = new StringBuilder();
        printProfilerInfo(builder, Objects.requireNonNull(group), 0);
        return builder.toString();
    }

    private static void printProfilerInfo(@NotNull StringBuilder builder, @NotNull ProfilerGroup group, int ind) {
        builder.append("  ".repeat(ind)).append('[').append(group).append(']').append('\n');
        for (var e : group.getEntries()) printProfilerInfo(builder, e, ind + 1);
        for (var g : group.getSubgroups()) printProfilerInfo(builder, g, ind + 1);
    }

    private static void printProfilerInfo(@NotNull StringBuilder builder, @NotNull ProfilerEntry entry, int ind) {
        builder.append("  ".repeat(ind));
        builder.append(entry);
        builder.append('\n');
    }

}
