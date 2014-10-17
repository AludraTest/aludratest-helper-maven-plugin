package org.aludratest.maven.site.impl.parser;

import java.util.Collections;
import java.util.List;

public class ClassConfigurationPropertiesDescription {

	List<ConfigurationPropertyDescription> properties;

	private List<ComplexConfigurationTypeDescription> complexProperties;

    ClassConfigurationPropertiesDescription(List<ConfigurationPropertyDescription> properties,
			List<ComplexConfigurationTypeDescription> complexProperties) {
        this.properties = Collections.unmodifiableList(properties);
        this.complexProperties = Collections.unmodifiableList(complexProperties);
	}

    public List<ConfigurationPropertyDescription> getProperties() {
        return properties;
    }

    public List<ComplexConfigurationTypeDescription> getComplexProperties() {
        return complexProperties;
    }
}