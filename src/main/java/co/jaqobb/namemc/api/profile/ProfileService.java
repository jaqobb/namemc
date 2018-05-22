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

import co.jaqobb.namemc.api.json.JSONArray;
import co.jaqobb.namemc.api.util.IOUtils;

/**
 * Class used to store cached {@code Profile},
 * and to cache new ones.
 */
public class ProfileService
{
	/**
	 * Creates new {@code ProfileService} instance
	 * with the default values being 5 as a duration
	 * and minutes as a time unit.
	 */
	public static ProfileService ofDefault()
	{
		return new ProfileService();
	}

	/**
	 * Creates new {@code ProfileService} instance
	 * with the given duration and time unit.
	 *
	 * @param duration a duration.
	 * @param unit     a time unit.
	 *
	 * @throws IllegalArgumentException if the {@code duration} is less than 1.
	 * @throws NullPointerException     if the {@code unit} is {@code null}.
	 */
	public static ProfileService ofCustom(long duration, TimeUnit unit)
	{
		if (duration < 1)
		{
			throw new IllegalArgumentException("duration < 1");
		}
		return new ProfileService(duration, Objects.requireNonNull(unit));
	}

	/**
	 * Url used to cache {@code Profile}.
	 */
	private static final String PROFILE_FRIENDS_URL = "https://api.namemc.com/profile/%s/friends";

	/**
	 * Counter used to determine thread number.
	 */
	private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
	/**
	 * Executor used to cache {@code Profile}.
	 */
	private static final Executor      EXECUTOR                = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMC API Profile Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

	/**
	 * Duration that indicates how long {@code Profile} will be marked as cached.
	 */
	private final long               duration;
	/**
	 * Time unit used to describe a unit of {@code duration}.
	 */
	private final TimeUnit           unit;
	/**
	 * Collection of currently cached {@code Profile}s.
	 */
	private final Map<UUID, Profile> profiles = Collections.synchronizedMap(new WeakHashMap<>(100));

	/**
	 * Creates new {@code ProfileService} instance
	 * with the default values being 5 as a duration
	 * and minutes as a time unit.
	 */
	private ProfileService()
	{
		this(5, TimeUnit.MINUTES);
	}

	/**
	 * Creates new {@code ProfileService} instance
	 * with the given time and time unit.
	 *
	 * @param duration a duration.
	 * @param unit     a time unit.
	 */
	private ProfileService(long duration, TimeUnit unit)
	{
		this.duration = duration;
		this.unit = unit;
	}

	/**
	 * Returns duration that indicates how long
	 * {@code Profile} will be marked as cached.
	 *
	 * @return duration that indicates how long
	 * {@code Profile} will be marked as cached.
	 */
	public long getDuration()
	{
		return this.duration;
	}

	/**
	 * Returns unit of the tracked {@code duration}.
	 *
	 * @return unit of the tracked {@code duration}.
	 */
	public TimeUnit getUnit()
	{
		return this.unit;
	}

	/**
	 * Returns duration in milliseconds that indicates
	 * how long {@code Profile} will be marked as cached.
	 *
	 * @return duration in milliseconds that indicates
	 * how long {@code Profile} will be marked as cached.
	 */
	public long getDurationMillis()
	{
		return this.unit.toMillis(this.duration);
	}

	/**
	 * Returns an immutable collection
	 * of currently cached {@code Profile}s.
	 *
	 * @return an immutable collection of currently cached {@code Profile}s.
	 */
	public Collection<Profile> getProfiles()
	{
		synchronized (this.profiles)
		{
			return Collections.unmodifiableCollection(this.profiles.values());
		}
	}

	/**
	 * Returns an immutable collection of
	 * currently cached valid {@code Profile}s.
	 *
	 * @return an immutable collection of currently cached valid {@code Profile}s.
	 */
	public Collection<Profile> getValidProfiles()
	{
		synchronized (this.profiles)
		{
			return Collections.unmodifiableCollection(this.profiles.values().stream().filter(this::isProfileValid).collect(Collectors.toList()));
		}
	}

	/**
	 * Returns an immutable collection of
	 * currently cached invalid {@code Profile}s.
	 *
	 * @return an immutable collection of currently cached invalid {@code Profile}s.
	 */
	public Collection<Profile> getInvalidProfiles()
	{
		synchronized (this.profiles)
		{
			return Collections.unmodifiableCollection(this.profiles.values().stream().filter(profile -> ! this.isProfileValid(profile)).collect(Collectors.toList()));
		}
	}

	/**
	 * Delegates cached {@code Profile} or
	 * caches new {@code Profile} with the
	 * given unique id and then delegates
	 * it to the {@code callback}.
	 *
	 * @param uniqueId a unique id to cache.
	 * @param recache  a state which defines
	 *                 if the recache should
	 *                 be forced.
	 * @param callback a callback where cached
	 *                 {@code Profile} and exception
	 *                 (null if everything went good)
	 *                 will be delegated to.
	 *
	 * @throws NullPointerException if the {@code uniqueId} or the {@code callback} is null.
	 */
	public void getProfile(UUID uniqueId, boolean recache, BiConsumer<Profile, Exception> callback)
	{
		Objects.requireNonNull(uniqueId, "uniqueId");
		Objects.requireNonNull(callback, "callback");
		synchronized (this.profiles)
		{
			Profile profile = this.profiles.get(uniqueId);
			if (this.isProfileValid(profile) && ! recache)
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
	 * Returns {@code true} if the given {@code profile} is
	 * not {@code null} and does not need to be recached,
	 * {@code false} otherwise.
	 *
	 * @param profile a {@code Profile} to check.
	 *
	 * @return {@code true} if the given {@code profile} is
	 * not {@code null} and does not need to be recached,
	 * {@code false} otherwise.
	 */
	public boolean isProfileValid(Profile profile)
	{
		return profile != null && System.currentTimeMillis() - profile.getCacheTime() < this.getDurationMillis();
	}

	/**
	 * Clears {@code Profile}s cache.
	 */
	public void clearProfiles()
	{
		synchronized (this.profiles)
		{
			this.profiles.clear();
		}
	}
}