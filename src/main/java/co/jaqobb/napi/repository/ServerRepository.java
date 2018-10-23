/*
 * This file is a part of napi, licensed under the MIT License.
 *
 * Copyright (c) Jakub Zagórski (jaqobb)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package co.jaqobb.napi.repository;

import co.jaqobb.napi.data.Server;
import co.jaqobb.napi.helper.IOHelper;
import co.jaqobb.napi.util.Callback;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A server repository.
 */
public final class ServerRepository {
  /**
   * Creates a server repository.
   *
   * @return the server repository
   */
  public static ServerRepository of() {
    return new ServerRepository(10L, TimeUnit.MINUTES);
  }

  /**
   * Creates a server repository with duration and unit.
   *
   * @param duration the duration
   * @param unit the duration unit
   * @return the server repository
   * @throws IllegalArgumentException if the duration is less than 1
   */
  public static ServerRepository of(final long duration, final @NotNull TimeUnit unit) {
    if(duration < 1) {
      throw new IllegalArgumentException("duration < 1");
    }
    return new ServerRepository(duration, unit);
  }

  /**
   * The server's friends url.
   */
  private static final String SERVER_VOTES_URL = "https://api.namemc.com/server/%s/votes";
  /**
   * The server query executor thread counter.
   */
  private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
  /**
   * The server query executor.
   */
  private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NAPI Server Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

  /**
   * The duration.
   */
  private final long duration;
  /**
   * The duration unit.
   */
  private final TimeUnit unit;
  /**
   * The cached servers.
   */
  private final Map<String, Server> servers = Collections.synchronizedMap(new WeakHashMap<>(1, 1.0F));

  private ServerRepository(final long duration, final TimeUnit unit) {
    this.duration = duration;
    this.unit = unit;
  }

  /**
   * Gets the duration servers are cached.
   *
   * @return the duration servers are cached
   */
  public long getDuration() {
    return this.duration;
  }

  /**
   * Gets the duration servers are cached in millis.
   *
   * @return the duration servers are cached in millis
   */
  public long getDurationMillis() {
    return this.unit.toMillis(this.duration);
  }

  /**
   * Gets the duration unit servers are cached.
   *
   * @return the duration unit servers are cached
   */
  public TimeUnit getUnit() {
    return this.unit;
  }

  /**
   * Gets cached servers.
   *
   * @return cached servers
   */
  public Collection<Server> getServers() {
    return Collections.unmodifiableCollection(this.servers.values());
  }

  /**
   * Gets valid cached servers.
   *
   * @return valid cached servers
   */
  public Collection<Server> getValidServers() {
    return this.servers.values().stream().filter(this::isServerValid).collect(Collectors.toUnmodifiableList());
  }

  /**
   * Gets invalid cached servers.
   *
   * @return invalid cached servers
   */
  public Collection<Server> getInvalidServers() {
    return this.servers.values().stream().filter(server -> !this.isServerValid(server)).collect(Collectors.toUnmodifiableList());
  }

  /**
   * Caches a server and delegates the result to a callback.
   *
   * @param ip the ip of the server
   * @param recache the state if the server should be re-cached anyways
   * @param callback the callback that will contain the caching result
   */
  public void cacheServer(final @NotNull String ip, final boolean recache, final @NotNull Callback<Server> callback) {
    final String finalIp = ip.toLowerCase();
    if(this.servers.containsKey(finalIp)) {
      final Server server = this.servers.get(finalIp);
      if(this.isServerValid(server) && !recache) {
        callback.done(server, null);
        return;
      }
    }
    EXECUTOR.execute(() -> {
      final String url = String.format(SERVER_VOTES_URL, finalIp);
      try {
        final JSONArray array = new JSONArray(IOHelper.getWebsiteContent(url));
        final Server server = Server.of(finalIp, array);
        this.servers.put(finalIp, server);
        callback.done(server, null);
      } catch(final IOException exception) {
        callback.done(null, exception);
      }
    });
  }

  /**
   * Gets if a server is valid (doesn't need to be re-cached).
   *
   * @param server the server to check
   * @return {@code true} if the server is valid or {@code false} otherwise
   */
  public boolean isServerValid(final @NotNull Server server) {
    return System.currentTimeMillis() - server.getCacheTime() < this.getDurationMillis();
  }

  /**
   * Clears all cached servers.
   */
  public void clearServers() {
    this.servers.clear();
  }
}