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
/**
 * Verify SMTP connection window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.system.VerifySMTPConnection', {
  extend: 'Ext.Window',
  alias: 'widget.nx-coreui-system-verifysmtpconnection',

  title: 'Verify SMTP connection',
  defaultFocus: 'email',

  layout: 'fit',
  autoShow: true,
  constrain: true,
  resizable: false,
  width: 500,
  border: false,
  modal: true,

  items: [
    {
      xtype: 'form',
      bodyPadding: 10,
      defaults: {
        labelSeparator: '',
        labelWidth: 40,
        labelAlign: 'right',
        anchor: '100%'
      },
      items: [
        {
          xtype: 'panel',
          layout: 'hbox',
          style: {
            marginBottom: '10px'
          },
          // TODO Style
          items: [
            { xtype: 'component', html: NX.Icons.img('verifysmtpconnection', 'x32') },
            { xtype: 'label', html: 'Please enter an email address which will receive the test email message.',
              margin: '0 0 0 5'
            }
          ]
        },
        {
          xtype: 'nx-email',
          name: 'email',
          itemId: 'email',
          fieldLabel: 'E-mail'
        }
      ],

      buttonAlign: 'left',
      buttons: [
        {
          text: 'Verify',
          action: 'verify',
          formBind: true,
          bindToEnter: true,
          ui: 'primary',
          glyph: 'xf003@FontAwesome' /* fa-envelope-o */
        },
        {
          text: 'Cancel',
          handler: function(){
            this.up('window').close();
          }
        }
      ]
    }
  ]

});