/*
 * This file is a part of namemc-api, licensed under the MIT License.
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
package dev.jaqobb.namemc_api.repository;

import dev.jaqobb.namemc_api.data.Server;
import dev.jaqobb.namemc_api.util.IOHelper;
import org.json.JSONArray;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ServerRepository {
  public static ServerRepository of() {
    return of(10L, TimeUnit.MINUTES);
  }

  public static ServerRepository of(final long duration, final TimeUnit unit) {
    if(duration < 1) {
      throw new IllegalArgumentException("duration cannot be lower than 1");
    }
    if(unit == null) {
      throw new NullPointerException("unit cannot be null");
    }
    return new ServerRepository(duration, unit);
  }

  private static final String SERVER_VOTES_URL = "https://api.namemc.com/server/%s/votes";

  private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
  private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMCAPI Server Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

  private final long duration;
  private final TimeUnit unit;
  private final Map<String, Server> servers;

  protected ServerRepository(final long duration, final TimeUnit unit) {
    this.duration = duration;
    this.unit = unit;
    this.servers = Collections.synchronizedMap(new HashMap<>(1, 1.0F));
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

  public Collection<Server> getAll() {
    return Collections.unmodifiableCollection(this.servers.values());
  }

  public Collection<Server> getAllValid() {
    return this.servers.values().stream().filter(this::isValid).collect(Collectors.toUnmodifiableList());
  }

  public Collection<Server> getAllInvalid() {
    return this.servers.values().stream().filter(server -> !this.isValid(server)).collect(Collectors.toUnmodifiableList());
  }

  public void add(final Server server) {
    if(server == null) {
      throw new NullPointerException("server cannot be null");
    }
    if(!this.servers.containsKey(server.getAddress().toLowerCase())) {
      this.servers.put(server.getAddress().toLowerCase(), server);
    }
  }

  public void remove(final Server server) {
    if(server == null) {
      throw new NullPointerException("server cannot be null");
    }
    this.servers.remove(server.getAddress().toLowerCase());
  }

  public void cache(final String address, final boolean recache, final BiConsumer<Server, Throwable> callback) {
    if(address == null) {
      throw new NullPointerException("address cannot be null");
    }
    if(callback == null) {
      throw new NullPointerException("callback cannot be null");
    }
    if(this.servers.containsKey(address.toLowerCase())) {
      final Server server = this.servers.get(address.toLowerCase());
      if(this.isValid(server) && !recache) {
        callback.accept(server, null);
        return;
      }
    }
    EXECUTOR.execute(() -> {
      final String url = String.format(SERVER_VOTES_URL, address.toLowerCase());
      try {
        final JSONArray array = new JSONArray(IOHelper.getWebsiteContent(url));
        final Collection<UUID> likes = IntStream.range(0, array.length()).boxed().map(index -> UUID.fromString(array.getString(index))).collect(Collectors.toUnmodifiableList());
        final Server server = Server.of(address.toLowerCase(), likes);
        this.servers.put(address.toLowerCase(), server);
        callback.accept(server, null);
      } catch(final IOException exception) {
        callback.accept(null, exception);
      }
    });
  }

  public boolean isValid(final Server server) {
    if(server == null) {
      throw new NullPointerException("server cannot be null");
    }
    return Instant.now().toEpochMilli() - server.getCacheTime() < this.getDurationMillis();
  }

  public void clear() {
    this.servers.clear();
  }
}