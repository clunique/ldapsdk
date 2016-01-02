/*
 * Copyright 2008-2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2008-2016 UnboundID Corp.
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
package com.unboundid.ldap.matchingrules;



import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.Extensible;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;



/**
 * This class provides a common matching rule framework that may be extended by
 * matching rule implementations in which equality, ordering, and substring
 * matching can all be made based on byte-for-byte comparisons of the normalized
 * value, for values that are considered acceptable by the
 * {@link MatchingRule#normalize} and {@link MatchingRule#normalizeSubstring}
 * methods.
 */
@Extensible()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public abstract class SimpleMatchingRule
       extends MatchingRule
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -7221506185552250694L;



  /**
   * {@inheritDoc}
   */
  @Override()
  public boolean valuesMatch(final ASN1OctetString value1,
                             final ASN1OctetString value2)
         throws LDAPException
  {
    return normalize(value1).equals(normalize(value2));
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public boolean matchesSubstring(final ASN1OctetString value,
                                  final ASN1OctetString subInitial,
                                  final ASN1OctetString[] subAny,
                                  final ASN1OctetString subFinal)
         throws LDAPException
  {
    final byte[] normValue = normalize(value).getValue();

    int pos = 0;
    if (subInitial != null)
    {
      final byte[] normSubInitial =
           normalizeSubstring(subInitial, SUBSTRING_TYPE_SUBINITIAL).getValue();
      if (normValue.length < normSubInitial.length)
      {
        return false;
      }

      for (int i=0; i < normSubInitial.length; i++)
      {
        if (normValue[i] != normSubInitial[i])
        {
          return false;
        }
      }

      pos = normSubInitial.length;
    }

    if (subAny != null)
    {
      final byte[][] normSubAny = new byte[subAny.length][];
      for (int i=0; i < subAny.length; i++)
      {
        normSubAny[i] =
             normalizeSubstring(subAny[i],SUBSTRING_TYPE_SUBANY).getValue();
      }

      for (final byte[] b : normSubAny)
      {
        if (b.length == 0)
        {
          continue;
        }

        boolean match = false;
        final int subEndLength = normValue.length - b.length;
        while (pos <= subEndLength)
        {
          match = true;
          for (int i=0; i < b.length; i++)
          {
            if (normValue[pos+i] != b[i])
            {
              match = false;
              break;
            }
          }

          if (match)
          {
            pos += b.length;
            break;
          }
          else
          {
            pos++;
          }
        }

        if (! match)
        {
          return false;
        }
      }
    }

    if (subFinal != null)
    {
      final byte[] normSubFinal =
           normalizeSubstring(subFinal, SUBSTRING_TYPE_SUBFINAL).getValue();
      int finalStartPos = normValue.length - normSubFinal.length;
      if (finalStartPos < pos)
      {
        return false;
      }

      for (int i=0; i < normSubFinal.length; i++,finalStartPos++)
      {
        if (normValue[finalStartPos] != normSubFinal[i])
        {
          return false;
        }
      }
    }

    return true;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public int compareValues(final ASN1OctetString value1,
                           final ASN1OctetString value2)
         throws LDAPException
  {
    final byte[] normValue1 = normalize(value1).getValue();
    final byte[] normValue2 = normalize(value2).getValue();

    final int minLength = Math.min(normValue1.length, normValue2.length);
    for (int i=0; i < minLength; i++)
    {
      final int b1 = normValue1[i] & 0xFF;
      final int b2 = normValue2[i] & 0xFF;

      if (b1 < b2)
      {
        return -1;
      }
      else if (b1 > b2)
      {
        return 1;
      }
    }

    // If we've gotten here, then it means that all of the bytes they had in
    // common are the same.  At this point, the shorter of the two should be
    // ordered first, or return zero if they're the same length.
    return normValue1.length - normValue2.length;
  }
}
