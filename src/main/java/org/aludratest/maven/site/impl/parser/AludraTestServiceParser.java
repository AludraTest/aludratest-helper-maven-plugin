package org.aludratest.maven.site.impl.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.aludratest.maven.site.AludraTestClassConstants;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;

public final class AludraTestServiceParser implements AludraTestClassConstants {

	private Log log;

    private MavenProject project;

    private AludraTestServiceParser(MavenProject project, Log log) {
        this.project = project;
		this.log = log;
	}

    public static AludraTestServicesDescription parse(MavenProject project, File aludraServicesFile, Log log)
			throws MavenReportException {
        AludraTestServiceParser result = new AludraTestServiceParser(project, log);
        return result.internalParse(aludraServicesFile);
	}

	@SuppressWarnings("unchecked")
    private AludraTestServicesDescription internalParse(File aludraServicesFile)
            throws MavenReportException {
		// find all service interfaces
		Properties aludraServices = readAludraServicesFile(aludraServicesFile);

		ClassLoader cl = buildCompileClassLoader(project);

		Class<? extends Annotation> serviceInterfaceAnnot;
		Class<? extends Annotation> implementationAnnot;

		try {
			serviceInterfaceAnnot = (Class<? extends Annotation>) cl.loadClass(SERVICE_INTERFACE_ANNOTATION_CLASS);
			implementationAnnot = (Class<? extends Annotation>) cl.loadClass(IMPLEMENTATION_ANNOTATION_CLASS);
		}
		catch (ClassNotFoundException e) {
			throw new MavenReportException("Could not load ServiceInterface annotation class", e);
		}

		List<String> sortedServiceInterfaces = new ArrayList<String>(aludraServices.stringPropertyNames());
		Collections.sort(sortedServiceInterfaces);

        List<ServiceDescription> serviceDescriptions = new ArrayList<ServiceDescription>();
        List<ConfigurableDescription> configurableDescriptions = new ArrayList<ConfigurableDescription>();

		for (String serviceInterface : sortedServiceInterfaces) {
			try {
				Class<?> ifClass = cl.loadClass(serviceInterface);
				String implClassName = aludraServices.getProperty(serviceInterface);
				Class<?> implClass = cl.loadClass(implClassName);

				// search for implementations
				List<Class<?>> implementations = new ArrayList<Class<?>>();
				for (Object o : project.getCompileSourceRoots()) {
					String dir = o.toString();
                    File f = new File(dir);

					findClasses(f, null, new ClassWithAnnotationClassValueMatcher(implementationAnnot, "value", ifClass), cl,
							implementations);
				}

				if (ifClass.isAnnotationPresent(serviceInterfaceAnnot)) {
					Annotation annot = ifClass.getAnnotation(serviceInterfaceAnnot);
					ServiceDescription description = new ServiceDescription();
					description.interfaceClass = ifClass;
					description.defaultImplementationClass = implClass;
					description.name = extractStringAttribute(annot, "name");
					description.description = extractStringAttribute(annot, "description");
					description.implementorClasses = implementations;
					serviceDescriptions.add(description);
				}

                // collect all interface common configuration properties (if any)
				List<ConfigurationPropertyDescription> properties = new ArrayList<ConfigurationPropertyDescription>();
				List<ComplexConfigurationTypeDescription> complexTypes = new ArrayList<ComplexConfigurationTypeDescription>();
				getConfigurationProperties(ifClass, Object.class, properties, complexTypes);

				ConfigurableDescription confDesc = new ConfigurableDescription();
				confDesc.interfaceClass = ifClass;
				confDesc.defaultImplementationClass = implClass;
				confDesc.implementorClasses = implementations;
				confDesc.service = ifClass.isAnnotationPresent(serviceInterfaceAnnot);
                confDesc.description = JavadocExtractor.extractJavadocHtml(project, ifClass, log);
				confDesc.commonProperties = new ClassConfigurationPropertiesDescription(properties, complexTypes);
                confDesc.implementationSpecificProperties = new LinkedHashMap<Class<?>, ClassConfigurationPropertiesDescription>();

                // dive into implementations
                for (Class<?> configImplClass : confDesc.implementorClasses) {
                    properties = new ArrayList<ConfigurationPropertyDescription>();
                    complexTypes = new ArrayList<ComplexConfigurationTypeDescription>();
                    getConfigurationProperties(configImplClass, ifClass, properties, complexTypes);
                    if (!properties.isEmpty()) {
                        confDesc.implementationSpecificProperties.put(configImplClass,
                                new ClassConfigurationPropertiesDescription(properties, complexTypes));
                    }
                }

                // only add if common properties or implementation specific properties set
                if (!confDesc.commonProperties.properties.isEmpty() || !confDesc.implementationSpecificProperties.isEmpty()) {
                    configurableDescriptions.add(confDesc);
                }
			}
			catch (ClassNotFoundException e) {
				log.info("Could not load service interface (or implementing) class " + serviceInterface, e);
			}
		}

        return new AludraTestServicesDescription(serviceDescriptions, configurableDescriptions);
	}

