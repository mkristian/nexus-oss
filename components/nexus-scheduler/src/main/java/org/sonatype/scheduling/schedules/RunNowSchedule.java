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
package org.sonatype.scheduling.schedules;

import java.util.Date;

import org.sonatype.scheduling.iterators.RunNowSchedulerIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;

public class RunNowSchedule
    extends AbstractSchedule
{
  public RunNowSchedule() {
    this(new Date(System.currentTimeMillis() + 500));
  }

  private RunNowSchedule(Date date) {
    super(date, date);
  }

  @Override
  protected SchedulerIterator createIterator() {
    return new RunNowSchedulerIterator();
  }
}
