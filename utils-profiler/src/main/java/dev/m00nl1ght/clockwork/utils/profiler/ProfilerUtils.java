package dev.m00nl1ght.clockwork.utils.profiler;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.stream.Collectors;

public class ProfilerUtils {

    public static String printProfilerInfo(@NotNull ProfilerGroup group) {
        final var builder = new StringBuilder();
        builder.append("########## Debug Profiler ##########\n");
        printProfilerInfo(builder, group, 0);
        builder.append("####################################");
        return builder.toString();
    }

    private static void printProfilerInfo(@NotNull StringBuilder builder, @NotNull ProfilerGroup group, int ind) {
        builder.append("  ".repeat(ind)).append('[').append(group.getName()).append(']').append('\n');
        for (var e : group.getEntries()) printProfilerInfo(builder, e, ind + 1);
        for (var g : group.getSubgroups()) printProfilerInfo(builder, g, ind + 1);
    }

    private static void printProfilerInfo(@NotNull StringBuilder builder, @NotNull ProfilerEntry entry, int ind) {
        builder.append("  ".repeat(ind));
        builder.append(entry.getName()).append(" (").append(entry.getCount()).append(") -> ").append(entry.getAverage());
        builder.append('\n');
    }

    public static void writeProfilerResultsToCSV(@NotNull ProfilerGroup profilerGroup, @NotNull File file) {
        final var header = !file.exists();
        try (final var writer = new BufferedWriter(new FileWriter(file, true))) {
            if (header) {
                writer.append(profilerGroup.getEntries().stream()
                        .map(ProfilerEntry::getName)
                        .collect(Collectors.joining(",")));
                writer.newLine();
            }
            writer.append(profilerGroup.getEntries().stream()
                    .map(p -> String.valueOf(p.getAverage()))
                    .collect(Collectors.joining(",")));
            writer.newLine();
        } catch (Exception e) {
            throw new RuntimeException("Failed to write CSV", e);
        }
    }

}
