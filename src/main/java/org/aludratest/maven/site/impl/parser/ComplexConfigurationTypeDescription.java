package org.aludratest.maven.site.impl.parser;

import java.util.List;

public class ComplexConfigurationTypeDescription {

	Class<?> type;

    String description;

    List<ConfigurationPropertyDescription> properties;

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}

		return ((ComplexConfigurationTypeDescription) obj).type.equals(type);
	}

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    public Class<?> getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public List<ConfigurationPropertyDescription> getProperties() {
        return properties;
    }

}