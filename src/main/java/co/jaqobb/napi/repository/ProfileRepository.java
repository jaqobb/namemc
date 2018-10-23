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
import org.jetbrains.annotations.NotNull;
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

/**
 * A profile repository.
 */
public final class ProfileRepository {
  /**
   * Creates a profile repository.
   *
   * @return the profile repository
   */
  public static ProfileRepository of() {
    return new ProfileRepository(5L, TimeUnit.MINUTES);
  }

  /**
   * Creates a profile repository with duration and unit.
   *
   * @param duration the duration
   * @param unit the duration unit
   * @return the profile repository
   * @throws IllegalArgumentException if the duration is less than 1
   */
  public static ProfileRepository of(final long duration, final @NotNull TimeUnit unit) {
    if(duration < 1) {
      throw new IllegalArgumentException("duration < 1");
    }
    return new ProfileRepository(duration, unit);
  }

  /**
   * The profile's friends url.
   */
  private static final String PROFILE_FRIENDS_URL = "https://api.namemc.com/profile/%s/friends";
  /**
   * The profile query executor thread counter.
   */
  private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
  /**
   * The profile query executor.
   */
  private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NAPI Profile Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

  /**
   * The duration.
   */
  private final long duration;
  /**
   * The duration unit.
   */
  private final TimeUnit unit;
  /**
   * The cached profiles.
   */
  private final Map<UUID, Profile> profiles = Collections.synchronizedMap(new WeakHashMap<>(100, 0.85F));

  private ProfileRepository(final long duration, final TimeUnit unit) {
    this.duration = duration;
    this.unit = unit;
  }

  /**
   * Gets the duration profiles are cached.
   *
   * @return the duration profiles are cached
   */
  public long getDuration() {
    return this.duration;
  }

  /**
   * Gets the duration profiles are cached in millis.
   *
   * @return the duration profiles are cached in millis
   */
  public long getDurationMillis() {
    return this.unit.toMillis(this.duration);
  }

  /**
   * Gets the duration unit profiles are cached.
   *
   * @return the duration unit profiles are cached
   */
  public TimeUnit getUnit() {
    return this.unit;
  }

  /**
   * Gets cached profiles.
   *
   * @return cached profiles
   */
  public Collection<Profile> getProfiles() {
    return Collections.unmodifiableCollection(this.profiles.values());
  }

  /**
   * Gets valid cached profiles.
   *
   * @return valid cached profiles
   */
  public Collection<Profile> getValidProfiles() {
    return this.profiles.values().stream().filter(this::isProfileValid).collect(Collectors.toUnmodifiableList());
  }

  /**
   * Gets invalid cached profiles.
   *
   * @return invalid cached profiles
   */
  public Collection<Profile> getInvalidProfiles() {
    return this.profiles.values().stream().filter(profile -> !this.isProfileValid(profile)).collect(Collectors.toUnmodifiableList());
  }

  /**
   * Caches a profile and delegates the result to a callback.
   *
   * @param uniqueId the unique id of the profile
   * @param recache the state if the profile should be re-cached anyways
   * @param callback the callback that will contain the caching result
   */
  public void cacheProfile(final @NotNull UUID uniqueId, final boolean recache, final @NotNull Callback<Profile> callback) {
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

  /**
   * Gets if a profile is valid (doesn't need to be re-cached).
   *
   * @param profile the profile to check
   * @return {@code true} if the profile is valid or {@code false} otherwise
   */
  public boolean isProfileValid(final @NotNull Profile profile) {
    return System.currentTimeMillis() - profile.getCacheTime() < this.getDurationMillis();
  }

  /**
   * Clears all cached profiles.
   */
  public void clearProfiles() {
    this.profiles.clear();
  }
}