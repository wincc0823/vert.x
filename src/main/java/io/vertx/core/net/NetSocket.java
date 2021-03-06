/*
 * Copyright (c) 2011-2017 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.core.net;

import io.vertx.codegen.annotations.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

/**
 * Represents a socket-like interface to a TCP connection on either the
 * client or the server side.
 * <p>
 * Instances of this class are created on the client side by an {@link NetClient}
 * when a connection to a server is made, or on the server side by a {@link NetServer}
 * when a server accepts a connection.
 * <p>
 * It implements both {@link ReadStream} and {@link WriteStream} so it can be used with
 * {@link io.vertx.core.streams.Pump} to pump data with flow control.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface NetSocket extends ReadStream<Buffer>, WriteStream<Buffer> {

  @Override
  NetSocket exceptionHandler(Handler<Throwable> handler);

  @Override
  NetSocket handler(Handler<Buffer> handler);

  @Override
  NetSocket pause();

  @Override
  NetSocket resume();

  @Override
  NetSocket fetch(long amount);

  @Override
  NetSocket endHandler(Handler<Void> endHandler);

  @Override
  NetSocket write(Buffer data);

  @Override
  NetSocket setWriteQueueMaxSize(int maxSize);

  @Override
  NetSocket drainHandler(Handler<Void> handler);

  /**
   * When a {@code NetSocket} is created it automatically registers an event handler with the event bus, the ID of that
   * handler is given by {@code writeHandlerID}.
   * <p>
   * Given this ID, a different event loop can send a buffer to that event handler using the event bus and
   * that buffer will be received by this instance in its own event loop and written to the underlying connection. This
   * allows you to write data to other connections which are owned by different event loops.
   *
   * @return the write handler ID
   */
  String writeHandlerID();

  /**
   * Write a {@link String} to the connection, encoded in UTF-8.
   *
   * @param str  the string to write
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  NetSocket write(String str);

  /**
   * Write a {@link String} to the connection, encoded using the encoding {@code enc}.
   *
   * @param str  the string to write
   * @param enc  the encoding to use
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  NetSocket write(String str, String enc);

  /**
   * Tell the operating system to stream a file as specified by {@code filename} directly from disk to the outgoing connection,
   * bypassing userspace altogether (where supported by the underlying operating system. This is a very efficient way to stream files.
   *
   * @param filename  file name of the file to send
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default NetSocket sendFile(String filename) {
    return sendFile(filename, 0, Long.MAX_VALUE);
  }

  /**
   * Tell the operating system to stream a file as specified by {@code filename} directly from disk to the outgoing connection,
   * bypassing userspace altogether (where supported by the underlying operating system. This is a very efficient way to stream files.
   *
   * @param filename  file name of the file to send
   * @param offset offset
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default NetSocket sendFile(String filename, long offset) {
    return sendFile(filename, offset, Long.MAX_VALUE);
  }

  /**
   * Tell the operating system to stream a file as specified by {@code filename} directly from disk to the outgoing connection,
   * bypassing userspace altogether (where supported by the underlying operating system. This is a very efficient way to stream files.
   *
   * @param filename  file name of the file to send
   * @param offset offset
   * @param length length
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  NetSocket sendFile(String filename, long offset, long length);

  /**
   * Same as {@link #sendFile(String)} but also takes a handler that will be called when the send has completed or
   * a failure has occurred
   *
   * @param filename  file name of the file to send
   * @param resultHandler  handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default NetSocket sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {
    return sendFile(filename, 0, Long.MAX_VALUE, resultHandler);
  }

  /**
   * Same as {@link #sendFile(String, long)} but also takes a handler that will be called when the send has completed or
   * a failure has occurred
   *
   * @param filename  file name of the file to send
   * @param offset offset
   * @param resultHandler  handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default NetSocket sendFile(String filename, long offset, Handler<AsyncResult<Void>> resultHandler) {
    return sendFile(filename, offset, Long.MAX_VALUE, resultHandler);
  }

  /**
   * Same as {@link #sendFile(String, long, long)} but also takes a handler that will be called when the send has completed or
   * a failure has occurred
   *
   * @param filename  file name of the file to send
   * @param offset offset
   * @param length length
   * @param resultHandler  handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  NetSocket sendFile(String filename, long offset, long length, Handler<AsyncResult<Void>> resultHandler);

  /**
   * @return the remote address for this socket
   */
  @CacheReturn
  SocketAddress remoteAddress();

  /**
   * @return the local address for this socket
   */
  @CacheReturn
  SocketAddress localAddress();

  /**
   * Calls {@link #close()}
   */
  @Override
  void end();

  /**
   * Close the NetSocket
   */
  void close();

  /**
   * Set a handler that will be called when the NetSocket is closed
   *
   * @param handler  the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  NetSocket closeHandler(@Nullable Handler<Void> handler);

  /**
   * Upgrade channel to use SSL/TLS. Be aware that for this to work SSL must be configured.
   *
   * @param handler  the handler will be notified when it's upgraded
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  NetSocket upgradeToSsl(Handler<Void> handler);

  /**
   * Upgrade channel to use SSL/TLS. Be aware that for this to work SSL must be configured.
   *
   * @param serverName the server name
   * @param handler  the handler will be notified when it's upgraded
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  NetSocket upgradeToSsl(String serverName, Handler<Void> handler);

  /**
   * @return true if this {@link io.vertx.core.net.NetSocket} is encrypted via SSL/TLS.
   */
  boolean isSsl();

  /**
   * @return SSLSession associated with the underlying socket. Returns null if connection is
   *         not SSL.
   * @see javax.net.ssl.SSLSession
   */
  @SuppressWarnings("codegen-allow-any-java-type")
  SSLSession sslSession();

  /**
   * Note: Java SE 5+ recommends to use javax.net.ssl.SSLSession#getPeerCertificates() instead of
   * of javax.net.ssl.SSLSession#getPeerCertificateChain() which this method is based on. Use {@link #sslSession()} to
   * access that method.
   *
   * @return an ordered array of the peer certificates. Returns null if connection is
   *         not SSL.
   * @throws javax.net.ssl.SSLPeerUnverifiedException SSL peer's identity has not been verified.
   * @see javax.net.ssl.SSLSession#getPeerCertificateChain()
   * @see #sslSession()
   */
  @GenIgnore
  X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException;

  /**
   * Returns the SNI server name presented during the SSL handshake by the client.
   *
   * @return the indicated server name
   */
  String indicatedServerName();
}

