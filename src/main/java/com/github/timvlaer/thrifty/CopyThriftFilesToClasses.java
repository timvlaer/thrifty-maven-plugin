package com.github.timvlaer.thrifty;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CopyThriftFilesToClasses {
  private static final Pattern THRIFT_INCLUDE_PATTERN = Pattern.compile("^\\s*include\\s*['\"]([a-zA-Z0-9._\\-/]+)['\"]$");

  private final MavenProject project;
  private final Log log;

  public CopyThriftFilesToClasses(MavenProject project, Log log) {
    this.project = project;
    this.log = log;
  }

  public void findAllThriftFilesAndAddToClasses(List<String> entryThriftFiles) {
    log.debug("Add thrift files to classes directory");
    try {
      Path outputDirectory = Paths.get(project.getBuild().getOutputDirectory());
      Files.createDirectories(outputDirectory);

      for (Path entryThriftFile : entryThriftFiles.stream().map(Paths::get).collect(Collectors.toSet())) {
        Set<Path> includeGraph = getGraph(entryThriftFile);

        Path commonRoot = includeGraph.iterator().next().getParent();
        while (!allPathsStartWith(includeGraph, commonRoot)) {
          commonRoot = commonRoot.getParent();
        }

        for (Path thriftFile : includeGraph) {
          Path resultFile = outputDirectory
              .resolve(commonRoot.relativize(thriftFile.getParent()))
              .normalize()
              .resolve(thriftFile.getFileName());
          log.debug("Copy: " + thriftFile + " => " + resultFile);
          Files.createDirectories(resultFile.getParent());
          Files.copy(thriftFile, resultFile, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Set<Path> getGraph(Path thriftFile) {
    Set<Path> includeGraph = new HashSet<>();
    includeGraph.add(thriftFile);

    Set<Path> includes = getIncludesFor(thriftFile);
    includeGraph.addAll(includes);

    for (Path include : includes) {
      includeGraph.addAll(getGraph(include));
    }

    return includeGraph;
  }

  private Set<Path> getIncludesFor(Path thriftFile) {
    try {
      return Files.readAllLines(thriftFile).stream()
          .filter(l -> l.contains("include"))
          .map(THRIFT_INCLUDE_PATTERN::matcher)
          .filter(Matcher::matches)
          .map(matcher -> matcher.group(1))
          .map(include -> thriftFile.getParent().resolve(include).normalize())
          .collect(Collectors.toSet());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean allPathsStartWith(Set<Path> includeGraph, Path commonRoot) {
    return includeGraph.stream().allMatch(p -> p.startsWith(commonRoot));
  }


}
