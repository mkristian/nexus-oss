/*
 * Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package org.sonatype.nexus.ruby.client.internal;

import org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository.JerseyGroupRepository;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.ruby.client.RubyGroupRepository;

public class JerseyRubyGroupRepository
    extends JerseyGroupRepository<RubyGroupRepository>
    implements RubyGroupRepository
{
  static final String PROVIDER_ROLE = "org.sonatype.nexus.proxy.repository.GroupRepository";

  static final String PROVIDER = "rubygems-group";

  public JerseyRubyGroupRepository(final JerseyNexusClient nexusClient, final String id) {
    super(nexusClient, id);
  }

  public JerseyRubyGroupRepository(final JerseyNexusClient nexusClient,
                                   final RepositoryGroupResource settings)
  {
    super(nexusClient, settings);
  }

  @Override
  protected RepositoryGroupResource createSettings() {
    final RepositoryGroupResource settings = super.createSettings();

    settings.setProviderRole(JerseyRubyGroupRepository.PROVIDER_ROLE);
    settings.setProvider(JerseyRubyGroupRepository.PROVIDER);

    return settings;
  }
}
