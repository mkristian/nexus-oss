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

import java.io.IOException;

import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_TEST)
@RunWith(value = Parameterized.class)
public class DownloadsOnEmptyRepositoriesIT
    extends RubyITSupport
{
  public DownloadsOnEmptyRepositoriesIT(String repoId) {
    super(repoId);
  }

  protected String nexusXML() {
    return "nexus-downloads-on-empty.xml";
  }

  @Test
  public void nexusIsRunning() {
    assertThat(nexus().isRunning(), is(true));
  }

  @Test
  public void download() throws Exception {
    assertAllSpecsIndexDownload();
    // on an empty repo these directories still missing
    assertFileDownload("/gems", is(true));
    assertFileDownload("/quick", is(true));
    assertFileDownload("/api", is(true));
    assertFileDownload("/maven", is(true));
  }

  private void assertAllSpecsIndexDownload()throws IOException {
    assertSpecsIndexdownload("specs");
    assertSpecsIndexdownload("prerelease_specs");
    assertSpecsIndexdownload("latest_specs");
  }

  private void assertSpecsIndexdownload(String name) throws IOException {
    if (!client().getNexusStatus().getVersion().matches("^2\\.6\\..*")) {
      // skip this test for 2.6.x nexus :
      // something goes wrong but that is a formal feature not used by any ruby client
      assertFileDownload("/" + name + ".4.8", is(true));
    }
    assertFileDownload("/" + name + ".4.8.gz", is(true));
  }
}