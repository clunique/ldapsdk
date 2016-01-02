/*
 * Copyright 2012-2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2012-2016 UnboundID Corp.
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
package com.unboundid.util.ssl;



import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.X509TrustManager;

import com.unboundid.util.NotMutable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.Validator;

import static com.unboundid.util.Debug.*;



/**
 * This class provides an SSL trust manager that has the ability to delegate the
 * determination about whether to trust a given certificate to one or more other
 * trust managers.  It can be configured to use a logical AND (i.e., all
 * associated trust managers must be satisfied) or a logical OR (i.e., at least
 * one of the associated trust managers must be satisfied).
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class AggregateTrustManager
       implements X509TrustManager
{
  /**
   * A pre-allocated empty certificate array.
   */
  private static final X509Certificate[] NO_CERTIFICATES =
       new X509Certificate[0];



  // Indicates whether to require all of the associated trust managers to accept
  // a presented certificate, or just to require at least one of them to accept
  // the certificate.
  private final boolean requireAllAccepted;

  // The trust managers that will be used to ultimately make the determination.
  private final List<X509TrustManager> trustManagers;



  /**
   * Creates a new aggregate trust manager with the provided information.
   *
   * @param  requireAllAccepted  Indicates whether all of the associated trust
   *                             managers must accept a presented certificate
   *                             for it to be allowed, or just at least one of
   *                             them.
   * @param  trustManagers       The set of trust managers to use to make the
   *                             determination.  It must not be {@code null} or
   *                             empty.
   */
  public AggregateTrustManager(final boolean requireAllAccepted,
                               final X509TrustManager ... trustManagers)
  {
    this(requireAllAccepted, StaticUtils.toList(trustManagers));
  }



  /**
   * Creates a new aggregate trust manager with the provided information.
   *
   * @param  requireAllAccepted  Indicates whether all of the associated trust
   *                             managers must accept a presented certificate
   *                             for it to be allowed, or just at least one of
   *                             them.
   * @param  trustManagers       The set of trust managers to use to make the
   *                             determination.  It must not be {@code null} or
   *                             empty.
   */
  public AggregateTrustManager(final boolean requireAllAccepted,
              final Collection<X509TrustManager > trustManagers)
  {
    Validator.ensureNotNull(trustManagers);
    Validator.ensureFalse(trustManagers.isEmpty(),
         "The set of associated trust managers must not be empty.");

    this.requireAllAccepted = requireAllAccepted;
    this.trustManagers = Collections.unmodifiableList(
         new ArrayList<X509TrustManager>(trustManagers));
  }



  /**
   * Indicates whether all of the associated trust managers will be required to
   * accept a given certificate for it to be considered acceptable.
   *
   * @return  {@code true} if all of the associated trust managers will be
   *          required to accept the provided certificate chain, or
   *          {@code false} if it will be acceptable for at least one trust
   *          manager to accept the chain even if one or more others do not.
   */
  public boolean requireAllAccepted()
  {
    return requireAllAccepted;
  }



  /**
   * Retrieves the set of trust managers that will be used to perform the
   * validation.
   *
   * @return  The set of trust managers that will be used to perform the
   *          validation.
   */
  public List<X509TrustManager> getAssociatedTrustManagers()
  {
    return trustManagers;
  }



  /**
   * Checks to determine whether the provided client certificate chain should be
   * trusted.
   *
   * @param  chain     The client certificate chain for which to make the
   *                   determination.
   * @param  authType  The authentication type based on the client certificate.
   *
   * @throws  CertificateException  If the provided client certificate chain
   *                                should not be trusted.
   */
  public void checkClientTrusted(final X509Certificate[] chain,
                                 final String authType)
         throws CertificateException
  {
    ArrayList<String> exceptionMessages = null;

    for (final X509TrustManager m : trustManagers)
    {
      try
      {
        m.checkClientTrusted(chain, authType);

        if (! requireAllAccepted)
        {
          return;
        }
      }
      catch (final CertificateException ce)
      {
        debugException(ce);

        if (requireAllAccepted)
        {
          throw ce;
        }
        else
        {
          if (exceptionMessages == null)
          {
            exceptionMessages = new ArrayList<String>(trustManagers.size());
          }

          exceptionMessages.add(ce.getMessage());
        }
      }
    }

    // If we've gotten here and there are one or more exception messages, then
    // it means that none of the associated trust managers accepted the
    // certificate.
    if ((exceptionMessages != null) && (! exceptionMessages.isEmpty()))
    {
      throw new CertificateException(
           StaticUtils.concatenateStrings(exceptionMessages));
    }
  }



  /**
   * Checks to determine whether the provided server certificate chain should be
   * trusted.
   *
   * @param  chain     The server certificate chain for which to make the
   *                   determination.
   * @param  authType  The key exchange algorithm used.
   *
   * @throws  CertificateException  If the provided server certificate chain
   *                                should not be trusted.
   */
  public void checkServerTrusted(final X509Certificate[] chain,
                                 final String authType)
         throws CertificateException
  {
    ArrayList<String> exceptionMessages = null;

    for (final X509TrustManager m : trustManagers)
    {
      try
      {
        m.checkServerTrusted(chain, authType);

        if (! requireAllAccepted)
        {
          return;
        }
      }
      catch (final CertificateException ce)
      {
        debugException(ce);

        if (requireAllAccepted)
        {
          throw ce;
        }
        else
        {
          if (exceptionMessages == null)
          {
            exceptionMessages = new ArrayList<String>(trustManagers.size());
          }

          exceptionMessages.add(ce.getMessage());
        }
      }
    }

    // If we've gotten here and there are one or more exception messages, then
    // it means that none of the associated trust managers accepted the
    // certificate.
    if ((exceptionMessages != null) && (! exceptionMessages.isEmpty()))
    {
      throw new CertificateException(
           StaticUtils.concatenateStrings(exceptionMessages));
    }
  }



  /**
   * Retrieves the accepted issuer certificates for this trust manager.  This
   * will always return an empty array.
   *
   * @return  The accepted issuer certificates for this trust manager.
   */
  public X509Certificate[] getAcceptedIssuers()
  {
    return NO_CERTIFICATES;
  }
}
