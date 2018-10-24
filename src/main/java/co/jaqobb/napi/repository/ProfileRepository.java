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

import co.jaqobb.napi.data.Profile;
import co.jaqobb.napi.helper.IOHelper;
import co.jaqobb.napi.util.Callback;
import org.json.JSONArray;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class ProfileRepository {
  public static ProfileRepository of() {
    return new ProfileRepository(5L, TimeUnit.MINUTES);
  }

  public static ProfileRepository of(final long duration, final TimeUnit unit) {
    if(duration < 1) {
      throw new IllegalArgumentException("Duration cannot be lower than 1");
    }
    if(unit == null) {
      throw new NullPointerException("Unit cannot be null");
    }
    return new ProfileRepository(duration, unit);
  }

  private static final String PROFILE_FRIENDS_URL = "https://api.namemc.com/profile/%s/friends";

  private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
  private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NAPI Profile Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

  private final long duration;
  private final TimeUnit unit;
  private final Map<UUID, Profile> profiles = Collections.synchronizedMap(new WeakHashMap<>(100, 0.85F));

  private ProfileRepository(final long duration, final TimeUnit unit) {
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

  public Collection<Profile> getProfiles() {
    return Collections.unmodifiableCollection(this.profiles.values());
  }

  public Collection<Profile> getValidProfiles() {
    return this.profiles.values().stream().filter(this::isProfileValid).collect(Collectors.toUnmodifiableList());
  }

  public Collection<Profile> getInvalidProfiles() {
    return this.profiles.values().stream().filter(profile -> !this.isProfileValid(profile)).collect(Collectors.toUnmodifiableList());
  }

  public void cacheProfile(final UUID uniqueId, final boolean recache, final Callback<Profile> callback) {
    if(uniqueId == null) {
      throw new NullPointerException("Unique id cannot be null");
    }
    if(callback == null) {
      throw new NullPointerException("Callback cannot be null");
    }
    if(this.profiles.containsKey(uniqueId)) {
      final Profile profile = this.profiles.get(uniqueId);
      if(this.isProfileValid(profile) && !recache) {
        callback.done(profile, null);
        return;
      }
    }
    EXECUTOR.execute(() -> {
      final String url = String.format(PROFILE_FRIENDS_URL, uniqueId.toString());
      try {
        final JSONArray array = new JSONArray(IOHelper.getWebsiteContent(url));
        final Profile profile = Profile.of(uniqueId, array);
        this.profiles.put(uniqueId, profile);
        callback.done(profile, null);
      } catch(final IOException exception) {
        callback.done(null, exception);
      }
    });
  }

  public boolean isProfileValid(final Profile profile) {
    if(profile == null) {
      throw new NullPointerException("Profile cannot be null");
    }
    return System.currentTimeMillis() - profile.getCacheTime() < this.getDurationMillis();
  }

  public void clearProfiles() {
    this.profiles.clear();
  }
}