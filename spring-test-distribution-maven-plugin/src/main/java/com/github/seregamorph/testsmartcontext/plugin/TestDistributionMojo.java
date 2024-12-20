package com.github.seregamorph.testsmartcontext.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(
    name = "test-distribution",
    requiresDependencyResolution = ResolutionScope.TEST,
    defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
    threadSafe = true)
public class TestDistributionMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Collection<URI> urls = getClasspathUris();

        ClassLoader prevClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader classLoader = new URLClassLoader(toUrls(urls))) {
            Thread.currentThread().setContextClassLoader(classLoader);

        } catch (IOException e) {
            throw new MojoFailureException("Failed", e);
        } finally {
            Thread.currentThread().setContextClassLoader(prevClassLoader);
        }
    }

    private Set<URI> getClasspathUris() {
        Set<URI> urls = new LinkedHashSet<>();
        for (Artifact artifact : project.getArtifacts()) {
            urls.add(artifact.getFile().toPath().toUri());
        }

        File classesDir = new File(project.getBuild().getOutputDirectory());
        if (classesDir.exists()) {
            urls.add(classesDir.toURI());
        }
        File testClassesDir = new File(project.getBuild().getTestOutputDirectory());
        if (testClassesDir.exists()) {
            urls.add(testClassesDir.toURI());
        }
        return urls;
    }

    private static URL[] toUrls(Collection<URI> uris) throws MojoExecutionException {
        List<URL> urls = new ArrayList<>();
        for (URI uri : uris) {
            try {
                urls.add(uri.toURL());
            } catch (MalformedURLException e) {
                throw new MojoExecutionException("Failed to convert", e);
            }
        }
        return urls.toArray(new URL[0]);
    }
}
