package com.github.timvlaer.thrifty;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ThriftyCompilerMojoTest {

  private ThriftyCompilerMojo mojo = new ThriftyCompilerMojo();

  @Rule
  public TemporaryFolder outputFolder = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private MavenProject project;

  @Before
  public void setUp() {
    mojo.setProject(project);
    mojo.setOutputDirectory(outputFolder.getRoot().getAbsolutePath());
  }

  @Test
  public void execute() throws Exception {
    mojo.setThriftFiles(new File[]{new File("src/test/resources/testcase.thrift")});

    mojo.execute();

    Mockito.verify(project).addCompileSourceRoot(outputFolder.getRoot().getAbsolutePath());

    File resultFile = new File(outputFolder.getRoot(), "com/sentiance/thrift/LocationFix.java");
    assertThat(resultFile).exists();

    String result = new String(Files.readAllBytes(resultFile.toPath()), UTF_8);
    assertThat(result).contains("package com.sentiance.thrift;");
    assertThat(result).contains("public final class LocationFix implements Struct");
  }

}