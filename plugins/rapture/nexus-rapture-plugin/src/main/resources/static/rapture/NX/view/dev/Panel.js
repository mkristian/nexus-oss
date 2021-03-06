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
/*global Ext*/

/**
 * The developer panel.
 *
 * @since 3.0
 */
Ext.define('NX.view.dev.Panel', {
  extend: 'Ext.panel.Panel',
  requires: [
    'NX.view.dev.Buttons'
  ],
  alias: 'widget.nx-dev-panel',

  title: 'Developer',
  glyph: 'xf188@FontAwesome', // fa-bug
  ui: 'developer',
  stateful: true,
  stateId: 'nx-dev-panel',

  layout: 'fit',
  items: {
    xtype: 'tabpanel',
    tabPosition: 'bottom',

    items: [
      { xtype: 'nx-dev-tests' },
      { xtype: 'nx-dev-buttons' },
      { xtype: 'nx-dev-icons' },
      { xtype: 'nx-dev-features' },
      { xtype: 'nx-dev-permissions' },
      { xtype: 'nx-dev-messages' },
      { xtype: 'nx-dev-state' },
      { xtype: 'nx-dev-stores' }
    ]
  }
});
