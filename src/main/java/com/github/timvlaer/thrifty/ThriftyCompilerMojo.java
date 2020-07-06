package com.github.timvlaer.thrifty;

import com.microsoft.thrifty.compiler.ThriftyCompiler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "thrifty-compiler", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(phase = LifecyclePhase.GENERATE_SOURCES, goal = "thrifty-compiler")
public class ThriftyCompilerMojo extends AbstractMojo {

  @Parameter(required = true)
  private File[] thriftFiles;

  @Parameter(defaultValue = "${project.build.directory}/generated-sources/thrifty/")
  private String outputDirectory;

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "false")
  private boolean enableConvenienceMethods;

  @Parameter(defaultValue = "true")
  private boolean generateGettersInBuilders;

  public void execute() {
    GlobalFlags.enableConvenienceMethods = enableConvenienceMethods;
    GlobalFlags.generateGettersInBuilders = generateGettersInBuilders;

    List<String> entryThriftFiles =
        Arrays.stream(thriftFiles).map(File::getAbsolutePath).collect(Collectors.toList());

    List<String> arguments = new ArrayList<>();

    String compilerReleaseVersion = (String) project.getProperties().get("maven.compiler.release");
    if (compilerReleaseVersion != null) {
      getLog().info("maven.compiler.release property is set to " + compilerReleaseVersion);
      if (Integer.parseInt(compilerReleaseVersion) >= 9) {
        arguments.add("--generated-annotation-type=jdk9");
      }
    }

    arguments.add("--out=" + outputDirectory);
    arguments.addAll(entryThriftFiles);
    getLog().debug("Run thrifty compiler with following arguments: " + arguments);

    ThriftyCompiler.main(arguments.toArray(new String[] {}));

    getLog().debug("Adding " + outputDirectory + " as source root in the maven project.");
    project.addCompileSourceRoot(outputDirectory);

    new CopyThriftFilesToClasses(project, getLog())
        .findAllThriftFilesAndAddToClasses(entryThriftFiles);
  }

  void setThriftFiles(File[] thriftFiles) {
    this.thriftFiles = thriftFiles;
  }

  void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  void setProject(MavenProject project) {
    this.project = project;
  }

  public void setEnableConvenienceMethods(boolean enableConvenienceMethods) {
    this.enableConvenienceMethods = enableConvenienceMethods;
  }

  public void setGenerateGettersInBuilders(boolean generateGettersInBuilders) {
    this.generateGettersInBuilders = generateGettersInBuilders;
  }
}
