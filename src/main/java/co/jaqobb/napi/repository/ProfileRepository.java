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

  public static ProfileRepository of(long duration, TimeUnit unit) {
    if(duration < 1) {
      throw new IllegalArgumentException("duration cannot be lower than 1");
    }
    if(unit == null) {
      throw new NullPointerException("unit cannot be null");
    }
    return new ProfileRepository(duration, unit);
  }

  private static String PROFILE_FRIENDS_URL = "https://api.namemc.com/profile/%s/friends";

  private static AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
  private static Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NAPI Profile Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

  private long duration;
  private TimeUnit unit;
  private Map<UUID, Profile> profiles;

  private ProfileRepository(long duration, TimeUnit unit) {
    this.duration = duration;
    this.unit = unit;
    this.profiles = Collections.synchronizedMap(new WeakHashMap<>(100, 0.85F));
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
    return Collections.unmodifiableCollection(this.profiles.values().stream().filter(this::isProfileValid).collect(Collectors.toList()));
  }

  public Collection<Profile> getInvalidProfiles() {
    return Collections.unmodifiableCollection(this.profiles.values().stream().filter(profile -> !this.isProfileValid(profile)).collect(Collectors.toList()));
  }

  public void cacheProfile(UUID uuid, boolean recache, Callback<Profile> callback) {
    if(uuid == null) {
      throw new NullPointerException("uuid cannot be null");
    }
    if(callback == null) {
      throw new NullPointerException("callback cannot be null");
    }
    if(this.profiles.containsKey(uuid)) {
      Profile profile = this.profiles.get(uuid);
      if(this.isProfileValid(profile) && !recache) {
        callback.done(profile, null);
        return;
      }
    }
    EXECUTOR.execute(() -> {
      String url = String.format(PROFILE_FRIENDS_URL, uuid.toString());
      try {
        Profile profile = Profile.of(uuid, new JSONArray(IOHelper.getWebsiteContent(url)));
        this.profiles.put(uuid, profile);
        callback.done(profile, null);
      } catch(IOException exception) {
        callback.done(null, exception);
      }
    });
  }

  public boolean isProfileValid(Profile profile) {
    if(profile == null) {
      throw new NullPointerException("profile cannot be null");
    }
    return System.currentTimeMillis() - profile.getCacheTime() < this.getDurationMillis();
  }

  public void clearProfiles() {
    this.profiles.clear();
  }
}