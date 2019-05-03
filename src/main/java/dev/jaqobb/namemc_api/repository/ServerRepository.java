/*
 * MIT License
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
import org.json.JSONException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ServerRepository {
  private static final String SERVER_VOTES_URL = "https://api.namemc.com/server/%s/votes";

  private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
  private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMCAPI Server Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

  private final Duration cacheDuration;
  private final Map<String, Server> servers;

  public ServerRepository() {
    this(10, ChronoUnit.MINUTES);
  }

  public ServerRepository(final long duration, final TemporalUnit unit) {
    if(duration < 1) {
      throw new IllegalArgumentException("duration cannot be smaller than 1");
    }
    Objects.requireNonNull(unit, "unit");
    this.cacheDuration = Duration.of(duration, unit);
    this.servers = Collections.synchronizedMap(new HashMap<>(1, 1.0F));
  }

  public Duration getCacheDuration() {
    return this.cacheDuration;
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

  public void addServer(final Server server) {
    Objects.requireNonNull(server, "server");
    this.servers.putIfAbsent(server.getAddress().toLowerCase(), server);
  }

  public void removeServer(final Server server) {
    Objects.requireNonNull(server, "server");
    this.servers.remove(server.getAddress().toLowerCase());
  }

  public void cacheServer(final String address, final boolean recache, final BiConsumer<Server, Throwable> callback) {
    Objects.requireNonNull(address, "address");
    Objects.requireNonNull(callback, "callback");
    if(this.servers.containsKey(address.toLowerCase())) {
      final Server server = this.servers.get(address.toLowerCase());
      if(this.isServerValid(server) && !recache) {
        callback.accept(server, null);
        return;
      }
    }
    EXECUTOR.execute(() -> {
      final String url = String.format(SERVER_VOTES_URL, address.toLowerCase());
      try {
        final JSONArray array = new JSONArray(IOHelper.getWebsiteContent(url));
        final Collection<UUID> likes = IntStream.range(0, array.length())
          .boxed()
          .map(index -> UUID.fromString(array.getString(index)))
          .collect(Collectors.toUnmodifiableList());
        final Server server = new Server(address.toLowerCase(), likes);
        this.servers.put(address.toLowerCase(), server);
        callback.accept(server, null);
      } catch(final IOException | JSONException exception) {
        callback.accept(null, exception);
      }
    });
  }

  public boolean isServerValid(final Server server) {
    Objects.requireNonNull(server, "server");
    return Duration.between(server.getCacheTime(), Instant.now()).compareTo(this.cacheDuration) < 0;
  }

  public void clearServers() {
    this.servers.clear();
  }
}