package dev.m00nl1ght.clockwork.classloading;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;
import dev.m00nl1ght.clockwork.security.ClockworkSecurityPolicy;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Modified version of jdk.internal.loader.Loader
 */
public class PluginClassloader extends SecureClassLoader {

    // TODO optimize/rework permission system, hopefully then no custom classloader will be needed anymore

    static {
        ClassLoader.registerAsParallelCapable();
    }

    // manager that created this loader
    private final ModuleManager manager;

    // parent ClassLoader
    private final ClassLoader parent;

    // maps a module name to a module reference
    private final Map<String, ModuleReference> nameToModule;

    // maps package name to a module loaded by this class loader
    private final Map<String, LoadedModule> localPackageToModule;

    // maps package name to a remote class loader, populated post initialization
    private final Map<String, ClassLoader> remotePackageToLoader = new ConcurrentHashMap<>();

    // maps a module reference to a module reader, populated lazily
    private final Map<ModuleReference, ModuleReader> moduleToReader = new ConcurrentHashMap<>();

    // maps a code source to a plugin container, populated externally via bindPlugin
    private final Map<URL, PluginDescriptor> codeSourceToPlugin = new HashMap<>();

    // ACC used when loading classes and resources
    private final AccessControlContext acc;

    /**
     * Creates a {@code PluginClassloader} that loads classes/resources from a collection
     * of modules.
     *
     * @throws IllegalArgumentException If two or more modules have the same package
     */
    public PluginClassloader(Collection<ResolvedModule> modules, ClassLoader parent, ModuleManager manager) {
        super(parent);
        this.parent = Objects.requireNonNull(parent);
        this.manager = Objects.requireNonNull(manager);

        Map<String, ModuleReference> nameToModule = new HashMap<>();
        Map<String, LoadedModule> localPackageToModule = new HashMap<>();
        for (var resolvedModule : modules) {
            var mref = resolvedModule.reference();
            var descriptor = mref.descriptor();
            nameToModule.put(descriptor.name(), mref);
            descriptor.packages().forEach(pn -> {
                var lm = new LoadedModule(mref);
                if (localPackageToModule.put(pn, lm) != null)
                    throw new IllegalArgumentException("Package " + pn + " in more than one module");
            });
        }

        this.nameToModule = nameToModule;
        this.localPackageToModule = localPackageToModule;
        this.acc = AccessController.getContext();
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

    /**
     * Completes initialization of this Loader. This method populates
     * remotePackageToLoader with the packages of the remote modules, where
     * "remote modules" are the modules read by modules defined to this loader.
     *
     * @param cf                 the Configuration containing at least modules to be defined to
     *                           this class loader
     * @param parentModuleLayers the parent ModuleLayers
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public PluginClassloader initRemotePackageMap(Configuration cf, List<ModuleLayer> parentModuleLayers) {
        for (var name : nameToModule.keySet()) {
            var resolvedModule = cf.findModule(name).get();
            assert resolvedModule.configuration() == cf;

            for (var other : resolvedModule.reads()) {
                var mn = other.name();
                ClassLoader loader = null;

                if (other.configuration() == cf) {
                    assert nameToModule.containsKey(mn);
                } else {
                    // find the layer for the target module
                    var layer = parentModuleLayers.stream()
                            .map(parent -> findModuleLayer(parent, other.configuration()))
                            .flatMap(Optional::stream)
                            .findAny()
                            .orElseThrow(() -> new InternalError("Unable to find parent layer"));

                    // find the class loader for the module
                    // For now we use the platform loader for modules defined to the boot loader
                    assert layer.findModule(mn).isPresent();
                    loader = layer.findLoader(mn);
                    if (loader == null) loader = ClassLoader.getPlatformClassLoader();
                }

                // find the packages that are exported to the target module
                var descriptor = other.reference().descriptor();
                if (descriptor.isAutomatic()) {
                    var l = loader;
                    descriptor.packages().forEach(pn -> remotePackage(pn, l));
                } else {
                    var target = resolvedModule.name();
                    for (var e : descriptor.exports()) {
                        boolean delegate;
                        if (e.isQualified()) {
                            // qualified export in same configuration
                            delegate = (other.configuration() == cf) && e.targets().contains(target);
                        } else {
                            // unqualified
                            delegate = true;
                        }

                        if (delegate) {
                            remotePackage(e.source(), loader);
                        }
                    }
                }
            }
        }

        return this;
    }

    /**
     * Adds to remotePackageToLoader so that an attempt to load a class in
     * the package delegates to the given class loader.
     *
     * @throws IllegalStateException if the package is already mapped to a different class loader
     */
    private void remotePackage(String pn, ClassLoader loader) {
        var l = remotePackageToLoader.putIfAbsent(pn, loader);
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

    /**
     * Returns a URL to a resource of the given name in a module defined to
     * this class loader.
     */
    @Override
    protected URL findResource(String mn, String name) throws IOException {
        var mref = (mn != null) ? nameToModule.get(mn) : null;
        if (mref == null) return null; // not defined to this class loader

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
        var pn = toPackageName(name);
        var module = localPackageToModule.get(pn);

        if (module != null) {
            try {
                var url = findResource(module.name(), name);
                if (url != null && (name.endsWith(".class") || url.toString().endsWith("/") || isOpen(module.mref(), pn))) {
                    return url;
                }
            } catch (IOException ioe) {
                // ignore
            }
        } else {
            for (var mref : nameToModule.values()) {
                try {
                    var url = findResource(mref.descriptor().name(), name);
                    if (url != null) return url;
                } catch (IOException ioe) {
                    // ignore
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
        var url = findResource(name);
        if (url == null) url = parent.getResource(name);
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);

        var urls = findResourcesAsList(name);
        var e = parent.getResources(name);

        // concat the URLs with the URLs returned by the parent
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
        var pn = toPackageName(name);
        var module = localPackageToModule.get(pn);
        if (module != null) {
            var url = findResource(module.name(), name);
            if (url != null && (name.endsWith(".class") || url.toString().endsWith("/") || isOpen(module.mref(), pn))) {
                return List.of(url);
            } else {
                return Collections.emptyList();
            }
        } else {
            List<URL> urls = new ArrayList<>();
            for (var mref : nameToModule.values()) {
                var url = findResource(mref.descriptor().name(), name);
                if (url != null) urls.add(url);
            }
            return urls;
        }
    }

    /**
     * Finds the class with the specified binary name.
     */
    @Override
    protected Class<?> findClass(String cn) throws ClassNotFoundException {
        Class<?> c = null;
        var loadedModule = findLoadedModule(cn);
        if (loadedModule != null) c = findClassInModuleOrNull(loadedModule, cn);
        if (c == null) throw new ClassNotFoundException(cn);
        return c;
    }

    /**
     * Finds the class with the specified binary name in the given module.
     * This method returns {@code null} if the class cannot be found.
     */
    @Override
    protected Class<?> findClass(String mn, String cn) {
        var module = findLoadedModule(cn);
        if (module != null && module.name().equals(mn)) {
            return findClassInModuleOrNull(module, cn);
        } else {
            return null;
        }
    }

    /**
     * Loads the class with the specified binary name.
     */
    @Override
    protected Class<?> loadClass(String cn, boolean resolve) throws ClassNotFoundException {
        var sm = System.getSecurityManager();
        if (sm != null) {
            var pn = packageName(cn);
            if (!pn.isEmpty()) {
                sm.checkPackageAccess(pn);
            }
        }

        synchronized (getClassLoadingLock(cn)) {
            var c = findLoadedClass(cn); // check if already loaded
            if (c == null) {
                var loadedModule = findLoadedModule(cn);
                if (loadedModule != null) {
                    // class is in module defined to this class loader
                    c = findClassInModuleOrNull(loadedModule, cn);
                } else {
                    // type in another module or visible via the parent loader
                    var pn = packageName(cn);
                    var loader = remotePackageToLoader.get(pn);
                    if (loader == null) {
                        // type not in a module read by any of the modules
                        // defined to this loader, so delegate to parent
                        // class loader
                        loader = parent;
                    }

                    c = loader.loadClass(cn);
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
        var reader = moduleReaderFor(loadedModule.mref());

        try {
            // read class file
            var rn = cn.replace('.', '/').concat(".class");
            var bb = reader.read(rn).orElse(null);
            if (bb == null) return null; // class not found

            try {
                return defineClass(cn, bb, loadedModule.codeSource());
            } finally {
                reader.release(bb);
            }

        } catch (IOException ioe) {
            // TBD on how I/O errors should be propagated
            return null;
        }
    }

    /**
     * Returns the permissions for the given CodeSource.
     */
    @Override
    protected PermissionCollection getPermissions(CodeSource cs) {
        var perms = super.getPermissions(cs);
        var url = cs.getLocation();
        if (url == null) return perms;

        // add the permission to access the resource
        try {
            var perm = url.openConnection().getPermission();
            if (perm != null) {
                // for directories then need recursive access
                if (perm instanceof FilePermission) {
                    var path = perm.getName();
                    if (path.endsWith(File.separator)) {
                        path += "-";
                        perm = new FilePermission(path, "read");
                    }
                }
                perms.add(perm);
            }
        } catch (IOException ignored) {
            // ignored
        }

        // add the permissions from the clockwork policy, if any is active
        final var policy = ClockworkSecurityPolicy.getActivePolicy();
        if (policy != null) {
            var plugin = codeSourceToPlugin.get(cs.getLocation());
            var pluginPerms = plugin == null ? policy.getUntrusted() : policy.getUntrusted(plugin);
            pluginPerms.elementsAsStream().forEach(perms::add);
        }

        return perms;
    }

    /**
     * Find the candidate module for the given class name.
     * Returns {@code null} if none of the modules defined to this
     * class loader contain the API package for the class.
     */
    private LoadedModule findLoadedModule(String cn) {
        var pn = packageName(cn);
        return pn.isEmpty() ? null : localPackageToModule.get(pn);
    }

    /**
     * Returns the package name for the given class name.
     */
    private String packageName(String cn) {
        var pos = cn.lastIndexOf('.');
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
     * Returns true if the given module opens the given package
     * unconditionally.
     *
     * @implNote This method currently iterates over each of the open
     * packages. This will be replaced once the ModuleDescriptor.Opens
     * API is updated.
     */
    private boolean isOpen(ModuleReference mref, String pn) {
        var descriptor = mref.descriptor();
        if (descriptor.isOpen() || descriptor.isAutomatic()) return true;
        for (var opens : descriptor.opens()) {
            var source = opens.source();
            if (!opens.isQualified() && source.equals(pn)) return true;
        }
        return false;
    }

    /**
     * Binds a module to a plugin container.
     * This assigs the permissions of the plugin to the classes of the module.
     * This method should only be called before any classes of the module are loaded.
     * Every module can only be bound to one plugin.
     * If no module with the given name is present, or its location can not be determined,
     * then this method just has no effect, and will not throw any exception.
     */
    protected void bindPlugin(PluginReference plugin) {
        final var module = nameToModule.get(plugin.getModuleName());
        if (module != null && module.location().isPresent()) {
            try {
                codeSourceToPlugin.put(module.location().get().toURL(), plugin.getDescriptor());
            } catch (Exception e) {
                // ignored
            }
        }
    }

    private static class LoadedModule {

        private final ModuleReference mref;
        private final URL url; // may be null
        private final CodeSource cs;

        LoadedModule(ModuleReference mref) {
            URL url = null;
            if (mref.location().isPresent()) {
                try {
                    url = mref.location().get().toURL();
                } catch (MalformedURLException | IllegalArgumentException ignored) {
                    // ignored
                }
            }

            this.mref = mref;
            this.url = url;
            this.cs = new CodeSource(url, (CodeSigner[]) null);
        }

        ModuleReference mref() {return mref;}
        String name() {return mref.descriptor().name();}
        URL location() {return url;}
        CodeSource codeSource() {return cs;}

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

}
