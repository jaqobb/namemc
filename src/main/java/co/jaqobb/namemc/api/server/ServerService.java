/*
 * MIT License
 *
 * Copyright (c) 2018 Jakub Zagórski
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

package co.jaqobb.namemc.api.server;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import co.jaqobb.namemc.api.util.IOUtils;

import org.json.JSONArray;

/**
 * Class used to store cached servers and to cache new ones.
 */
public class ServerService {

  /**
   * Creates new server service instance with the default values being 10 as a duration and minutes as a time unit.
   */
  public static ServerService ofDefault() {
    return new ServerService();
  }

  /**
   * Creates new server service instance with the given duration and duration unit.
   *
   * @param duration A duration.
   * @param unit A time unit.
   * @throws IllegalArgumentException If the given duration is 0 or negative.
   * @throws NullPointerException If the given unit is null.
   */
  public static ServerService ofCustom(long duration, TimeUnit unit) {
    if (duration < 1) {
      throw new IllegalArgumentException("duration < 1");
    }
    return new ServerService(duration, Objects.requireNonNull(unit, "unit"));
  }

  /**
   * Url used to cache servers.
   */
  private static final String SERVER_VOTES_URL = "https://api.namemc.com/server/%s/votes";

  /**
   * Counter used to determine thread number.
   */
  private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
  /**
   * Executor used to cache servers.
   */
  private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMC API Server Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

  /**
   * Duration that indicates how long servers will be marked as cached.
   */
  private final long duration;
  /**
   * Time unit used to describe a unit of the duration.
   */
  private final TimeUnit unit;
  /**
   * Collection of the currently cached servers.
   */
  private final Map<String, Server> servers = Collections.synchronizedMap(new WeakHashMap<>(100));

  /**
   * Creates new server service instance with the default values being 10 as a duration and minutes as a time unit.
   */
  private ServerService() {
    this(10, TimeUnit.MINUTES);
  }

  /**
   * Creates new server service instance with the given duration and time unit.
   *
   * @param duration A duration.
   * @param unit A time unit.
   */
  private ServerService(long duration, TimeUnit unit) {
    this.duration = duration;
    this.unit = unit;
  }

  /**
   * Returns duration that indicates how long servers will be marked as cached.
   *
   * @return Duration that indicates how long servers will be marked as cached.
   */
  public long getDuration() {
    return this.duration;
  }

  /**
   * Returns unit of the tracked duration.
   *
   * @return Unit of the tracked duration.
   */
  public TimeUnit getUnit() {
    return this.unit;
  }

  /**
   * Returns duration in milliseconds that indicates how long servers will be marked as cached.
   *
   * @return Duration in milliseconds that indicates how long servers will be marked as cached.
   */
  public long getDurationMillis() {
    return this.unit.toMillis(this.duration);
  }

  /**
   * Returns an immutable collection of the currently cached servers.
   *
   * @return An immutable collection of the currently cached servers.
   */
  public Collection<Server> getServers() {
    synchronized (this.servers) {
      return Collections.unmodifiableCollection(this.servers.values());
    }
  }

  /**
   * Returns an immutable collection of the currently cached valid servers.
   *
   * @return An immutable collection of the currently cached valid servers.
   */
  public Collection<Server> getValidServers() {
    synchronized (this.servers) {
      return Collections.unmodifiableCollection(this.servers.values().stream().filter(this::isServerValid).collect(Collectors.toList()));
    }
  }

  /**
   * Returns an immutable collection of the currently cached invalid servers.
   *
   * @return An immutable collection of the currently cached invalid servers.
   */
  public Collection<Server> getInvalidServers() {
    synchronized (this.servers) {
      return Collections.unmodifiableCollection(this.servers.values().stream().filter(server -> !this.isServerValid(server)).collect(Collectors.toList()));
    }
  }

  /**
   * Delegates cached server or caches new server with the given ip and then delegates it to the given callback.
   *
   * @param ip An ip to cache (case insensitive).
   * @param recache A state which defines if the recache should be forced.
   * @param callback A callback where cached server and exception (that is null if everything went good) will be
   * delegated to.
   * @throws NullPointerException If the given ip or the given callback is null.
   */
  public void getServer(String ip, boolean recache, BiConsumer<Server, Exception> callback) {
    Objects.requireNonNull(ip, "ip");
    Objects.requireNonNull(callback, "callback");
    synchronized (this.servers) {
      Server server = this.servers.get(ip.toLowerCase());
      if (this.isServerValid(server) && !recache) {
        callback.accept(server, null);
      }
    }
    EXECUTOR.execute(() -> {
      String url = String.format(SERVER_VOTES_URL, ip);
      try {
        String content = IOUtils.getWebsiteContent(url);
        JSONArray array = new JSONArray(content);
        Server server = new Server(ip, array);
        this.servers.put(ip.toLowerCase(), server);
        callback.accept(server, null);
      } catch (IOException exception) {
        callback.accept(null, exception);
      }
    });
  }

  /**
   * Returns true if the given server is not null and does not need to be recached and false otherwise.
   *
   * @param server A server to check.
   * @return True if the given server is not null and does not need to be recached and false otherwise.
   */
  public boolean isServerValid(Server server) {
    return server != null && System.currentTimeMillis() - server.getCacheTime() < this.getDurationMillis();
  }

  /**
   * Clears servers cache.
   */
  public void clearServers() {
    synchronized (this.servers) {
      this.servers.clear();
    }
  }

}