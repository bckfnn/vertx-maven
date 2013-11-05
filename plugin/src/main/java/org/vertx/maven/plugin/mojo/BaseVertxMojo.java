package org.vertx.maven.plugin.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.file.Files.readAllBytes;

public abstract class BaseVertxMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project.dependencyArtifacts}", required = true, readonly = true)
  private Set<Artifact> dependencyArtifacts;

  @Parameter(defaultValue = "${project}", required = true, readonly = true) 
  protected MavenProject project;
    
  @Parameter(defaultValue = "${localRepository}", required = true, readonly = true) 
  private ArtifactRepository localRepository;

  @Parameter(defaultValue = "${project.remoteArtifactRepositories}", required = true, readonly = true) 
  private List<ArtifactRepository> remoteRepositories;

  @Component
  private ArtifactResolver resolver;
    
  @Component
  private MavenProjectBuilder mavenProjectBuilder;
    
  @Component
  private ArtifactFactory artifactFactory;
    
  @Component
  private ArtifactMetadataSource artifactMetadataSource;


  /**
   * The name of the module to run.
   * <p/>
   * If you're running a module, it's the name of the module to be run.
   */
  @Parameter(property = "moduleName", defaultValue = "${project.groupId}~${project.artifactId}~${project.version}")
  protected String moduleName;

  /**
   * <p>
   * The config file for this verticle.
   * </p>
   * <p>
   * If the path is relative (does not start with / or a drive letter like
   * C:), the path is relative to the directory containing the POM.
   * </p>
   * <p>
   * An example value would be src/main/resources/com/acme/MyVerticle.conf
   * </p>
   */
  @Parameter
  protected File configFile = null;

  /**
   * The number of instances of the verticle to instantiate in the vert.x
   * server. The default is 1.
   */
  @Parameter(defaultValue = "1")
  protected Integer instances;

  /**
   * The mods directory.  The default is relative path target/mods.
   */
  @Parameter(defaultValue = "target/mods")
  protected File modsDir;

  
  protected List<URL> getPlatformDependencies() throws Exception {
    Set<Artifact> vertxArtifacts = new HashSet<Artifact>();
    //System.out.println(dependencyArtifacts);
    for (Artifact a : dependencyArtifacts) {
      if (a.getGroupId().equals("io.vertx")) {
        if (a.getArtifactId().equals("vertx-core") || a.getArtifactId().equals("vertx-platform")) {
          resolver.resolve(a, remoteRepositories, localRepository);
          vertxArtifacts.add(a);
          MavenProject pomProject = mavenProjectBuilder.buildFromRepository(a, remoteRepositories, localRepository);
          AndArtifactFilter filter = new AndArtifactFilter();  
          filter.add(new ScopeArtifactFilter(DefaultArtifact.SCOPE_COMPILE));
          if (a.getDependencyFilter() != null) {
            filter.add(a.getDependencyFilter());
          }
          Set<?> artifacts = pomProject.createArtifacts(artifactFactory, null, null);
          artifacts.removeAll(dependencyArtifacts);
          ArtifactResolutionResult arr = resolver.resolveTransitively(artifacts, a, pomProject.getManagedVersionMap(), localRepository, remoteRepositories, artifactMetadataSource, filter);
          vertxArtifacts.addAll(arr.getArtifacts());
        }
      }
    }

    List<URL> urls = new ArrayList<>();
    for (Artifact a : vertxArtifacts) {
      getLog().debug("vertx dependency:" + a);
      urls.add(a.getFile().toURI().toURL());
    }
    return urls;
  }

  protected ClassLoader getPlatformClassLoader() throws Exception {
    List<URL> urls = getPlatformDependencies();
    return new URLClassLoader(urls.toArray(new URL[urls.size()]));
  }
  
  protected void starterMain(String... args) throws Exception {
      System.setProperty("vertx.mods", modsDir.getAbsolutePath());
      
      for (Object e : project.getRuntimeClasspathElements()) {
          getLog().debug("module dependency:" + e);
      }
      
      ClassLoader platformClassLoader = getPlatformClassLoader();

      Thread.currentThread().setContextClassLoader(platformClassLoader);
      Method main = platformClassLoader.loadClass("org.vertx.java.platform.impl.cli.Starter").getMethod("main", new Class[] { String[].class });
 
      main.invoke(null, new Object[] { args });
  }

}

