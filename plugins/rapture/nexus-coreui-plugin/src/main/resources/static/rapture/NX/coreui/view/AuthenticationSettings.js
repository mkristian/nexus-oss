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
/*global Ext, NX*/

/**
 * Authentication settings fields.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.AuthenticationSettings', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-authenticationsettings',

  namePrefix: undefined,

  defaults: {
    xtype: 'textfield'
  },

  /**
   * @override
   */
  initComponent: function () {
    var me = this,
        namePrefix = me.namePrefix ? me.namePrefix + 'A' : 'a';

    me.items = [
      {
        name: namePrefix + 'uthUsername',
        fieldLabel: 'Username',
        allowBlank: false
      },
      {
        xtype: 'nx-password',
        name: namePrefix + 'uthPassword',
        fieldLabel: 'Password'
      },
      {
        xtype: 'label',
        html: 'Windows NT Lan Manager options:'
      },
      {
        name: namePrefix + 'uthNtlmHost',
        fieldLabel: 'NT LAN Host',
        helpText: 'Lan Manager host-name.'
      },
      {
        name: namePrefix + 'uthNtlmDomain',
        fieldLabel: 'NT LAN Manager Domain',
        helpText: 'Lan Manager domain-name.'
      }
    ];

    me.callParent(arguments);
  }

});