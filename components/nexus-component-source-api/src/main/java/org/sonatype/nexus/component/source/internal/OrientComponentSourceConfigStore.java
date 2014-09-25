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
package org.sonatype.nexus.component.source.internal;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.api.config.ComponentConfigId;
import org.sonatype.nexus.component.source.api.config.ComponentSourceConfig;
import org.sonatype.nexus.component.source.api.config.ComponentSourceConfigStore;

/**
 * A {@link ComponentSourceConfigStore} that persists source configs to the "config" orient DB instance.
 *
 * TODO: The entire implementation.
 *
 * @since 3.0
 */
@Named("orient")
@Singleton
public class OrientComponentSourceConfigStore
    implements ComponentSourceConfigStore
{
  @Override
  public ComponentConfigId add(final ComponentSourceConfig config) throws IOException {
    return null;
  }

  @Override
  public void update(final ComponentConfigId id, final ComponentSourceConfig config) throws IOException {

  }

  @Override
  public void remove(final ComponentConfigId id) throws IOException {

  }

  @Override
  public Map<ComponentConfigId, ComponentSourceConfig> getAll() throws IOException {
    return null;
  }

  @Nullable
  @Override
  public ComponentSourceConfig get(final String sourceName) throws IOException {
    return null;
  }
}
