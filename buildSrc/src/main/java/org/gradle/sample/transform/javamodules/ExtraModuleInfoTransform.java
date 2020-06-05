package org.gradle.sample.transform.javamodules;

import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.jar.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

/**
 * An artifact transform that applies additional information to Jars without module information.
 * The transformation fails the build if a Jar does not contain information and no extra information
 * was defined for it. This way we make sure that all Jars are turned into modules.
 *
 * Edited by m00nl1ght to fix some issues with missing or misplaced jar manifest files causing NPEs.
 */
public abstract class ExtraModuleInfoTransform implements TransformAction<ExtraModuleInfoTransform.Parameter> {

    public static class Parameter implements TransformParameters, Serializable {
        private Map<String, ModuleInfo> moduleInfo = Collections.emptyMap();
        private Map<String, String> automaticModules = Collections.emptyMap();

        @Input
        public Map<String, ModuleInfo> getModuleInfo() {
            return moduleInfo;
        }

        @Input
        public Map<String, String> getAutomaticModules() {
            return automaticModules;
        }

        public void setModuleInfo(Map<String, ModuleInfo> moduleInfo) {
            this.moduleInfo = moduleInfo;
        }

        public void setAutomaticModules(Map<String, String> automaticModules) {
            this.automaticModules = automaticModules;
        }
    }

    @InputArtifact
    protected abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        Map<String, ModuleInfo> moduleInfo = getParameters().moduleInfo;
        Map<String, String> automaticModules = getParameters().automaticModules;
        File originalJar = getInputArtifact().get().getAsFile();
        String originalJarName = originalJar.getName();

        if (isModule(originalJar)) {
            outputs.file(originalJar);
        } else if (moduleInfo.containsKey(originalJarName)) {
            addModuleDescriptor(originalJar, getModuleJar(outputs, originalJar), moduleInfo.get(originalJarName));
        } else if (isAutoModule(originalJar)) {
            outputs.file(originalJar);
        } else if (automaticModules.containsKey(originalJarName)) {
            addAutomaticModuleName(originalJar,  getModuleJar(outputs, originalJar), automaticModules.get(originalJarName));
        } else {
            throw new RuntimeException("Not a module and no mapping defined: " + originalJarName);
        }
    }

    /**
     * Added by m00nl1ght
     *
     * Fixes Manifest not being found by JarInputStream.getManifest when it is not the first entry in the jar.
     */
    private static Manifest findManifest(File jar) {
        try (JarInputStream inputStream = new JarInputStream(new FileInputStream(jar))) {
            if (inputStream.getManifest() != null) return inputStream.getManifest();
            JarEntry jarEntry = inputStream.getNextJarEntry();
            while (jarEntry != null) {
                if (JarFile.MANIFEST_NAME.equalsIgnoreCase(jarEntry.getName())) {
                    final Manifest manifest = new Manifest();
                    manifest.read(inputStream);
                    return manifest;
                } else {
                    jarEntry = inputStream.getNextJarEntry();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private boolean isModule(File jar) {
        Pattern moduleInfoClassMrjarPath = Pattern.compile("META-INF/versions/\\d+/module-info.class");
        try (JarInputStream inputStream =  new JarInputStream(new FileInputStream(jar))) {
            boolean isMultiReleaseJar = containsMultiReleaseJarEntry(jar);
            ZipEntry next = inputStream.getNextEntry();
            while (next != null) {
                if ("module-info.class".equals(next.getName())) {
                    return true;
                }
                if (isMultiReleaseJar && moduleInfoClassMrjarPath.matcher(next.getName()).matches()) {
                    return true;
                }
                next = inputStream.getNextEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Edit by m00nl1ght
     * - added check manifest != null to avoid NullPointerException
     * - use findManifest instead of JarInputStream.getManifest
     */
    private boolean containsMultiReleaseJarEntry(File jar) {
        Manifest manifest = findManifest(jar);
        return manifest != null && Boolean.parseBoolean(manifest.getMainAttributes().getValue("Multi-Release"));
    }

    /**
     * Edit by m00nl1ght
     * - added check manifest != null to avoid NullPointerException
     * - use findManifest instead of JarInputStream.getManifest
     */
    private boolean isAutoModule(File jar) {
        final Manifest manifest = findManifest(jar);
        return manifest != null && manifest.getMainAttributes().getValue("Automatic-Module-Name") != null;
    }

    private File getModuleJar(TransformOutputs outputs, File originalJar) {
        return outputs.file(originalJar.getName().substring(0, originalJar.getName().lastIndexOf('.')) + "-module.jar");
    }

    /**
     * Edit by m00nl1ght
     * - create new manifest if none is present
     * - use findManifest instead of JarInputStream.getManifest
     */
    private static void addAutomaticModuleName(File originalJar, File moduleJar, String moduleName) {
        try (JarInputStream inputStream = new JarInputStream(new FileInputStream(originalJar))) {
            Manifest manifest = findManifest(originalJar);
            if (manifest == null) manifest = new Manifest();
            manifest.getMainAttributes().put(new Attributes.Name("Automatic-Module-Name"), moduleName);
            try (JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(moduleJar), manifest)) {
                copyEntries(inputStream, outputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Edit by m00nl1ght
     * - use findManifest instead of JarInputStream.getManifest
     * - fix NPE when no manifest is present
     */
    private static void addModuleDescriptor(File originalJar, File moduleJar, ModuleInfo moduleInfo) {
        try (JarInputStream inputStream = new JarInputStream(new FileInputStream(originalJar))) {
            final Manifest manifest = findManifest(originalJar);
            try (JarOutputStream outputStream = manifest == null ?
                    new JarOutputStream(new FileOutputStream(moduleJar)) :
                    new JarOutputStream(new FileOutputStream(moduleJar), manifest)) {
                copyEntries(inputStream, outputStream);
                outputStream.putNextEntry(new JarEntry("module-info.class"));
                outputStream.write(addModuleInfo(moduleInfo));
                outputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Edit by m00nl1ght
     * - ignore manifest file, in case it was not the first entry in the jar file
     */
    private static void copyEntries(JarInputStream inputStream, JarOutputStream outputStream) throws IOException {
        JarEntry jarEntry = inputStream.getNextJarEntry();
        while (jarEntry != null) {
            if (!JarFile.MANIFEST_NAME.equalsIgnoreCase(jarEntry.getName())) {
                outputStream.putNextEntry(jarEntry);
                outputStream.write(inputStream.readAllBytes());
                outputStream.closeEntry();
            }
            jarEntry = inputStream.getNextJarEntry();
        }
    }

    private static byte[] addModuleInfo(ModuleInfo moduleInfo) {
        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
        ModuleVisitor moduleVisitor = classWriter.visitModule(moduleInfo.getModuleName(), Opcodes.ACC_OPEN, moduleInfo.getModuleVersion());
        for (String packageName : moduleInfo.getExports()) {
            moduleVisitor.visitExport(packageName.replace('.', '/'), 0);
        }
        moduleVisitor.visitRequire("java.base", 0, null);
        for (String requireName : moduleInfo.getRequires()) {
            moduleVisitor.visitRequire(requireName, 0, null);
        }
        for (String requireName : moduleInfo.getRequiresTransitive()) {
            moduleVisitor.visitRequire(requireName, Opcodes.ACC_TRANSITIVE, null);
        }
        moduleVisitor.visitEnd();
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
}
