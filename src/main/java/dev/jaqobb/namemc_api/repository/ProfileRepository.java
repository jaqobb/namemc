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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import dev.jaqobb.namemc_api.data.Friend;
import dev.jaqobb.namemc_api.data.Profile;
import dev.jaqobb.namemc_api.util.IOHelper;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileRepository {

	private static final String PROFILE_FRIENDS_URL = "https://api.namemc.com/profile/%s/friends";

	private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
	private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMCAPI Profile Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

	@NotNull
	private Duration cacheDuration;
	@NotNull
	private Map<UUID, Profile> profiles = Collections.synchronizedMap(new HashMap<>(100, 0.85F));

	public ProfileRepository() {
		this(5, ChronoUnit.MINUTES);
	}

	public ProfileRepository(long duration, @NotNull TemporalUnit unit) {
		if(duration < 1) {
			throw new IllegalArgumentException("duration cannot be smaller than 1");
		}
		this.cacheDuration = Duration.of(duration, unit);
	}

	@NotNull
	public Duration getCacheDuration() {
		return this.cacheDuration;
	}

	@NotNull
	public Collection<Profile> getProfiles() {
		return Collections.unmodifiableCollection(this.profiles.values());
	}

	@NotNull
	public Collection<Profile> getValidProfiles() {
		return this.profiles.values().stream()
			.filter(this::isProfileValid)
			.collect(Collectors.toUnmodifiableList());
	}

	@NotNull
	public Collection<Profile> getInvalidProfiles() {
		return this.profiles.values().stream()
			.filter(profile -> !isProfileValid(profile))
			.collect(Collectors.toUnmodifiableList());
	}

	public void addProfile(@NotNull Profile profile) {
		this.profiles.putIfAbsent(profile.getUniqueId(), profile);
	}

	public void removeProfile(@NotNull Profile profile) {
		this.profiles.remove(profile.getUniqueId());
	}

	public void cacheProfile(@NotNull UUID uniqueId, boolean recache, @NotNull BiConsumer<Profile, Throwable> callback) {
		if(this.profiles.containsKey(uniqueId)) {
			Profile profile = this.profiles.get(uniqueId);
			if(isProfileValid(profile) && !recache) {
				callback.accept(profile, null);
				return;
			}
		}
		EXECUTOR.execute(() -> {
			String url = String.format(PROFILE_FRIENDS_URL, uniqueId.toString());
			try {
				JSONArray array = new JSONArray(IOHelper.getWebsiteContent(url));
				Collection<Friend> friends = IntStream.range(0, array.length())
					.boxed()
					.map(index -> {
						JSONObject object = array.getJSONObject(index);
						return new Friend(UUID.fromString(object.getString("uniqueId")), object.getString("name"));
					})
					.collect(Collectors.toUnmodifiableList());
				Profile profile = new Profile(uniqueId, friends);
				this.profiles.put(uniqueId, profile);
				callback.accept(profile, null);
			} catch(IOException | JSONException exception) {
				callback.accept(null, exception);
			}
		});
	}

	public boolean isProfileValid(@NotNull Profile profile) {
		Duration difference = Duration.between(profile.getCacheTime(), Instant.now());
		return difference.compareTo(this.cacheDuration) < 0;
	}

	public void clearProfiles() {
		this.profiles.clear();
	}
}