	private Properties readAludraServicesFile(File aludraServicesFile) throws MavenReportException {
		if (aludraServicesFile == null || !aludraServicesFile.isFile()) {
			throw new MavenReportException("aludraServicesFile does not exist: " + aludraServicesFile);
		}

		InputStream in = null;
		Properties services = new Properties();
		try {
			in = new FileInputStream(aludraServicesFile);
			services.load(in);
			if (services.isEmpty()) {
				throw new MavenReportException("Aludra Services File " + aludraServicesFile + " is empty");
			}
			return services;
		}
		catch (IOException e) {
			throw new MavenReportException("Could not read Aludra Services File", e);
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			}
			catch (IOException e2) {
				// ignore
			}
		}
	}

	private ClassLoader buildCompileClassLoader(MavenProject project) throws MavenReportException {
		try {
			List<?> classpathElements = project.getCompileClasspathElements();
            log.debug("Building ClassLoader for service classes and implementations with following locations: ");

			List<URL> urls = new ArrayList<URL>();
			for (Object o : classpathElements) {
                log.debug("  " + o);
				try {
					urls.add(new File(o.toString()).getAbsoluteFile().toURI().toURL());
				}
				catch (MalformedURLException e) {
					if (log.isDebugEnabled()) {
						log.debug("Could not add classpath element " + o + " to classpath", e);
					}
					else {
						log.warn("Could not add classpath element " + o + " to classpath");
					}
				}
			}

			return new URLClassLoader(urls.toArray(new URL[0]));
		}
		catch (DependencyResolutionRequiredException e) {
			throw new MavenReportException("Could not resolve Maven classpath elements", e);
		}
	}

	private static String extractStringAttribute(Annotation annot, String attrName) {
		try {
			return (String) annot.getClass().getMethod(attrName).invoke(annot);
		}
		catch (Exception e) {
			return null;
		}
	}

	private static Class<?> extractClassAttribute(Annotation annot, String attrName) {
		try {
			return (Class<?>) annot.getClass().getMethod(attrName).invoke(annot);
		}
		catch (Exception e) {
			return null;
		}
	}

    private static Class<?>[] extractClassArrayAttribute(Annotation annot, String attrName) {
        try {
            return (Class<?>[]) annot.getClass().getMethod(attrName).invoke(annot);
        }
        catch (Exception e) {
            return null;
        }
    }

	private static boolean extractBooleanAttribute(Annotation annot, String attrName) {
		try {
			return ((Boolean) annot.getClass().getMethod(attrName).invoke(annot)).booleanValue();
		}
		catch (Exception e) {
			return false;
		}
	}

	private void findClasses(File sourceDir, String classNamePrefix, ClassMatcher matcher, ClassLoader cl, List<Class<?>> result) {
        File[] fileList = sourceDir.listFiles();
        if (fileList == null) {
            log.debug("Encountered an invalid directory when scanning for Java files: " + sourceDir.getAbsolutePath());
            return;
        }
        for (File f : fileList) {
			if (f.isDirectory()) {
				findClasses(f, classNamePrefix == null ? f.getName() : (classNamePrefix + "." + f.getName()), matcher, cl, result);
			}
			else if (f.isFile() && f.getName().endsWith(".java")) {
				String className = classNamePrefix + "." + f.getName().substring(0, f.getName().length() - ".java".length());
				try {
					Class<?> clazz = cl.loadClass(className);
					if (matcher.matches(clazz)) {
						result.add(clazz);
					}
				}
				catch (Throwable t) {
					// ignore silently
				}
			}
		}
	}

	private void getConfigurationProperties(Class<?> clazz, Class<?> stopClass, List<ConfigurationPropertyDescription> result,
			List<ComplexConfigurationTypeDescription> complexProperties) {
        if (clazz == null || clazz.equals(stopClass)) {
            return;
        }
        // start with parent classes and interfaces
        getConfigurationProperties(clazz.getSuperclass(), stopClass, result, complexProperties);
        for (Class<?> ifClass : clazz.getInterfaces()) {
            getConfigurationProperties(ifClass, stopClass, result, complexProperties);
        }

		Annotation[] annots = clazz.getAnnotations();

		for (Annotation annot : annots) {
			String annotationName = annot.annotationType().getName();
			if (annotationName.equals(CONFIG_PROPERTIES_ANNOTATION_CLASS)) {
				Annotation[] childAnnotations = extractChildAnnotations(clazz, annot);
				for (Annotation a : childAnnotations) {
					result.add(createPropertyDescFromAnnotation(a));
				}
			}
			else if (annotationName.equals(CONFIG_PROPERTY_ANNOTATION_CLASS)) {
				result.add(createPropertyDescFromAnnotation(annot));
			}
			else if (annotationName.equals(CONFIG_NODE_PROPERTIES_ANNOTATION_CLASS)) {
				Annotation[] childAnnotations = extractChildAnnotations(clazz, annot);
				for (Annotation a : childAnnotations) {
					result.add(createNodePropertyDescFromAnnotation(a, complexProperties));
				}
			}
			else if (annotationName.equals(CONFIG_NODE_PROPERTY_ANNOTATION_CLASS)) {
				result.add(createNodePropertyDescFromAnnotation(annot, complexProperties));
			}
		}

	}

	private ConfigurationPropertyDescription createPropertyDescFromAnnotation(Annotation a) {
		ConfigurationPropertyDescription desc = new ConfigurationPropertyDescription();
		desc.name = extractStringAttribute(a, "name");
		desc.type = extractClassAttribute(a, "type");
		desc.description = extractStringAttribute(a, "description");
		desc.defaultValue = extractStringAttribute(a, "defaultValue");
		desc.required = extractBooleanAttribute(a, "required");
		desc.complex = false;
		return desc;
	}

	private ConfigurationPropertyDescription createNodePropertyDescFromAnnotation(Annotation a,
			List<ComplexConfigurationTypeDescription> complexTypes) {
		ConfigurationPropertyDescription desc = new ConfigurationPropertyDescription();
		desc.name = extractStringAttribute(a, "name");
		if (extractBooleanAttribute(a, "appendCounterPattern")) {
			desc.name += "[1..999]";
		}
        desc.type = extractClassAttribute(a, "elementType");
		desc.description = extractStringAttribute(a, "description");
		desc.defaultValue = null;
		desc.required = false;
		desc.complex = true;

		// parse complex type
		ComplexConfigurationTypeDescription complexDesc = new ComplexConfigurationTypeDescription();
		complexDesc.type = desc.type;
        complexDesc.description = JavadocExtractor.extractJavadocHtml(project, desc.type, log);

		if (!complexTypes.contains(complexDesc)) {
			List<ConfigurationPropertyDescription> propertyList = new ArrayList<ConfigurationPropertyDescription>();
			getConfigurationProperties(complexDesc.type, Object.class, propertyList, complexTypes);
            complexDesc.properties = propertyList;
            complexTypes.add(complexDesc);
		}

		return desc;
	}

	private Annotation[] extractChildAnnotations(Class<?> clazz, Annotation annot) {
		try {
			Object[] annots = (Object[]) annot.getClass().getMethod("value").invoke(annot);
			Annotation[] result = new Annotation[annots.length];
			for (int i = 0; i < annots.length; i++) {
				result[i] = (Annotation) annots[i];
			}
			return result;
		}
		catch (Exception e) {
			log.error("Could not extract annotations from class " + clazz.getName(), e);
			return new Annotation[0];
		}
	}

	private static interface ClassMatcher {
		public boolean matches(Class<?> clazz);
	}

	private static class ClassWithAnnotationClassValueMatcher implements ClassMatcher {

		private Class<? extends Annotation> annotation;

		private String attributeName;

		private Class<?> expectedValue;

        private ClassWithAnnotationClassValueMatcher(Class<? extends Annotation> annotation, String attributeName,
				Class<?> expectedValue) {
			this.annotation = annotation;
			this.attributeName = attributeName;
			this.expectedValue = expectedValue;
		}

		@Override
		public boolean matches(Class<?> clazz) {
			if (!clazz.isAnnotationPresent(annotation)) {
				return false;
			}

			Annotation annot = clazz.getAnnotation(annotation);
            Class<?>[] classArrayValue = extractClassArrayAttribute(annot, attributeName);
            return classArrayValue != null && Arrays.asList(classArrayValue).contains(expectedValue);
		}

	}

}
