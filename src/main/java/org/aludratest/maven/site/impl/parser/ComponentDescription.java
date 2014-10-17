package org.aludratest.maven.site.impl.parser;

import java.util.List;

public abstract class ComponentDescription {

	protected Class<?> interfaceClass;

	protected List<Class<?>> implementorClasses;

	protected Class<?> defaultImplementationClass;

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public List<Class<?>> getImplementorClasses() {
        return implementorClasses;
    }

    public Class<?> getDefaultImplementationClass() {
        return defaultImplementationClass;
    }

}