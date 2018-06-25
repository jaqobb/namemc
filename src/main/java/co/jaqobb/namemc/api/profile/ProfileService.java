/*
 * This file is a part of namemc-api, licensed under the MIT License.
 *
 * Copyright (c) jaqobb (Jakub Zagórski) <jaqobb@jaqobb.co>
 * Copyright (c) contributors
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

package co.jaqobb.namemc.api.profile;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
 * Class used to store cached profile and to cache new ones.
 */
public final class ProfileService
{
	/**
	 * Creates a new ProfileService class instance with the default settings being 5 as a duration and minutes as a duration unit.
	 *
	 * @return A new ProfileService class instance with the default settings.
	 */
	public static ProfileService ofDefault()
	{
		return new ProfileService();
	}

	/**
	 * Creates a new ProfileService class instance with the given duration and duration unit.
	 *
	 * @param duration     A duration.
	 * @param durationUnit A duration unit.
	 *
	 * @return A new ProfileService class instance with the given settings.
	 *
	 * @throws IllegalArgumentException If the given duration is lower than 1.
	 * @throws NullPointerException     If the given duration unit is null.
	 */
	public static ProfileService ofCustom(long duration, TimeUnit durationUnit)
	{
		if (duration < 1)
		{
			throw new IllegalArgumentException("duration < 1");
		}
		return new ProfileService(duration, Objects.requireNonNull(durationUnit, "durationUnit"));
	}

	/**
	 * Url used to cache profiles.
	 */
	private static final String PROFILE_FRIENDS_URL = "https://api.namemc.com/profile/%s/friends";

	/**
	 * Counter used to determine thread number.
	 */
	private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
	/**
	 * Executor used to cache profiles.
	 */
	private static final Executor      EXECUTOR                = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMC API Profile Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

	/**
	 * Duration that indicates how long profiles will be marked as cached.
	 */
	private final long               duration;
	/**
	 * Duration unti.
	 */
	private final TimeUnit           unit;
	/**
	 * Collection of the currently cached profiles.
	 */
	private final Map<UUID, Profile> profiles = Collections.synchronizedMap(new WeakHashMap<>(100));

	/**
	 * Creates a new ProfileService class instance with the default settings being 5 as a duration and minutes as a time unit.
	 */
	private ProfileService()
	{
		this(5, TimeUnit.MINUTES);
	}

	/**
	 * Creates a new ProfileService class instance with the given duration and duration unit.
	 *
	 * @param duration     A duration.
	 * @param durationUnit A duration unit.
	 */
	private ProfileService(long duration, TimeUnit durationUnit)
	{
		this.duration = duration;
		this.unit = durationUnit;
	}

	/**
	 * Returns a duration that indicates how long profiles will be marked as cached.
	 *
	 * @return A duration that indicates how long profiles will be marked as cached.
	 */
	public long getDuration()
	{
		return this.duration;
	}

	/**
	 * Returns a duration unit.
	 *
	 * @return A duration unit.
	 */
	public TimeUnit getUnit()
	{
		return this.unit;
	}

	/**
	 * Returns a duration in milliseconds that indicates how long profiles will be marked as cached.
	 *
	 * @return A Duration in milliseconds that indicates how long profiles will be marked as cached.
	 */
	public long getDurationMillis()
	{
		return this.unit.toMillis(this.duration);
	}

	/**
	 * Returns an immutable collection of the currently cached profiles.
	 *
	 * @return An immutable collection of the currently cached profiles.
	 */
	public Collection<Profile> getProfiles()
	{
		synchronized (this.profiles)
		{
			return Collections.unmodifiableCollection(this.profiles.values());
		}
	}

	/**
	 * Returns an immutable collection of the currently cached valid profiles.
	 *
	 * @return An immutable collection of the currently cached valid profiles.
	 */
	public Collection<Profile> getValidProfiles()
	{
		synchronized (this.profiles)
		{
			return Collections.unmodifiableCollection(this.profiles.values().stream().filter(this::isProfileValid).collect(Collectors.toList()));
		}
	}

	/**
	 * Returns an immutable collection of the currently cached invalid profiles.
	 *
	 * @return An immutable collection of the currently cached invalid profiles.
	 */
	public Collection<Profile> getInvalidProfiles()
	{
		synchronized (this.profiles)
		{
			return Collections.unmodifiableCollection(this.profiles.values().stream().filter(profile -> !this.isProfileValid(profile)).collect(Collectors.toList()));
		}
	}

	/**
	 * Delegates cached profile or caches a new profile with the given unique id and then delegates it to the given callback.
	 *
	 * @param uniqueId A unique id to cache.
	 * @param recache  A state which defines if the re-cache should be forced.
	 * @param callback A callback where cached profile and exception (that is null if everything went good) will be delegated to.
	 *
	 * @throws NullPointerException If the given unique id or callback is null.
	 */
	public void getProfile(UUID uniqueId, boolean recache, BiConsumer<Profile, Exception> callback)
	{
		Objects.requireNonNull(uniqueId, "uniqueId");
		Objects.requireNonNull(callback, "callback");
		synchronized (this.profiles)
		{
			Profile profile = this.profiles.get(uniqueId);
			if (this.isProfileValid(profile) && !recache)
			{
				callback.accept(profile, null);
				return;
			}
		}
		EXECUTOR.execute(() ->
		{
			String url = String.format(PROFILE_FRIENDS_URL, uniqueId.toString());
			try
			{
				String content = IOUtils.getWebsiteContent(url);
				JSONArray array = new JSONArray(content);
				Profile profile = new Profile(uniqueId, array);
				this.profiles.put(uniqueId, profile);
				callback.accept(profile, null);
			}
			catch (IOException exception)
			{
				callback.accept(null, exception);
			}
		});
	}

	/**
	 * Returns true if the given profile is not null and does not need to be re-cached, and false otherwise.
	 *
	 * @param profile A profile to check.
	 *
	 * @return True if the given profile is not null and does not need to be re-cached, and false otherwise.
	 */
	public boolean isProfileValid(Profile profile)
	{
		return profile != null && System.currentTimeMillis() - profile.getCacheTime() < this.getDurationMillis();
	}

	/**
	 * Clears profiles cache.
	 */
	public void clearProfiles()
	{
		synchronized (this.profiles)
		{
			this.profiles.clear();
		}
	}
}