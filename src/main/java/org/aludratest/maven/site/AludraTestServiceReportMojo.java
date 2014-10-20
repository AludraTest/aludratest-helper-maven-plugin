package org.aludratest.maven.site;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.aludratest.maven.site.impl.parser.AludraTestServiceParser;
import org.aludratest.maven.site.impl.parser.AludraTestServicesDescription;
import org.aludratest.maven.site.impl.parser.ClassConfigurationPropertiesDescription;
import org.aludratest.maven.site.impl.parser.ComplexConfigurationTypeDescription;
import org.aludratest.maven.site.impl.parser.ConfigurableDescription;
import org.aludratest.maven.site.impl.parser.ConfigurationPropertyDescription;
import org.aludratest.maven.site.impl.parser.ServiceDescription;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 * This report mojo generates a report about the available AludraTest services and their configurations. This report is mainly
 * used by the AludraTest project itself, but can also be used by other projects which add new services, configurable components,
 * or different service implementations.
 * 
 * @goal services-doc
 * 
 * @author falbrech
 * 
 */
public class AludraTestServiceReportMojo extends AbstractMavenReport {

    /**
     * Directory where reports will go.
     * 
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    private String outputDirectory;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

    /**
     * The aludraservices.properties file declaring the available services and components, and the implementing classes which will
     * be queried for configuration elements.
     * 
     * @parameter default-value="${basedir}/src/main/resources/aludraservice.properties.default"
     */
    private File aludraServicesFile;

    /* Constants of AludraTest class names for Reflection use. */

    @Override
    public String getDescription(Locale locale) {
        return "AludraTest Services Documentation";
    }

    @Override
    public String getName(Locale locale) {
        return "AludraTest Services";
    }

    @Override
    public String getOutputName() {
        return "aludratest-services";
    }

    @Override
    protected Renderer getSiteRenderer() {
        return siteRenderer;
    }

    @Override
    protected String getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected MavenProject getProject() {
        return project;
    }

	private String getProjectName() {
		String name = getProject().getName();
		if (name == null || "".equals(name)) {
			name = getProject().getArtifactId();
		}
		return name;
	}

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        // parse project
        AludraTestServicesDescription description = AludraTestServiceParser.parse(getProject(), aludraServicesFile, getLog());

        Sink sink = getSink();
        renderHead(sink);

        renderBody(sink, description);

