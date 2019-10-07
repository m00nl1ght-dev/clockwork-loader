package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.api.PluginLoader;
import dev.m00nl1ght.clockwork.core.ComponentDefinition;
import dev.m00nl1ght.clockwork.core.ComponentTargetDefinition;
import dev.m00nl1ght.clockwork.core.DependencyDefinition;
import dev.m00nl1ght.clockwork.core.PluginDefinition;

import java.util.stream.Stream;

public class TestPluginLoader implements PluginLoader {

    private static final String MAIN_CLASS = "dev.m00nl1ght.clockwork.test.plugin.ExamplePlugin";
    private static final String TEST_COMP_01 = "dev.m00nl1ght.clockwork.test.plugin.TestComponent01";
    private static final String TEST_COMP_02 = "dev.m00nl1ght.clockwork.test.plugin.TestComponent02";
    private static final String TEST_COMP_03 = "dev.m00nl1ght.clockwork.test.plugin.TestComponent03";
    private static final String TEST_COMP_HOLDER = "dev.m00nl1ght.clockwork.test.TestComponentHolder";
    private static final String TEST_CUSTOM_COMP_HOLDER = "dev.m00nl1ght.clockwork.test.plugin.TestCustomComponentTarget01";

    @Override
    public Stream<PluginDefinition> findPlugins() {
        var plugin = new PluginDefinition("example", "0.1", MAIN_CLASS);
        var comp1 = new ComponentDefinition("example:comp1", "0.1", TEST_COMP_01, "test:holder");
        comp1.addDependency(new DependencyDefinition("example"));
        comp1.addDependency(new DependencyDefinition("example:comp2"));
        var comp2 = new ComponentDefinition("example:comp2", "0.1", TEST_COMP_02, "example:holder1");
        comp2.addDependency(new DependencyDefinition("example"));
        var comp3 = new ComponentDefinition("example:comp3", "0.1", TEST_COMP_03, "clockwork:core");
        comp3.addDependency(new DependencyDefinition("example"));
        var holder1 = new ComponentTargetDefinition("example:holder1", TEST_CUSTOM_COMP_HOLDER);

        plugin.addComponentDef(comp1);
        plugin.addComponentDef(comp2);
        plugin.addComponentDef(comp3);
        plugin.addComponentHolderDef(holder1);
        return Stream.of(plugin);
    }

}
