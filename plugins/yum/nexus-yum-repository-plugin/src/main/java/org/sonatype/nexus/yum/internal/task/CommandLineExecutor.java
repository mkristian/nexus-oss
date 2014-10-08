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
package org.sonatype.nexus.yum.internal.task;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.gossip.Level;
import org.sonatype.gossip.support.LoggingOutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since yum 3.0
 */
@Named
@Singleton
public class CommandLineExecutor
{

  private static final Logger LOG = LoggerFactory.getLogger(CommandLineExecutor.class);

  public int exec(String command)
      throws IOException
  {
    LOG.debug("Execute command : {}", command);

    CommandLine cmdLine = CommandLine.parse(command);
    DefaultExecutor executor = new DefaultExecutor();
    executor.setStreamHandler(new PumpStreamHandler(
        new LoggingOutputStream(LOG, Level.DEBUG),
        new LoggingOutputStream(LOG, Level.ERROR)
    ));

    int exitValue = executor.execute(cmdLine);
    LOG.debug("Execution finished with exit code : {}", exitValue);
    return exitValue;
  }
}
