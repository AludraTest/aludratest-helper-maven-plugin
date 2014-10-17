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