        sink.flush();
        sink.close();
    }

    private void renderHead(Sink sink) {
        sink.head();
        sink.title();
        sink.text("AludraTest Services and Component Documentation");
        sink.title_();
        sink.head_();
    }

    private void renderTableHeader(Sink sink, String... headerLabels) {
        sink.tableRow();
        for (String header : headerLabels) {
            sink.tableHeaderCell();
            sink.text(header);
            sink.tableHeaderCell_();
        }
        sink.tableRow_();
    }

    private void renderServicesTableRow(Sink sink, ServiceDescription serviceDesc) {
        sink.tableRow();
        sink.tableCell();
        sink.link(buildJavadocLink(serviceDesc.getInterfaceClass().getName()));
        sink.text(serviceDesc.getInterfaceClass().getName());
        sink.link_();
        sink.tableCell_();
        sink.tableCell();
        sink.text(serviceDesc.getName());
        sink.tableCell_();
        sink.tableCell();
        sink.text(serviceDesc.getDescription());
        sink.tableCell_();
        sink.tableCell();
        sink.link(buildSourceXrefLink(serviceDesc.getDefaultImplementationClass().getName()));
        sink.text(serviceDesc.getDefaultImplementationClass().getName());
        sink.link_();
        sink.tableCell_();
        sink.tableRow_();
    }

    private void renderBody(Sink sink, AludraTestServicesDescription description) throws MavenReportException {
        sink.body();

        sink.section1();
        sink.sectionTitle1();
		sink.text("Services and Component Documentation");
        sink.sectionTitle1_();

		sink.text("This documentation describes the available " + getProjectName()
				+ " services and components which can be configured from client test projects.");
        sink.lineBreak();

        renderServicesSection(sink, description.getServiceDescriptions());
        renderConfigurationSection(sink, description.getConfigurableDescriptions());

        sink.section1_();

        sink.body_();
    }

    private void renderServicesSection(Sink sink, List<ServiceDescription> serviceDescriptions) {
        sink.section2();
        sink.sectionTitle2();
        sink.text("Services");
        sink.sectionTitle2_();
        sink.text("These are the services which can be queried from an AludraTestCase for use.");
        sink.lineBreak();
        sink.lineBreak();

        sink.table();

        renderTableHeader(sink, "Service Interface", "Service Name", "Description", "Default implementation");

        for (ServiceDescription serviceDesc : serviceDescriptions) {
            renderServicesTableRow(sink, serviceDesc);
        }
        sink.table_();

        sink.section2_();
    }

    private void renderConfigurationSection(Sink sink, List<ConfigurableDescription> configDescriptions) {
        sink.section2();
        sink.sectionTitle2();
        sink.text("Configuration");
        sink.sectionTitle2_();
        sink.text("Most of the services and some internal components of AludraTest can be configured. For information on configuration files and properties, please refer to the following link:");
        sink.lineBreak();
        sink.lineBreak();
        sink.link("./service-configuration.html");
        sink.text("AludraTest Service Configuration");
        sink.link_();
        sink.lineBreak();
        sink.lineBreak();
        sink.text("The following sections describe the configurable services and components, and which configuration properties are available for each.");

        for (ConfigurableDescription configDesc : configDescriptions) {
            renderConfigurableDescription(sink, configDesc);
        }

        sink.section2_();
    }

    private void renderConfigurableDescription(Sink sink, ConfigurableDescription configDescription) {
        sink.section3();
        sink.sectionTitle3();
        sink.text(configDescription.getInterfaceClass().getSimpleName());
        sink.sectionTitle3_();
        
        if (configDescription.getDescription() != null) {
            String descriptionJavadoc = configDescription.getDescription();
            descriptionJavadoc.replace("<br>", "<br />");
            sink.rawText(descriptionJavadoc);
            sink.lineBreak();
            sink.lineBreak();
        }

        sink.italic();
        sink.text("Interface class: " + configDescription.getInterfaceClass().getName());
        sink.lineBreak();
        sink.text("Default implementation: " + configDescription.getDefaultImplementationClass().getName());
        sink.lineBreak();
        sink.text("All known implementations: ");

        StringBuilder sb = new StringBuilder();
        for (Class<?> implClass : configDescription.getImplementorClasses()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(implClass.getName());
        }
        sink.text(sb.toString());
        sink.italic_();
        sink.lineBreak();
        sink.lineBreak();

        if (!configDescription.getCommonProperties().getProperties().isEmpty()) {
            sink.section4();
            sink.sectionTitle4();
            sink.text("Common properties");
            sink.sectionTitle4_();

            renderPropertiesTable(sink, configDescription.getCommonProperties().getProperties());
            renderComplexTypes(sink, configDescription.getCommonProperties().getComplexProperties());

            sink.section4_();
        }

        for (Map.Entry<Class<?>, ClassConfigurationPropertiesDescription> entry : configDescription
                .getImplementationSpecificProperties().entrySet()) {
            sink.section4();
            sink.sectionTitle4();
            sink.text("Properties for implementation " + entry.getKey().getName());
            sink.sectionTitle4_();

            renderPropertiesTable(sink, entry.getValue().getProperties());
            renderComplexTypes(sink, entry.getValue().getComplexProperties());

            sink.section4_();

        }

        sink.section3_();
    }

    private void renderPropertiesTable(Sink sink, List<ConfigurationPropertyDescription> properties) {
        sink.table();
        renderTableHeader(sink, "Name", "Type", "Description", "Default value", "Required");
        
        for (ConfigurationPropertyDescription property : properties) {
            sink.tableRow();
            sink.tableCell();
            sink.text(property.getName());
            sink.tableCell_();
            sink.tableCell();
            if (property.isComplex()) {
                sink.bold();
                sink.text(property.getType().getSimpleName());
                sink.bold_();
            }
            else {
                sink.italic();
                sink.text(property.getType().getSimpleName());
                sink.italic_();
            }
            sink.tableCell_();
            sink.tableCell();
            sink.text(property.getDescription());
            sink.tableCell_();
            sink.tableCell();
            if (property.getDefaultValue() != null) {
                sink.text(property.getDefaultValue());
            }
            sink.tableCell_();
            sink.tableCell();
            sink.italic();
            sink.text(property.isRequired() ? "yes" : "no");
            sink.italic_();
            sink.tableCell_();
            sink.tableRow_();
        }
        
        sink.table_();
    }

    private void renderComplexTypes(Sink sink, List<ComplexConfigurationTypeDescription> complexTypes) {
        for (ComplexConfigurationTypeDescription complexType : complexTypes) {
            sink.section5();
            sink.sectionTitle5();
            sink.text(complexType.getType().getSimpleName() + " Type");
            sink.sectionTitle5_();

            if (complexType.getDescription() != null) {
                sink.rawText(complexType.getDescription());
                sink.lineBreak();
                sink.lineBreak();
            }

            renderPropertiesTable(sink, complexType.getProperties());

            sink.section5_();
        }
    }

    private String buildJavadocLink(String className) {
        return "apidocs/" + className.replace('.', '/') + ".html";
    }

    private String buildSourceXrefLink(String className) {
        return "xref/" + className.replace('.', '/') + ".html";
    }

}
