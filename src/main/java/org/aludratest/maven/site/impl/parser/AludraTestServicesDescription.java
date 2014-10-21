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

import java.util.Collections;
import java.util.List;

public final class AludraTestServicesDescription {

    private List<ServiceDescription> serviceDescriptions;

    private List<ConfigurableDescription> configurableDescriptions;

    public AludraTestServicesDescription(List<ServiceDescription> serviceDescriptions,
            List<ConfigurableDescription> configurableDescriptions) {
        this.serviceDescriptions = Collections.unmodifiableList(serviceDescriptions);
        this.configurableDescriptions = Collections.unmodifiableList(configurableDescriptions);
    }

    public List<ConfigurableDescription> getConfigurableDescriptions() {
        return configurableDescriptions;
    }

    public List<ServiceDescription> getServiceDescriptions() {
        return serviceDescriptions;
    }

}
