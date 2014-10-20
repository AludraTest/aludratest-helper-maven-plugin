package org.aludratest.maven.site.impl.parser;

import java.util.Map;

public class ConfigurableDescription extends ComponentDescription {

	boolean service;

    String description;

	ClassConfigurationPropertiesDescription commonProperties;

	Map<Class<?>, ClassConfigurationPropertiesDescription> implementationSpecificProperties;

    public String getDescription() {
        return description;
    }

    public boolean isService() {
        return service;
    }

    public ClassConfigurationPropertiesDescription getCommonProperties() {
        return commonProperties;
    }

    public Map<Class<?>, ClassConfigurationPropertiesDescription> getImplementationSpecificProperties() {
        return implementationSpecificProperties;
    }

}