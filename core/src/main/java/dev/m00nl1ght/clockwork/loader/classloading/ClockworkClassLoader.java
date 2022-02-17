package dev.m00nl1ght.clockwork.loader.classloading;

import dev.m00nl1ght.clockwork.core.ClockworkException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.module.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Modified version of {@link jdk.internal.loader.Loader}.
 */
public class ClockworkClassLoader extends SecureClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    // Cached reference to platform classloader.
    private final ClassLoader parent = ClassLoader.getPlatformClassLoader();

    // List of bytecode transformers to be applied.
    private final List<ClassTransformer> transformers;

    // Maps a module name to a module reference.
    private final Map<String, ModuleReference> nameToModule;

    // Maps package name to a module loaded by this class loader.
    private final Map<String, LoadedModule> localPackageToModule;

    // Maps package name to a remote class loader, populated post initialization.
    private final Map<String, ClassLoader> remotePackageToLoader = new ConcurrentHashMap<>();

    // Maps a module reference to a module reader, populated lazily.
    private final Map<ModuleReference, ModuleReader> moduleToReader = new ConcurrentHashMap<>();

    // AccessControlContext used when loading classes and resources.
    private final AccessControlContext acc;

    // ### Init ###

    public ClockworkClassLoader(@NotNull Configuration cf,
                                @NotNull List<@NotNull ModuleLayer> parentLayers,
                                @NotNull List<@NotNull ClassTransformer> transformers) {

        super(null);

        this.transformers = List.copyOf(Objects.requireNonNull(transformers));

        Map<String, ModuleReference> nameToModule = new HashMap<>();
        Map<String, LoadedModule> localPackageToModule = new HashMap<>();

        for (ResolvedModule resolvedModule : cf.modules()) {
            ModuleReference mref = resolvedModule.reference();
            ModuleDescriptor descriptor = mref.descriptor();
            nameToModule.put(descriptor.name(), mref);
            descriptor.packages().forEach(pn -> {
                LoadedModule lm = new LoadedModule(mref);
                if (localPackageToModule.put(pn, lm) != null)
                    throw new IllegalArgumentException("Package " + pn + " in more than one module");
            });
        }

        this.nameToModule = nameToModule;
        this.localPackageToModule = localPackageToModule;
        this.acc = AccessController.getContext();

        initRemotePackageMap(cf, parentLayers);
    }

    /**
     * Completes initialization of this Loader. This method populates
     * remotePackageToLoader with the packages of the remote modules, where
     * "remote modules" are the modules read by modules defined to this loader.
     *
     * @param cf the Configuration containing at least modules to be defined to this class loader
     *
     * @param parentModuleLayers the parent ModuleLayers
     */
    private void initRemotePackageMap(Configuration cf, List<ModuleLayer> parentModuleLayers) {

        for (String name : nameToModule.keySet()) {

            ResolvedModule resolvedModule = cf.findModule(name).orElseThrow();
            assert resolvedModule.configuration() == cf;

            for (ResolvedModule other : resolvedModule.reads()) {

                String mn = other.name();
                ClassLoader loader;

                if (other.configuration() == cf) {

                    // The module reads another module in the newly created
                    // layer. If all modules are defined to the same class
                    // loader then the packages are local.
                    assert nameToModule.containsKey(mn);
                    continue;

                } else {

                    // Find the layer for the target module.
                    ModuleLayer layer = parentModuleLayers.stream()
                            .map(parent -> findModuleLayer(parent, other.configuration()))
                            .flatMap(Optional::stream)
                            .findAny()
                            .orElseThrow(() -> new InternalError("Unable to find parent layer"));

                    // Find the class loader for the module.
                    // For now, we use the platform loader for modules defined to the boot loader.
                    assert layer.findModule(mn).isPresent();
                    loader = layer.findLoader(mn);
                    if (loader == null) loader = parent;
                }

                // Find the packages that are exported to the target module.
                ModuleDescriptor descriptor = other.reference().descriptor();
                if (descriptor.isAutomatic()) {
                    ClassLoader l = loader;
                    descriptor.packages().forEach(pn -> remotePackage(pn, l));
                } else {
                    String target = resolvedModule.name();
                    for (ModuleDescriptor.Exports e : descriptor.exports()) {
                        boolean delegate;
                        if (e.isQualified()) {
                            // Qualified export in same configuration
                            delegate = (other.configuration() == cf) && e.targets().contains(target);
                        } else {
                            // Unqualified
                            delegate = true;
                        }

                        if (delegate) {
                            remotePackage(e.source(), loader);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds to remotePackageToLoader so that an attempt to load a class in
     * the package delegates to the given class loader.
     *
     * @throws IllegalStateException
     *         if the package is already mapped to a different class loader
     */
    private void remotePackage(String pn, ClassLoader loader) {
        ClassLoader l = remotePackageToLoader.putIfAbsent(pn, loader);
        if (l != null && l != loader) {
            throw new IllegalStateException("Package " + pn + " cannot be imported from multiple loaders");
        }
    }

    /**
     * Find the layer corresponding to the given configuration in the tree
     * of layers rooted at the given origin.
     */
    private Optional<ModuleLayer> findModuleLayer(ModuleLayer origin, Configuration cf) {
        Set<ModuleLayer> visited = new HashSet<>();
        Deque<ModuleLayer> stack = new ArrayDeque<>();
        visited.add(origin);
        stack.push(origin);

        while (!stack.isEmpty()) {
            var layer = stack.pop();
            if (layer.configuration() == cf) return Optional.of(layer);
            for (var i = layer.parents().size() - 1; i >= 0; i--) {
                var parent = layer.parents().get(i);
                if (visited.add(parent)) {
                    stack.push(parent);
                }
            }
        }

        return Optional.empty();
    }

    // ### Resources ###

    /**
     * Returns a URL to a resource of the given name in a module defined to
     * this class loader.
     */
    @Override
    protected URL findResource(String mn, String name) throws IOException {
        ModuleReference mref = (mn != null) ? nameToModule.get(mn) : null;

        // Not defined to this class loader
        if (mref == null) return null;

        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<URL>) () -> getURL(mref, name));
        } catch (PrivilegedActionException pae) {
            throw (IOException) pae.getCause();
        }
    }

    private URL getURL(ModuleReference mref, String name) throws IOException {
        var ouri = moduleReaderFor(mref).find(name);
        if (ouri.isPresent()) {
            try {
                return ouri.get().toURL();
            } catch (MalformedURLException | IllegalArgumentException ignored) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public URL findResource(String name) {
        String pn = toPackageName(name);
        LoadedModule module = localPackageToModule.get(pn);

        if (module != null) {
            try {
                URL url = findResource(module.name(), name);
                if (url != null
                        && (name.endsWith(".class")
                        || url.toString().endsWith("/")
                        || isOpen(module.mref(), pn))) {
                    return url;
                }
            } catch (IOException ioe) {
                // Ignore
            }
        } else {
            for (ModuleReference mref : nameToModule.values()) {
                try {
                    URL url = findResource(mref.descriptor().name(), name);
                    if (url != null) return url;
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }

        return null;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return Collections.enumeration(findResourcesAsList(name));
    }

    @Override
    public URL getResource(String name) {
        Objects.requireNonNull(name);
        URL url = findResource(name);
        if (url == null) url = parent.getResource(name);
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);

        List<URL> urls = findResourcesAsList(name);
        Enumeration<URL> e = parent.getResources(name);

        // Concat the URLs with the URLs returned by the parent
        return new Enumeration<>() {
            final Iterator<URL> iterator = urls.iterator();

            @Override
            public boolean hasMoreElements() {
                return (iterator.hasNext() || e.hasMoreElements());
            }

            @Override
            public URL nextElement() {
                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    return e.nextElement();
                }
            }
        };
    }

    /**
     * Finds the resources with the given name in this class loader.
     */
    private List<URL> findResourcesAsList(String name) throws IOException {
        String pn = toPackageName(name);
        LoadedModule module = localPackageToModule.get(pn);
        if (module != null) {
            URL url = findResource(module.name(), name);
            if (url != null
                    && (name.endsWith(".class")
                    || url.toString().endsWith("/")
                    || isOpen(module.mref(), pn))) {
                return List.of(url);
            } else {
                return Collections.emptyList();
            }
        } else {
            List<URL> urls = new ArrayList<>();
            for (ModuleReference mref : nameToModule.values()) {
                URL url = findResource(mref.descriptor().name(), name);
                if (url != null) {
                    urls.add(url);
                }
            }
            return urls;
        }
    }

    /**
     * Derive a <em>package name</em> for a resource. The package name
     * returned by this method may not be a legal package name. This method
     * returns null if the resource name ends with a "/" (a directory)
     * or the resource name does not contain a "/".
     */
    private static String toPackageName(String name) {
        var index = name.lastIndexOf('/');
        if (index == -1 || index == name.length() - 1) {
            return "";
        } else {
            return name.substring(0, index).replace("/", ".");
        }
    }

    // ### Classloading ###

    /**
     * Finds the class with the specified binary name.
     */
    @Override
    protected Class<?> findClass(String cn) throws ClassNotFoundException {
        Class<?> c = null;
        LoadedModule loadedModule = findLoadedModule(cn);
        if (loadedModule != null)
            c = findClassInModuleOrNull(loadedModule, cn);
        if (c == null)
            throw new ClassNotFoundException(cn);
        return c;
    }

    /**
     * Finds the class with the specified binary name in the given module.
     * This method returns {@code null} if the class cannot be found.
     */
    @Override
    protected Class<?> findClass(String mn, String cn) {
        Class<?> c = null;
        LoadedModule loadedModule = findLoadedModule(cn);
        if (loadedModule != null && loadedModule.name().equals(mn))
            c = findClassInModuleOrNull(loadedModule, cn);
        return c;
    }

    /**
     * Loads the class with the specified binary name.
     */
    @Override
    protected Class<?> loadClass(String cn, boolean resolve) throws ClassNotFoundException {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            String pn = packageName(cn);
            if (!pn.isEmpty()) {
                sm.checkPackageAccess(pn);
            }
        }

        synchronized (getClassLoadingLock(cn)) {
            // Check if already loaded
            Class<?> c = findLoadedClass(cn);

            if (c == null) {

                LoadedModule loadedModule = findLoadedModule(cn);

                if (loadedModule != null) {

                    // Class is in module defined to this class loader
                    c = findClassInModuleOrNull(loadedModule, cn);

                } else {

                    // Type in another module or visible via the parent loader
                    String pn = packageName(cn);
                    ClassLoader loader = remotePackageToLoader.get(pn);

                    if (loader == null) {
                        c = parent.loadClass(cn);
                    } else {
                        c = loader.loadClass(cn);
                    }

                }
            }

            if (c == null) throw new ClassNotFoundException(cn);

            if (resolve) resolveClass(c);

            return c;
        }
    }

    /**
     * Finds the class with the specified binary name if in a module
     * defined to this ClassLoader.
     *
     * @return the resulting Class or {@code null} if not found
     */
    private Class<?> findClassInModuleOrNull(LoadedModule loadedModule, String cn) {
        PrivilegedAction<Class<?>> pa = () -> defineClass(cn, loadedModule);
        return AccessController.doPrivileged(pa, acc);
    }

    /**
     * Defines the given binary class name to the VM, loading the class
     * bytes from the given module.
     *
     * @return the resulting Class or {@code null} if an I/O error occurs
     */
    private Class<?> defineClass(String cn, LoadedModule loadedModule) {
        ModuleReader reader = moduleReaderFor(loadedModule.mref());
        try {

            String rn = cn.replace('.', '/').concat(".class");
            ByteBuffer bb = reader.read(rn).orElse(null);
            if (bb == null) return null;

            if (transformers.isEmpty()) {
                try {
                    return defineClass(cn, bb, loadedModule.codeSource());
                } finally {
                    reader.release(bb);
                }
            }

            byte[] tb = new byte[bb.remaining()];

            try {
                bb.get(tb);
            } finally {
                reader.release(bb);
            }

            byte[] transformed = transformClass(cn, tb);
            return defineClass(cn, transformed, 0, transformed.length, loadedModule.codeSource());

        } catch (IOException ioe) {
            return null;
        }
    }

    private byte[] transformClass(String cn, byte[] classBytes) {
        for (ClassTransformer transformer : transformers) {
            try {
                classBytes = transformer.transform(cn, classBytes);
            } catch (Throwable t) {
                throw ClockworkException.generic(t, "Exception in [] while transforming []", transformer, cn);
            }
        }
        return classBytes;
    }

    // ### Permissions ###

    /**
     * Returns the permissions for the given CodeSource.
     */
    @Override
    protected PermissionCollection getPermissions(CodeSource cs) {
        PermissionCollection perms = super.getPermissions(cs);

        URL url = cs.getLocation();
        if (url == null) return perms;

        // Add the permission to access the resource
        try {
            Permission p = url.openConnection().getPermission();
            if (p != null) {
                // For directories then need recursive access
                if (p instanceof FilePermission) {
                    String path = p.getName();
                    if (path.endsWith(File.separator)) {
                        path += "-";
                        p = new FilePermission(path, "read");
                    }
                }
                perms.add(p);
            }
        } catch (IOException ioe) { }

        return perms;
    }

    // ### Helpers ###

    /**
     * Find the candidate module for the given class name.
     * Returns {@code null} if none of the modules defined to this
     * class loader contain the API package for the class.
     */
    private LoadedModule findLoadedModule(String cn) {
        String pn = packageName(cn);
        return pn.isEmpty() ? null : localPackageToModule.get(pn);
    }

    /**
     * Returns the package name for the given class name.
     */
    private String packageName(String cn) {
        int pos = cn.lastIndexOf('.');
        return (pos < 0) ? "" : cn.substring(0, pos);
    }

    /**
     * Returns the ModuleReader for the given module.
     */
    private ModuleReader moduleReaderFor(ModuleReference mref) {
        return moduleToReader.computeIfAbsent(mref, m -> createModuleReader(mref));
    }

    /**
     * Creates a ModuleReader for the given module.
     */
    private ModuleReader createModuleReader(ModuleReference mref) {
        try {
            return mref.open();
        } catch (IOException e) {
            // Return a null module reader to avoid a future class load
            // attempting to open the module again.
            return new NullModuleReader();
        }
    }

    /**
     * A ModuleReader that doesn't read any resources.
     */
    private static class NullModuleReader implements ModuleReader {

        @Override
        public Optional<URI> find(String name) {
            return Optional.empty();
        }

        @Override
        public Stream<String> list() {
            return Stream.empty();
        }

        @Override
        public void close() {
            throw new InternalError("Should not get here");
        }

    }

    /**
     * Returns true if the given module opens the given package unconditionally.
     *
     * @implNote This method currently iterates over each of the open packages.
     * This will be replaced once the ModuleDescriptor.Opens API is updated.
     */
    private boolean isOpen(ModuleReference mref, String pn) {
        ModuleDescriptor descriptor = mref.descriptor();
        if (descriptor.isOpen() || descriptor.isAutomatic()) return true;
        for (ModuleDescriptor.Opens opens : descriptor.opens()) {
            String source = opens.source();
            if (!opens.isQualified() && source.equals(pn)) return true;
        }
        return false;
    }

    private static class LoadedModule {

        private final ModuleReference mref;
        private final @Nullable URL url;
        private final CodeSource cs;

        LoadedModule(ModuleReference mref) {
            URL url = null;
            if (mref.location().isPresent()) {
                try {
                    url = mref.location().get().toURL();
                } catch (MalformedURLException | IllegalArgumentException e) { }
            }

            this.mref = mref;
            this.url = url;
            this.cs = new CodeSource(url, (CodeSigner[]) null);
        }

        ModuleReference mref() { return mref; }
        String name() { return mref.descriptor().name(); }
        URL location() { return url; }
        CodeSource codeSource() { return cs; }

    }

}
