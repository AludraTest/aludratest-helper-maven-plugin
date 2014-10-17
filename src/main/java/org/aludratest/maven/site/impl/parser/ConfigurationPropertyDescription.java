package org.aludratest.maven.site.impl.parser;

public class ConfigurationPropertyDescription {

	String name;

	String description;

	Class<?> type;

	String defaultValue;

	boolean required;

	boolean complex;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isComplex() {
        return complex;
    }

}