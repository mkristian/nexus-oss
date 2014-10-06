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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.litmus.testsupport.junit.TestDataRule;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    Repository repository = mock(Repository.class);
    when(repository.retrieveItem(any(ResourceStoreRequest.class))).thenAnswer(new Answer<StorageFileItem>()
    {
      @Override
      public StorageFileItem answer(final InvocationOnMock invocationOnMock) throws Throwable {
        ResourceStoreRequest request = (ResourceStoreRequest) invocationOnMock.getArguments()[0];
        StorageFileItem storageFileItem = mock(StorageFileItem.class);
        when(storageFileItem.getInputStream()).thenReturn(
            new FileInputStream(testData.resolveFile("repo3" + request.getRequestPath()))
        );
        return storageFileItem;
      }
    });
    MetadataRewriter.rewritePrimaryLocationsAfterMerge(
        repository,
        Arrays.asList(new File(STORAGE + "releases"), new File(STORAGE + "thirdparty"))
    );

    ArgumentCaptor<ResourceStoreRequest> requests = ArgumentCaptor.forClass(ResourceStoreRequest.class);
    ArgumentCaptor<InputStream> streams = ArgumentCaptor.forClass(InputStream.class);
    verify(repository, times(2)).storeItem(requests.capture(), streams.capture(), Mockito.any(Map.class));

    // check that first stored is primary.xml with a new name
    assertThat(requests.getAllValues().get(0).getRequestPath(), is(
        "/repodata/2fefc2f0541419d2dcf3fc73d2b8db1cc5782d522443b51b954c4e94d5a8c001-primary.xml.gz"
    ));

    // check that second stored is repomd.xml
    assertThat(requests.getAllValues().get(1).getRequestPath(), is("/repodata/repomd.xml"));

    // compare primary.xml content
    assertThat(
        IOUtils.toString(streams.getAllValues().get(0)),
        is(equalTo(readFileToString(testData.resolveFile("repo3/repodata/primary-result.xml"))))
    );

    // compare repomd.xml content
    assertThat(
        IOUtils.toString(streams.getAllValues().get(1)),
        is(equalTo(readFileToString(testData.resolveFile("repo3/repodata/repomd-result.xml"))))
    );
  }

}
