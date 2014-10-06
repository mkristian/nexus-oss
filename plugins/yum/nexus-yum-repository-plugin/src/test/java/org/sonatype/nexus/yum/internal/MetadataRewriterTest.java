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
package org.sonatype.nexus.yum.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers;
import org.sonatype.sisu.litmus.testsupport.junit.TestDataRule;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.DiffMatchers.equalTo;

/**
 * {@link MetadataRewriter} UTs.
 *
 * @since 2.11
 */
public class MetadataRewriterTest
    extends TestSupport
{
  private static final String STORAGE = "/home/centos/sonatype/nexus-bundles/assemblies/nexus-pro/target/nexus/sonatype-work/nexus/storage/";

  @Rule
  public TestDataRule testData = new TestDataRule(util.resolveFile("src/test/ut-resources"));

  @Test
  public void verifyRewriteOfPrimaryLocationsAfterMerge()
      throws Exception
  {
    File workDir = new File(util.getTargetDir(), "work-" + System.currentTimeMillis());
    FileUtils.copyDirectory(testData.resolveFile("repo3"), workDir);

    Repository repository = mock(Repository.class);
    when(repository.getLocalUrl()).thenReturn(workDir.getAbsolutePath());

    MetadataRewriter.rewritePrimaryLocationsAfterMerge(
        repository,
        Arrays.asList(new File(STORAGE + "releases"), new File(STORAGE + "thirdparty"))
    );

    // check that we have a new primary.xml
    File newPrimary = new File(
        workDir,
        "repodata/2fefc2f0541419d2dcf3fc73d2b8db1cc5782d522443b51b954c4e94d5a8c001-primary.xml.gz"
    );
    assertThat(newPrimary, FileMatchers.exists());

    // check that we old primary.xml was removed
    File oldPrimary = new File(
        workDir,
        "repodata/b7bad82bc89d0c651f8aeefffa3b43e69725c6fc0cc4dd840ecb999698861428-primary.xml.gz"
    );
    assertThat(oldPrimary, not(FileMatchers.exists()));

    // compare repomd.xml content
    assertThat(
        readFileToString(new File(workDir, "repodata/repomd.xml")),
        is(equalTo(readFileToString(testData.resolveFile("repo3-result/repodata/repomd.xml"))))
    );

    // compare primary.xml content
    try (InputStream primaryIn = new GZIPInputStream(new BufferedInputStream(new FileInputStream(newPrimary)))) {
      assertThat(
          new String(IOUtils.toByteArray(primaryIn)),
          is(equalTo(readFileToString(testData.resolveFile("repo3-result/repodata/primary.xml"))))
      );
    }
  }

  @Test
  public void verifyRewriteOfPrimaryLocationsAfterProxy()
      throws Exception
  {
    File workDir = new File(util.getTargetDir(), "work-" + System.currentTimeMillis());
    FileUtils.copyDirectory(testData.resolveFile("repo5"), workDir);

    ProxyRepository repository = mock(ProxyRepository.class);
    when(repository.getLocalUrl()).thenReturn(workDir.getAbsolutePath());
    when(repository.getRemoteUrl()).thenReturn("http://localhost:8082/nexus/content/repositories/thirdparty");

    MetadataRewriter.rewritePrimaryLocationsAfterProxy(repository);

    // check that we have a new primary.xml
    File newPrimary = new File(
        workDir,
        "repodata/8ef31e4bdca995f6fe5b7a63387ddc60c718175ca6eaec9d4cadbefb81fd5828-primary.xml.gz"
    );
    assertThat(newPrimary, FileMatchers.exists());

    // check that we old primary.xml was removed
    File oldPrimary = new File(
        workDir,
        "repodata/57a84398efb9478dd2fc2d23467b55479939ff3a48f21d0beacd0b5849713f48-primary.xml.gz"
    );
    assertThat(oldPrimary, not(FileMatchers.exists()));

    // compare repomd.xml content
    assertThat(
        readFileToString(new File(workDir, "repodata/repomd.xml")),
        is(equalTo(readFileToString(testData.resolveFile("repo5-result/repodata/repomd.xml"))))
    );

    // compare primary.xml content
    try (InputStream primaryIn = new GZIPInputStream(new BufferedInputStream(new FileInputStream(newPrimary)))) {
      assertThat(
          new String(IOUtils.toByteArray(primaryIn)),
          is(equalTo(readFileToString(testData.resolveFile("repo5-result/repodata/primary.xml"))))
      );
    }
  }

  @Test
  public void verifyRemoveOfSqliteFromRepoMD()
      throws Exception
  {
    File workDir = new File(util.getTargetDir(), "work-" + System.currentTimeMillis());
    FileUtils.copyDirectory(testData.resolveFile("repo4"), workDir);

    Repository repository = mock(Repository.class);
    when(repository.getLocalUrl()).thenReturn(workDir.getAbsolutePath());

    MetadataRewriter.removeSqliteFromRepoMD(repository);

    // compare repomd.xml content
    assertThat(
        readFileToString(new File(workDir, "repodata/repomd.xml")),
        is(equalTo(readFileToString(testData.resolveFile("repo4-result/repodata/repomd.xml"))))
    );
  }

}
