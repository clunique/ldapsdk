/*
 * Copyright 2008-2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2015-2016 UnboundID Corp.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk.unboundidds.monitors;



import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.unboundid.util.NotMutable;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class defines a data structure that can hold information about a thread
 * stack trace read from the UnboundID Directory Server's stack trace monitor.
 * The information available in a thread stack trace includes:
 * <UL>
 *   <LI>The name of the thread.  This is generally a user-friendly string that
 *       indicates what that thread does within the server.</LI>
 *   <LI>The thread ID that is assigned to the thread by the JVM.</LI>
 *   <LI>The stack trace frames for that thread as a list of
 *       {@link StackTraceElement} objects.</LI>
 * </UL>
 * See the documentation in the {@link StackTraceMonitorEntry} class for
 * information about accessing the Directory Server stack trace.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class ThreadStackTrace
       implements Serializable
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 5032934844534051999L;



  // The thread ID for this thread.
  private final int threadID;

  // The list of stack trace elements for the thread.
  private final List<StackTraceElement> stackTraceElements;

  // The name for this thread.
  private final String threadName;



  /**
   * Creates a new thread stack trace with the provided information.
   *
   * @param  threadID            The thread ID for the associated thread.
   * @param  threadName          The name for the associated thread.
   * @param  stackTraceElements  A list of the stack trace elements for the
   *                             associated thread.  It may be empty if no stack
   *                             trace was available.
   */
  public ThreadStackTrace(final int threadID, final String threadName,
                          final List<StackTraceElement> stackTraceElements)
  {
    this.threadID           = threadID;
    this.threadName         = threadName;
    this.stackTraceElements = Collections.unmodifiableList(stackTraceElements);
  }



  /**
   * Retrieves the thread ID for the associated thread.
   *
   * @return  The thread ID for the associated thread.
   */
  public int getThreadID()
  {
    return threadID;
  }



  /**
   * Retrieves the name of the associated thread.
   *
   * @return  The name of the associated thread.
   */
  public String getThreadName()
  {
    return threadName;
  }



  /**
   * Retrieves the list of stack trace elements for the associated thread.
   *
   * @return  The list of stack trace elements for the associated thread, or an
   *          empty list if no stack trace was available.
   */
  public List<StackTraceElement> getStackTraceElements()
  {
    return stackTraceElements;
  }
}
