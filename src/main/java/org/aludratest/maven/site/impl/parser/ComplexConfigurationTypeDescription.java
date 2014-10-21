/*
 * Copyright (C) 2010-2014 Hamburg Sud and the contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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