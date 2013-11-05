package org.vertx.maven.plugin.mojo;

/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */


import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @description Runs The module using the resources in IntelliJ IDEA
 */
@Mojo(name = "runModDev", requiresProject = true, threadSafe = false, requiresDependencyResolution = COMPILE_PLUS_RUNTIME)
public class RunModDevMojo extends BaseVertxMojo {
    
  @Override
  public void execute() throws MojoExecutionException {
    try {
      for (Object e : project.getRuntimeClasspathElements()) {
          getLog().debug("module dependency:" + e);
      }
      List<String> args = new ArrayList<>();
      args.add("runmod");
      args.add(moduleName);
      args.add("-cp");
      args.add(join(project.getRuntimeClasspathElements()));
      if (configFile != null) {
          args.add("-conf");
          args.add(configFile.getPath());
      }
      args.add("-instances");
      args.add(Integer.toString(instances));
      starterMain(args.toArray(new String[args.size()]));
      
    } catch (Exception exc) {
      throw new MojoExecutionException(exc.getMessage());
    }
  }

  @Override
  protected ClassLoader getPlatformClassLoader() throws Exception {
    final List<String> resourceFolders = new ArrayList<>();
    
    for (int i = 0; i< project.getResources().size(); i++) {
        Resource res = (Resource) project.getResources().get(i);
        resourceFolders.add(res.getDirectory());
    }

    List<URL> urls = getPlatformDependencies();
    return new URLClassLoader(urls.toArray(new URL[urls.size()])) {
      @Override
      public URL findResource(String name) {
        if (name.equals("langs.properties") || name.equals("repos.txt")) {
          for (String folder : resourceFolders) {
            File f = new File(new File(folder), name);
            if (f.exists()) {
              try {
                getLog().debug("using platform resource from local project " + f);
                return f.toURI().toURL();
              } catch (MalformedURLException e) {
                // ignore
              } 
            }
          }
        }
        return super.findResource(name);
      }
    };
  }
  
  private String join(List<?> list) {
    StringBuilder ret = new StringBuilder();
    for (Object o : list) {
      if (ret.length() > 0) {
        ret.append(";");
      }
      ret.append(o);
    }
    return ret.toString();
  }
}
