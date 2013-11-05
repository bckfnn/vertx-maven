package org.vertx.maven.plugin.mojo;

import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

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

/**
 * @description Runs vert.x directly from a Maven project.
 */
@Mojo(name = "runMod", requiresProject = true, threadSafe = false, requiresDependencyResolution = COMPILE_PLUS_RUNTIME)
public class VertxRunModMojo extends BaseVertxMojo {

  @Override
  public void execute() throws MojoExecutionException {
    try {
      List<String> args = new ArrayList<>();
      args.add("runmod");
      args.add(moduleName);
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
}
