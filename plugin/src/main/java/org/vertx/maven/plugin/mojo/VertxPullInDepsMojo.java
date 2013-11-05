package org.vertx.maven.plugin.mojo;

import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "pullInDeps", requiresProject = true, threadSafe = false, requiresDependencyResolution =
    COMPILE_PLUS_RUNTIME)
public class VertxPullInDepsMojo extends BaseVertxMojo {

  @Parameter(property = "vertx.pullInDeps", defaultValue = "false")
  protected Boolean pullInDeps;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      if (pullInDeps) {
        starterMain("pulldeps", moduleName);
      }
    } catch (final Exception e) {
      throw new MojoExecutionException(e.getMessage());
    }
  }
}
