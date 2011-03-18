/*
 * Copyright 2011 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2011 UnboundID Corp.
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
package com.unboundid.ldap.listener;



import java.net.InetAddress;
import javax.net.SocketFactory;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Debug;
import com.unboundid.util.NotMutable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import static com.unboundid.ldap.listener.ListenerMessages.*;



/**
 * This class provides a data structure that can be used to configure a
 * listener for use in the in-memory directory server.  Each in-memory directory
 * server instance has the ability to have multiple listeners, and those
 * listeners may have different settings (e.g., listen on one port for
 * unencrypted LDAP communication with optional support for StartTLS, and listen
 * on a separate port for SSL-encrypted communication).
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class InMemoryListenerConfig
{
  // The address on which this listener should accept client connections.
  private final InetAddress listenAddress;

  // The port on which this listener should accept client connections.
  private final int listenPort;

  // The socket factory that should be used for accepting new connections.
  private final ServerSocketFactory serverSocketFactory;

  // The socket factory that should be used for creating client connections.
  private final SocketFactory clientSocketFactory;

  // The socket factory that will be used to add StartTLS encryption to an
  // existing connection.
  private final SSLSocketFactory startTLSSocketFactory;

  // The used to refer to this listener.
  private final String listenerName;



  /**
   * Creates a new in-memory directory server listener configuration with the
   * provided settings.
   *
   * @param  listenerName           The name to assign to this listener.  It
   *                                must not be {@code null} and must not be the
   *                                same as the name for any other listener
   *                                configured in the server.
   * @param  listenAddress          The address on which the listener should
   *                                accept connections from clients.  It may be
   *                                {@code null} to indicate that it should
   *                                accept connections on all addresses on all
   *                                interfaces.
   * @param  listenPort             The port on which the listener should accept
   *                                connections from clients.  It may be 0 to
   *                                indicate that the server should
   *                                automatically choose an available port.
   * @param  serverSocketFactory    The socket factory that should be used to
   *                                create sockets when accepting client
   *                                connections.  It may be {@code null} if the
   *                                JVM-default server socket factory should be
   *                                used.
   * @param  clientSocketFactory    The socket factory that should be used to
   *                                create client connections to the server.  It
   *                                may be {@code null} if the JVM-default
   *                                socket factory should be used.
   * @param  startTLSSocketFactory  The socket factory that should be used to
   *                                add StartTLS encryption to existing
   *                                connections.  It may be {@code null} if
   *                                StartTLS is not to be supported on this
   *                                listener, and should be {@code null} if the
   *                                server socket factory already provides some
   *                                other form of communication security.
   *
   * @throws  LDAPException  If the provided listener name is {@code null} or
   *                         the configured listen port is out of range.
   */
  public InMemoryListenerConfig(final String listenerName,
                                final InetAddress listenAddress,
                                final int listenPort,
                                final ServerSocketFactory serverSocketFactory,
                                final SocketFactory clientSocketFactory,
                                final SSLSocketFactory startTLSSocketFactory)
         throws LDAPException
  {
    if ((listenerName == null) || (listenerName.length() == 0))
    {
      throw new LDAPException(ResultCode.PARAM_ERROR,
           ERR_LISTENER_CFG_NO_NAME.get());
    }

    if ((listenPort < 0) || (listenPort > 65535))
    {
      throw new LDAPException(ResultCode.PARAM_ERROR,
           ERR_LISTENER_CFG_INVALID_PORT.get(listenPort));
    }

    this.listenerName          = listenerName;
    this.listenAddress         = listenAddress;
    this.listenPort            = listenPort;
    this.serverSocketFactory   = serverSocketFactory;
    this.clientSocketFactory   = clientSocketFactory;
    this.startTLSSocketFactory = startTLSSocketFactory;
  }



  /**
   * Creates a new listener configuration that will listen for unencrypted LDAP
   * communication on an automatically-selected port on all available addresses.
   * It will not support StartTLS.
   *
   * @param  listenerName  The name to use for the listener.  It must not be
   *                       {@code null}.
   *
   * @return  The newly-created listener configuration.
   *
   * @throws  LDAPException  If the provided name is {@code null}.
   */
  public static InMemoryListenerConfig createLDAPConfig(
                                            final String listenerName)
         throws LDAPException
  {
    return new InMemoryListenerConfig(listenerName, null, 0, null, null, null);
  }



  /**
   * Creates a new listener configuration that will listen for unencrypted LDAP
   * communication on the specified port on all available addresses.  It will
   * not support StartTLS.
   *
   * @param  listenerName  The name to use for the listener.  It must not be
   *                       {@code null}.
   * @param  listenPort    The port on which the listener should accept
   *                       connections from clients.  It may be 0 to indicate
   *                       that the server should automatically choose an
   *                       available port.
   *
   * @return  The newly-created listener configuration.
   *
   * @throws  LDAPException  If the provided listener name is {@code null} or
   *                         the configured listen port is out of range.
   */
  public static InMemoryListenerConfig createLDAPConfig(
                                            final String listenerName,
                                            final int listenPort)
         throws LDAPException
  {
    return new InMemoryListenerConfig(listenerName, null, listenPort, null,
         null, null);
  }



  /**
   * Creates a new listener configuration that will listen for unencrypted LDAP
   * communication, and may optionally support StartTLS.
   *
   * @param  listenerName           The name to assign to this listener.  It
   *                                must not be {@code null} and must not be the
   *                                same as the name for any other listener
   *                                configured in the server.
   * @param  listenAddress          The address on which the listener should
   *                                accept connections from clients.  It may be
   *                                {@code null} to indicate that it should
   *                                accept connections on all addresses on all
   *                                interfaces.
   * @param  listenPort             The port on which the listener should accept
   *                                connections from clients.  It may be 0 to
   *                                indicate that the server should
   *                                automatically choose an available port.
   * @param  startTLSSocketFactory  The socket factory that should be used to
   *                                add StartTLS encryption to an existing
   *                                connection.  It may be {@code null} if
   *                                StartTLS is not to be supported on this
   *                                listener, and should be {@code null} if the
   *                                server socket factory already provides some
   *                                other form of communication security.
   *
   * @return  The newly-created listener configuration.
   *
   * @throws  LDAPException  If the provided listener name is {@code null} or
   *                         the configured listen port is out of range.
   */
  public static InMemoryListenerConfig createLDAPConfig(
                     final String listenerName, final InetAddress listenAddress,
                     final int listenPort,
                     final SSLSocketFactory startTLSSocketFactory)
         throws LDAPException
  {
    return new InMemoryListenerConfig(listenerName, listenAddress, listenPort,
         null, null, startTLSSocketFactory);
  }



  /**
   * Creates a new listener configuration that will listen for SSL-encrypted
   * LDAP communication on an automatically-selected port on all available
   * addresses.
   *
   * @param  listenerName         The name to use for the listener.  It must not
   *                              be {@code null}.
   * @param  serverSocketFactory  The SSL server socket factory that will be
   *                              used for accepting SSL-based connections from
   *                              clients.  It must not be {@code null}.
   *
   * @return  The newly-created listener configuration.
   *
   * @throws  LDAPException  If the provided name is {@code null}.
   */
  public static InMemoryListenerConfig createLDAPSConfig(
                     final String listenerName,
                     final SSLServerSocketFactory serverSocketFactory)
         throws LDAPException
  {
    return createLDAPSConfig(listenerName, null, 0, serverSocketFactory, null);
  }



  /**
   * Creates a new listener configuration that will listen for SSL-encrypted
   * LDAP communication on the specified port on all available addresses.
   *
   * @param  listenerName         The name to use for the listener.  It must not
   *                              be {@code null}.
   * @param  listenPort           The port on which the listener should accept
   *                              connections from clients.  It may be 0 to
   *                              indicate that the server should
   *                              automatically choose an available port.
   * @param  serverSocketFactory  The SSL server socket factory that will be
   *                              used for accepting SSL-based connections from
   *                              clients.  It must not be {@code null}.
   *
   * @return  The newly-created listener configuration.
   *
   * @throws  LDAPException  If the provided name is {@code null}.
   */
  public static InMemoryListenerConfig createLDAPSConfig(
                     final String listenerName, final int listenPort,
                     final SSLServerSocketFactory serverSocketFactory)
         throws LDAPException
  {
    return createLDAPSConfig(listenerName, null, listenPort,
         serverSocketFactory, null);
  }



  /**
   * Creates a new listener configuration that will listen for SSL-encrypted
   * LDAP communication on an automatically-selected port on all available
   * addresses.
   *
   * @param  listenerName         The name to use for the listener.  It must not
   *                              be {@code null}.
   * @param  listenAddress        The address on which the listener should
   *                              accept connections from clients.  It may be
   *                              {@code null} to indicate that it should
   *                              accept connections on all addresses on all
   *                              interfaces.
   * @param  listenPort           The port on which the listener should accept
   *                              connections from clients.  It may be 0 to
   *                              indicate that the server should
   *                              automatically choose an available port.
   * @param  serverSocketFactory  The SSL server socket factory that will be
   *                              used for accepting SSL-based connections from
   *                              clients.  It must not be {@code null}.
   * @param  clientSocketFactory  The SSL socket factory that will be used to
   *                              create secure connections to the server.  It
   *                              may be {@code null} if a default "trust all"
   *                              socket factory should be used.
   *
   * @return  The newly-created listener configuration.
   *
   * @throws  LDAPException  If the provided name or server socket factory is
   *          {@code null}, or an error occurs while attempting to create a
   *          client socket factory.
   */
  public static InMemoryListenerConfig createLDAPSConfig(
                     final String listenerName, final InetAddress listenAddress,
                     final int listenPort,
                     final SSLServerSocketFactory serverSocketFactory,
                     final SSLSocketFactory clientSocketFactory)
         throws LDAPException
  {
    if (serverSocketFactory == null)
    {
      throw new LDAPException(ResultCode.PARAM_ERROR,
           ERR_LISTENER_CFG_NO_SSL_SERVER_SOCKET_FACTORY.get());
    }

    final SSLSocketFactory clientFactory;
    if (clientSocketFactory == null)
    {
      try
      {
        final SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
        clientFactory = sslUtil.createSSLSocketFactory();
      }
      catch (final Exception e)
      {
        Debug.debugException(e);
        throw new LDAPException(ResultCode.LOCAL_ERROR,
             ERR_LISTENER_CFG_COULD_NOT_CREATE_SSL_SOCKET_FACTORY.get(
                  StaticUtils.getExceptionMessage(e)),
             e);
      }
    }
    else
    {
      clientFactory = clientSocketFactory;
    }

    return new InMemoryListenerConfig(listenerName, listenAddress, listenPort,
         serverSocketFactory, clientFactory, null);
  }



  /**
   * Retrieves the name for this listener configuration.
   *
   * @return  The name for this listener configuration.
   */
  public String getListenerName()
  {
    return listenerName;
  }



  /**
   * Retrieves the address on which the listener should accept connections from
   * clients, if defined.
   *
   * @return  The address on which the listener should accept connections from
   *          clients, or {@code null} if it should accept connections on all
   *          addresses on all interfaces.
   */
  public InetAddress getListenAddress()
  {
    return listenAddress;
  }



  /**
   * Retrieves the port on which the listener should accept connections from
   * clients, if defined.
   *
   * @return  The port on which the listener should accept connections from
   *          clients, or 0 if the listener should automatically select an
   *          available port.
   */
  public int getListenPort()
  {
    return listenPort;
  }



  /**
   * Retrieves the socket factory that should be used to create sockets when
   * accepting client connections, if defined.
   *
   * @return  The socket factory that should be used to create sockets when
   *          accepting client connections, or {@code null} if the JVM-default
   *          server socket factory should be used.
   */
  public ServerSocketFactory getServerSocketFactory()
  {
    return serverSocketFactory;
  }



  /**
   * Retrieves the socket factory that should be used to create client
   * connections to the server, if defined.
   *
   * @return  The socket factory that should be used to create client
   *          connections to the server, or {@code null} if the JVM-default
   *          socket factory should be used.
   */
  public SocketFactory getClientSocketFactory()
  {
    return clientSocketFactory;
  }



  /**
   * Retrieves the socket factory that should be used to add StartTLS encryption
   * to existing connections, if defined.
   *
   * @return  The socket factory that should be used to add StartTLS encryption
   *          to existing connections, or {@code null} if StartTLS should not be
   *          supported.
   */
  public SSLSocketFactory getStartTLSSocketFactory()
  {
    return startTLSSocketFactory;
  }



  /**
   * Retrieves a string representation of this listener configuration.
   *
   * @return  A string representation of this listener configuration.
   */
  @Override()
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder();
    toString(buffer);
    return buffer.toString();
  }



  /**
   * Appends a string representation of this listener configuration to the
   * provided buffer.
   *
   * @param  buffer  The buffer to which the information should be appended.
   */
  public void toString(final StringBuilder buffer)
  {
    buffer.append("InMemoryListenerConfig(name='");
    buffer.append(listenerName);
    buffer.append('\'');

    if (listenAddress != null)
    {
      buffer.append(", listenAddress='");
      buffer.append(listenAddress.getHostAddress());
      buffer.append('\'');
    }

    buffer.append(", listenPort=");
    buffer.append(listenPort);

    if (serverSocketFactory != null)
    {
      buffer.append(", serverSocketFactoryClass='");
      buffer.append(serverSocketFactory.getClass().getName());
      buffer.append('\'');
    }

    if (clientSocketFactory != null)
    {
      buffer.append(", clientSocketFactoryClass='");
      buffer.append(clientSocketFactory.getClass().getName());
      buffer.append('\'');
    }

    if (startTLSSocketFactory != null)
    {
      buffer.append(", startTLSSocketFactoryClass='");
      buffer.append(startTLSSocketFactory.getClass().getName());
      buffer.append('\'');
    }

    buffer.append(')');
  }
}