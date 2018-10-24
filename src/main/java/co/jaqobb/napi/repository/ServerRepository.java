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

public final class ServerRepository {
  public static ServerRepository of() {
    return new ServerRepository(10L, TimeUnit.MINUTES);
  }

  public static ServerRepository of(final long duration, final TimeUnit unit) {
    if(duration < 1) {
      throw new IllegalArgumentException("Duration cannot be lower than 1");
    }
    if(unit == null) {
      throw new NullPointerException("Unit cannot be null");
    }
    return new ServerRepository(duration, unit);
  }

  private static final String SERVER_VOTES_URL = "https://api.namemc.com/server/%s/votes";

  private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
  private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NAPI Server Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

  private final long duration;
  private final TimeUnit unit;
  private final Map<String, Server> servers = Collections.synchronizedMap(new WeakHashMap<>(1, 1.0F));

  private ServerRepository(final long duration, final TimeUnit unit) {
    this.duration = duration;
    this.unit = unit;
  }

  public long getDuration() {
    return this.duration;
  }

  public long getDurationMillis() {
    return this.unit.toMillis(this.duration);
  }

  public TimeUnit getUnit() {
    return this.unit;
  }

  public Collection<Server> getServers() {
    return Collections.unmodifiableCollection(this.servers.values());
  }

  public Collection<Server> getValidServers() {
    return this.servers.values().stream().filter(this::isServerValid).collect(Collectors.toUnmodifiableList());
  }

  public Collection<Server> getInvalidServers() {
    return this.servers.values().stream().filter(server -> !this.isServerValid(server)).collect(Collectors.toUnmodifiableList());
  }

  public void cacheServer(final String ip, final boolean recache, final Callback<Server> callback) {
    if(ip == null) {
      throw new NullPointerException("Ip cannot be null");
    }
    if(callback == null) {
      throw new NullPointerException("Callback cannot be null");
    }
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

  public boolean isServerValid(final Server server) {
    if(server == null) {
      throw new NullPointerException("Server cannot be null");
    }
    return System.currentTimeMillis() - server.getCacheTime() < this.getDurationMillis();
  }

  public void clearServers() {
    this.servers.clear();
  }
}