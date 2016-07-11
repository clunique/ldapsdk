/*
 * Copyright 2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2016 UnboundID Corp.
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
package com.unboundid.ldap.sdk.experimental;



import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ModifyDNRequest;
import com.unboundid.ldap.sdk.OperationType;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.NotMutable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.ldap.sdk.experimental.ExperimentalMessages.*;



/**
 * This class represents an entry that holds information about a modify DN
 * operation processed by an LDAP server, as per the specification described in
 * draft-chu-ldap-logschema-00.
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class DraftChuLDAPLogSchema00ModifyDNEntry
       extends DraftChuLDAPLogSchema00Entry
{
  /**
   * The name of the attribute used to hold the value of the delete old RDN
   * flag.
   */
  public static final String ATTR_DELETE_OLD_RDN = "reqDeleteOldRDN";



  /**
   * The name of the attribute used to hold the new RDN value.
   */
  public static final String ATTR_NEW_RDN = "reqNewRDN";



  /**
   * The name of the attribute used to hold the new superior DN value.
   */
  public static final String ATTR_NEW_SUPERIOR_DN = "reqNewSuperior";



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 5891004379538957384L;



  // The delete old RDN value.
  private final boolean deleteOldRDN;

  // The new RDN.
  private final String newRDN;

  // The new superior DN.
  private final String newSuperiorDN;



  /**
   * Creates a new instance of this modify DN access log entry from the provided
   * entry.
   *
   * @param  entry  The entry used to create this modify DN access log entry.
   *
   * @throws  LDAPException  If the provided entry cannot be decoded as a valid
   *                         modify DN access log entry as per the specification
   *                         contained in draft-chu-ldap-logschema-00.
   */
  public DraftChuLDAPLogSchema00ModifyDNEntry(final Entry entry)
         throws LDAPException
  {
    super(entry, OperationType.MODIFY_DN);


    // Get the new RDN.
    newRDN = entry.getAttributeValue(ATTR_NEW_RDN);
    if (newRDN == null)
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_LOGSCHEMA_DECODE_MISSING_REQUIRED_ATTR.get(entry.getDN(),
                ATTR_NEW_RDN));
    }


    // Get the delete old RDN flag.
    final String deleteOldRDNString =
         entry.getAttributeValue(ATTR_DELETE_OLD_RDN);
    if (deleteOldRDNString == null)
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_LOGSCHEMA_DECODE_MISSING_REQUIRED_ATTR.get(entry.getDN(),
                ATTR_DELETE_OLD_RDN));
    }

    final String lowerDeleteOldRDN =
         StaticUtils.toLowerCase(deleteOldRDNString);
    if (lowerDeleteOldRDN.equals("true"))
    {
      deleteOldRDN = true;
    }
    else if (lowerDeleteOldRDN.equals("false"))
    {
      deleteOldRDN = false;
    }
    else
    {
      throw new LDAPException(ResultCode.DECODING_ERROR,
           ERR_LOGSCHEMA_DECODE_MODIFY_DN_DELETE_OLD_RDN_ERROR.get(
                entry.getDN(), ATTR_DELETE_OLD_RDN, deleteOldRDNString));
    }


    // Get the new superior DN.
    newSuperiorDN = entry.getAttributeValue(ATTR_NEW_SUPERIOR_DN);
  }



  /**
   * Retrieves the new RDN for the modify DN request described by this modify DN
   * access log entry.
   *
   * @return  The new RDN for the modify DN request described by this modify DN
   *          access log entry.
   */
  public String getNewRDN()
  {
    return newRDN;
  }



  /**
   * Retrieves the value of the "delete old RDN" flag for the modify DN request
   * described by this modify DN access log entry.
   *
   * @return  {@code true} if the modify request indicated that old RDN
   *          attribute values should be removed from the entry, or
   *          {@code false} if old RDN attribute values should be preserved.
   */
  public boolean deleteOldRDN()
  {
    return deleteOldRDN;
  }



  /**
   * Retrieves the new superior DN for the modify DN request described by this
   * modify DN access log entry, if any.
   *
   * @return  The new superior DN for the modify DN request described by this
   *          modify DN access log entry, or {@code null} if there is no new
   *          superior DN.
   */
  public String getNewSuperiorDN()
  {
    return newSuperiorDN;
  }



  /**
   * Retrieves a {@code ModifyDNRequest} created from this modify DN access log
   * entry.
   *
   * @return  The {@code ModifyDNRequest} created from this modify DN access log
   *          entry.
   */
  public ModifyDNRequest toModifyDNRequest()
  {
    return new ModifyDNRequest(getTargetEntryDN(), newRDN, deleteOldRDN,
         newSuperiorDN, getRequestControlArray());
  }
}
