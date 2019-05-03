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

package dev.jaqobb.namemc_api.data;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Profile {

	@NotNull
	private UUID uniqueId;
	@NotNull
	private Collection<Friend> friends;
	@NotNull
	private Instant cacheTime;

	public Profile(@NotNull UUID uniqueId, @NotNull Collection<Friend> friends) {
		this.uniqueId = uniqueId;
		this.friends = friends;
		cacheTime = Instant.now();
	}

	@NotNull
	public UUID getUniqueId() {
		return uniqueId;
	}

	@NotNull
	public Collection<Friend> getFriends() {
		return Collections.unmodifiableCollection(friends);
	}

	@Nullable
	public Friend getFriend(@NotNull UUID uniqueId) {
		return friends.stream()
			.filter(friend -> friend.getUniqueId().equals(uniqueId))
			.findFirst()
			.orElse(null);
	}

	@Nullable
	public Friend getFriend(@NotNull String name) {
		return getFriend(name, true);
	}

	@Nullable
	public Friend getFriend(@NotNull String name, boolean caseSensitive) {
		return friends.stream()
			.filter(friend -> caseSensitive ? friend.getName().equals(name) : friend.getName().equalsIgnoreCase(name))
			.findFirst()
			.orElse(null);
	}

	@NotNull
	public Instant getCacheTime() {
		return cacheTime;
	}

	public boolean hasLikedServer(@NotNull Server server) {
		return server.hasLiked(uniqueId);
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if(this == object) {
			return true;
		}
		if(object == null || getClass() != object.getClass()) {
			return false;
		}
		Profile that = (Profile) object;
		return Objects.equals(uniqueId, that.uniqueId) &&
			Objects.equals(friends, that.friends) &&
			Objects.equals(cacheTime, that.cacheTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uniqueId, friends, cacheTime);
	}

	@Override
	public String toString() {
		return "Profile{" +
			"uniqueId=" + uniqueId +
			", friends=" + friends +
			", cacheTime=" + cacheTime +
			"}";
	}
}
