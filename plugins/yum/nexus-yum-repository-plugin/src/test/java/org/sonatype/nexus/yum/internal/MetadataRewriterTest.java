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
import java.util.zip.GZIPInputStream;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.litmus.testsupport.junit.TestDataRule;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
    when(repository.getId()).thenReturn("test");
    when(repository.retrieveItem(eq(false), any(ResourceStoreRequest.class))).thenAnswer(new Answer<StorageFileItem>()
    {
      @Override
      public StorageFileItem answer(final InvocationOnMock invocationOnMock) throws Throwable {
        ResourceStoreRequest request = (ResourceStoreRequest) invocationOnMock.getArguments()[1];
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

    ArgumentCaptor<StorageFileItem> items = ArgumentCaptor.forClass(StorageFileItem.class);
    ArgumentCaptor<ResourceStoreRequest> deleteRequests = ArgumentCaptor.forClass(ResourceStoreRequest.class);
    verify(repository).deleteItem(eq(false), deleteRequests.capture());
    verify(repository, times(2)).storeItem(eq(false), items.capture());

    // verify that old primary.xml was deleted
    assertThat(deleteRequests.getAllValues().get(0).getRequestPath(), is(
        "/repodata/b7bad82bc89d0c651f8aeefffa3b43e69725c6fc0cc4dd840ecb999698861428-primary.xml.gz"
    ));

    // check that first stored is primary.xml with a new name
    assertThat(items.getAllValues().get(0).getPath(), is(
        "/repodata/2fefc2f0541419d2dcf3fc73d2b8db1cc5782d522443b51b954c4e94d5a8c001-primary.xml.gz"
    ));

    // check that second stored is repomd.xml
    assertThat(items.getAllValues().get(1).getPath(), is("/repodata/repomd.xml"));

    // compare primary.xml content
    try (InputStream primaryIn = new GZIPInputStream(items.getAllValues().get(0).getInputStream())) {
      assertThat(
          IOUtils.toString(primaryIn),
          is(equalTo(readFileToString(testData.resolveFile("repo3-result/repodata/primary.xml"))))
      );
    }

    // compare repomd.xml content
    assertThat(
        IOUtils.toString(items.getAllValues().get(1).getInputStream()),
        is(equalTo(readFileToString(testData.resolveFile("repo3-result/repodata/repomd.xml"))))
    );
  }

  @Test
  public void verifyRewriteOfPrimaryLocationsAfterProxy()
      throws Exception
  {
    ProxyRepository repository = mock(ProxyRepository.class);
    when(repository.getId()).thenReturn("test");
    when(repository.getRemoteUrl()).thenReturn("http://localhost:8082/nexus/content/repositories/thirdparty");
    when(repository.retrieveItem(eq(false), any(ResourceStoreRequest.class))).thenAnswer(new Answer<StorageFileItem>()
    {
      @Override
      public StorageFileItem answer(final InvocationOnMock invocationOnMock) throws Throwable {
        ResourceStoreRequest request = (ResourceStoreRequest) invocationOnMock.getArguments()[1];
        StorageFileItem storageFileItem = mock(StorageFileItem.class);
        when(storageFileItem.getInputStream()).thenReturn(
            new FileInputStream(testData.resolveFile("repo5" + request.getRequestPath()))
        );
        return storageFileItem;
      }
    });

    MetadataRewriter.rewritePrimaryLocationsAfterProxy(repository);

    ArgumentCaptor<StorageFileItem> items = ArgumentCaptor.forClass(StorageFileItem.class);
    ArgumentCaptor<ResourceStoreRequest> deleteRequests = ArgumentCaptor.forClass(ResourceStoreRequest.class);
    verify(repository).deleteItem(eq(false), deleteRequests.capture());
    verify(repository, times(2)).storeItem(eq(false), items.capture());

    // verify that old primary.xml was deleted
    assertThat(deleteRequests.getAllValues().get(0).getRequestPath(), is(
        "/repodata/57a84398efb9478dd2fc2d23467b55479939ff3a48f21d0beacd0b5849713f48-primary.xml.gz"
    ));

    // check that first stored is primary.xml with a new name
    assertThat(items.getAllValues().get(0).getPath(), is(
        "/repodata/8ef31e4bdca995f6fe5b7a63387ddc60c718175ca6eaec9d4cadbefb81fd5828-primary.xml.gz"
    ));

    // check that second stored is repomd.xml
    assertThat(items.getAllValues().get(1).getPath(), is("/repodata/repomd.xml"));

    // compare primary.xml content
    try (InputStream primaryIn = new GZIPInputStream(items.getAllValues().get(0).getInputStream())) {
      assertThat(
          IOUtils.toString(primaryIn),
          is(equalTo(readFileToString(testData.resolveFile("repo5-result/repodata/primary.xml"))))
      );
    }

    // compare repomd.xml content
    assertThat(
        IOUtils.toString(items.getAllValues().get(1).getInputStream()),
        is(equalTo(readFileToString(testData.resolveFile("repo5-result/repodata/repomd.xml"))))
    );
  }

  @Test
  public void verifyRemoveOfSqliteFromRepoMD()
      throws Exception
  {
    Repository repository = mock(Repository.class);
    when(repository.getId()).thenReturn("test");
    when(repository.retrieveItem(eq(false), any(ResourceStoreRequest.class))).thenAnswer(new Answer<StorageFileItem>()
    {
      @Override
      public StorageFileItem answer(final InvocationOnMock invocationOnMock) throws Throwable {
        ResourceStoreRequest request = (ResourceStoreRequest) invocationOnMock.getArguments()[1];
        StorageFileItem storageFileItem = mock(StorageFileItem.class);
        when(storageFileItem.getInputStream()).thenReturn(
            new FileInputStream(testData.resolveFile("repo4" + request.getRequestPath()))
        );
        return storageFileItem;
      }
    });

    MetadataRewriter.removeSqliteFromRepoMD(repository);

    ArgumentCaptor<StorageFileItem> items = ArgumentCaptor.forClass(StorageFileItem.class);
    verify(repository).storeItem(eq(false), items.capture());

    // check that repomd.xml is stored
    assertThat(items.getAllValues().get(0).getPath(), is("/repodata/repomd.xml"));

    // compare repomd.xml content
    assertThat(
        IOUtils.toString(items.getAllValues().get(0).getInputStream()),
        is(equalTo(readFileToString(testData.resolveFile("repo4-result/repodata/repomd.xml"))))
    );
  }

}
