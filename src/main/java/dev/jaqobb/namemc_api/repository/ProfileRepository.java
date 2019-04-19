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

import dev.jaqobb.namemc_api.data.Friend;
import dev.jaqobb.namemc_api.data.Profile;
import dev.jaqobb.namemc_api.util.IOHelper;
import org.json.JSONArray;
import org.json.JSONObject;

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

public final class ProfileRepository {
  public static ProfileRepository of() {
    return of(5L, TimeUnit.MINUTES);
  }

  public static ProfileRepository of(final long duration, final TimeUnit unit) {
    if(duration < 1) {
      throw new IllegalArgumentException("duration cannot be lower than 1");
    }
    if(unit == null) {
      throw new NullPointerException("unit cannot be null");
    }
    return new ProfileRepository(duration, unit);
  }

  private static final String PROFILE_FRIENDS_URL = "https://api.namemc.com/profile/%s/friends";

  private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
  private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMCAPI Profile Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

  private final long duration;
  private final TimeUnit unit;
  private final Map<UUID, Profile> profiles;

  protected ProfileRepository(final long duration, final TimeUnit unit) {
    this.duration = duration;
    this.unit = unit;
    this.profiles = Collections.synchronizedMap(new HashMap<>(100, 0.85F));
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

  public Collection<Profile> getAll() {
    return Collections.unmodifiableCollection(this.profiles.values());
  }

  public Collection<Profile> getAllValid() {
    return this.profiles.values().stream().filter(this::isValid).collect(Collectors.toUnmodifiableList());
  }

  public Collection<Profile> getAllInvalid() {
    return this.profiles.values().stream().filter(profile -> !this.isValid(profile)).collect(Collectors.toUnmodifiableList());
  }

  public void add(final Profile profile) {
    if(profile == null) {
      throw new NullPointerException("profile cannot be null");
    }
    if(!this.profiles.containsKey(profile.getUniqueId())) {
      this.profiles.put(profile.getUniqueId(), profile);
    }
  }

  public void remove(final Profile profile) {
    if(profile == null) {
      throw new NullPointerException("profile cannot be null");
    }
    this.profiles.remove(profile.getUniqueId());
  }

  public void cache(final UUID uniqueId, final boolean recache, final BiConsumer<Profile, Throwable> callback) {
    if(uniqueId == null) {
      throw new NullPointerException("uniqueId cannot be null");
    }
    if(callback == null) {
      throw new NullPointerException("callback cannot be null");
    }
    if(this.profiles.containsKey(uniqueId)) {
      final Profile profile = this.profiles.get(uniqueId);
      if(this.isValid(profile) && !recache) {
        callback.accept(profile, null);
        return;
      }
    }
    EXECUTOR.execute(() -> {
      final String url = String.format(PROFILE_FRIENDS_URL, uniqueId.toString());
      try {
        final JSONArray array = new JSONArray(IOHelper.getWebsiteContent(url));
        final Collection<Friend> friends = IntStream.range(0, array.length()).boxed().map(index -> {
          JSONObject object = array.getJSONObject(index);
          return Friend.of(UUID.fromString(object.getString("uniqueId")), object.getString("name"));
        }).collect(Collectors.toUnmodifiableList());
        final Profile profile = Profile.of(uniqueId, friends);
        this.profiles.put(uniqueId, profile);
        callback.accept(profile, null);
      } catch(final IOException exception) {
        callback.accept(null, exception);
      }
    });
  }

  public boolean isValid(final Profile profile) {
    if(profile == null) {
      throw new NullPointerException("profile cannot be null");
    }
    return Instant.now().toEpochMilli() - profile.getCacheTime() < this.getDurationMillis();
  }

  public void clear() {
    this.profiles.clear();
  }
}