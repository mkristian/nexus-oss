/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.ruby;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.content.Location;
import org.sonatype.nexus.client.rest.BaseUrl;
import org.sonatype.nexus.ruby.BundleRunner;
import org.sonatype.nexus.ruby.GemRunner;
import org.sonatype.nexus.testsuite.support.NexusRunningITSupport;
import org.sonatype.sisu.filetasks.FileTaskBuilder;
import org.sonatype.sisu.goodies.common.Time;

import org.hamcrest.Matcher;
import org.jruby.embed.ScriptingContainer;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

public abstract class RubyNexusRunningITSupport
    extends NexusRunningITSupport
{
  @Parameters
  public static Collection<String[]> data() {
    String[][] data = new String[][]{
        {"gemshost"},
        {"gemsproxy"},
        {"gemshostgroup"},
        {"gemsproxygroup"},
        {"gemsgroup"}
    };
    return Arrays.asList(data);
  }

  @Inject
  protected FileTaskBuilder overlays;

  protected GemRunner gemRunner;

  protected ScriptingContainer ruby;

  protected final String repoId;

  private BundleRunner bundleRunner;

  public RubyNexusRunningITSupport(String nexusBundleCoordinates, String repoId) {
    super(nexusBundleCoordinates);
    this.repoId = repoId;
  }

  public RubyNexusRunningITSupport(String repoId) {
    this(null, repoId);
  }

  private ScriptingContainer ruby() {
    if (this.ruby == null) {
      this.ruby = createScriptingContainer();
    }
    return this.ruby;
  }

  protected ScriptingContainer createScriptingContainer() {
    return new ITestJRubyScriptingContainer(testData().resolveFile(".gem").getParent());
  }

  protected GemRunner gemRunner() {
    if (gemRunner == null) {
      gemRunner = createGemRunner();
    }
    return gemRunner;
  }

  protected BundleRunner bundleRunner() {
    if (bundleRunner == null) {
      bundleRunner = createBundleRunner();
    }
    return bundleRunner;
  }

  GemRunner createGemRunner() {
    BaseUrl base = client().getConnectionInfo().getBaseUrl();
    return new GemRunner(ruby(), base.toString() + "content/repositories/");
  }

  BundleRunner createBundleRunner() {
    return new BundleRunner(ruby());
  }

  protected File assertFileDownload(String name, Matcher<Boolean> matcher) {
    File target = new File(util.createTempDir(), "null");

    try {
      client().getSubsystem(Content.class).download(new Location(repoId, name), target);
    }
    catch (Exception e) {
      // just ignore it and let matcher test
    }
    // from version 2.4.0-03 onwards count empty files as non-existing
    assertThat(name, target.exists() && target.length() > 0, matcher);

    target.deleteOnExit();

    return target;
  }

  protected void assertFileRemoval(String name, Matcher<Boolean> matcher) {
    try {
      client().getSubsystem(Content.class).delete(new Location(repoId, name));
      assertThat(name, true, matcher);
    }
    catch (Exception e) {
      // just ignore it and let matcher test
      assertThat(name, false, matcher);
    }
  }

  protected String nexusXML() {
    return "nexus-" + repoId + ".xml";
  }

  @Override
  protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
    return configuration
        .addPlugins(
            artifactResolver().resolvePluginFromDependencyManagement(
                "org.sonatype.nexus.plugins", "nexus-ruby-plugin"
            )
        )
        .addOverlays(
            overlays.copy()
                .file(file(testData().resolveFile(nexusXML())))
                .to().file(path("sonatype-work/nexus/conf/nexus.xml"))
        )
        .setStartTimeout(Time.minutes(3).toSecondsI())
        .setLogLevel("DEBUG")
        .setPort(4711);
  }

  protected File installLatestNexusGem() {
    return installLatestNexusGem(false);
  }

  protected File installLatestNexusGem(boolean withBundler) {

    //nexus gem
    File nexusGem;
    try {
      nexusGem = artifactResolver().resolveFromDependencyManagement("rubygems", "nexus", "gem", null, null, null);
    }
    catch (RuntimeException e) {
      throw new RuntimeException("maybe you forgot to install nexus gem", e);
    }

    if (withBundler) {
      // install nexus + bundler gem
      File bundlerGem = testData().resolveFile("bundler.gem");
      gemRunner().install(nexusGem, bundlerGem);
    }
    else {
      // install nexus gem
      gemRunner().install(nexusGem);
    }
    assertThat(gemRunner().list().toString(), containsString("nexus ("));

    return nexusGem;
  }
}
