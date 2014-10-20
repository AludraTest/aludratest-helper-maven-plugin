package org.aludratest.maven.site.impl.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.parser.ParseException;

public final class JavadocExtractor {

    private static final Pattern PATTERN_LINK = Pattern.compile("\\{@link ([^\\}]+)\\}");

    private JavadocExtractor() {
    }

    @SuppressWarnings("unchecked")
    public static String extractJavadocHtml(MavenProject project, Class<?> clazz, Log log) {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.setEncoding("UTF-8");

        String fileClassName = clazz.getName();
        // if internal class, extract wrapper class name
        while (fileClassName.contains("$")) {
            fileClassName = fileClassName.substring(0, fileClassName.lastIndexOf('$'));
        }

        String javaFileName = fileClassName.replace('.', '/') + ".java";

        try {
            JavaSource src = null;
            // search Java file in source roots
            for (String source : (List<String>) project.getCompileSourceRoots()) {
                File root = new File(source);
                File f = new File(root, javaFileName);
                if (f.isFile()) {
                    src = builder.addSource(f);
                    break;
                }
            }

            if (src == null) {
                throw new IOException("No source file found for class " + clazz.getName());
            }

            // find matching class - could be multiple classes
            JavaClass srcClass = findClassIn(clazz, src.getClasses());
            if (srcClass == null) {
                throw new IOException("No source file found for class " + clazz.getName());
            }

            String javadoc = srcClass.getComment();

            // translate {@link ...} links, if any
            javadoc = resolveDocLinks(javadoc, src);

            return javadoc;
        }
        catch (IOException e) {
            log.error("Could not extract description Javadoc from class " + clazz.getName(), e);
            return null;
        }
        catch (ParseException pe) {
            log.warn("Could not extract description Javadoc from class " + clazz.getName(), pe);
            return null;
        }
    }

    private static String resolveDocLinks(String javadoc, JavaSource source) {
        Matcher m = PATTERN_LINK.matcher(javadoc);
        while (m.find()) {
            String targetName = m.group(1).trim();
            // resolve imported classes
            if (!targetName.contains(".")) {
                for (String impName : source.getImports()) {
                    if (impName.endsWith("." + targetName)) {
                        targetName = impName;
                        break;
                    }
                }
            }

            String linkHref = "./apidocs/" + targetName.replace('.', '/') + ".html";
            javadoc = javadoc.substring(0, m.start()) + "<a href=\"" + linkHref + "\">" + m.group(1) + "</a>"
                    + javadoc.substring(m.end());
            m = PATTERN_LINK.matcher(javadoc);
        }

        return javadoc;
    }

    private static JavaClass findClassIn(Class<?> clazz, List<JavaClass> classes) {
        for (JavaClass clz : classes) {
            if (clz.getFullyQualifiedName().equals(clazz.getName())) {
                return clz;
            }

            // inner classes?
            JavaClass subclz = findClassIn(clazz, clz.getNestedClasses());
            if (subclz != null) {
                return subclz;
            }
        }

        return null;
    }

}
