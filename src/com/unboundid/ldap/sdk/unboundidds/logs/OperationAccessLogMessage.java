/*
 * Copyright 2009-2016 UnboundID Corp.
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
package com.unboundid.ldap.sdk.unboundidds.logs;



import com.unboundid.util.NotExtensible;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class provides a data structure that holds information about a log
 * message that may appear in the Directory Server access log about an
 * operation processed by the server.
 */
@NotExtensible()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public abstract class OperationAccessLogMessage
       extends AccessLogMessage
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 5311424730889643655L;



  // The message ID for this access log message.
  private final Integer messageID;

  // The operation ID for this access log message.
  private final Long operationID;

  // The message origin for this access log message.
  private final String origin;



  /**
   * Creates a new operation access log message from the provided log message.
   *
   * @param  m  The log message to be parsed as an operation access log message.
   */
  protected OperationAccessLogMessage(final LogMessage m)
  {
    super(m);

    messageID   = getNamedValueAsInteger("msgID");
    operationID = getNamedValueAsLong("op");
    origin      = getNamedValue("origin");
  }



  /**
   * Retrieves the operation ID for the associated operation.
   *
   * @return  The operation ID for the associated operation, or {@code null} if
   *          it is not included in the log message.
   */
  public final Long getOperationID()
  {
    return operationID;
  }



  /**
   * Retrieves the message ID for the associated operation.
   *
   * @return  The message ID for the associated operation, or {@code null} if
   *          it is not included in the log message.
   */
  public final Integer getMessageID()
  {
    return messageID;
  }



  /**
   * Retrieves the origin of the associated operation.  If present, it may be
   * "synchronization" if the operation is replicated, or "internal" if it is an
   * internal operation.
   *
   * @return  The origin for the associated operation, or {@code null} if it is
   *          not included in the log message.
   */
  public final String getOrigin()
  {
    return origin;
  }



  /**
   * Retrieves the operation type for the associated operation.
   *
   * @return  The operation type for this access log message.
   */
  public abstract AccessLogOperationType getOperationType();
}
