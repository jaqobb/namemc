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
import java.util.Optional;
import java.util.UUID;

public final class Profile {
  private final UUID uniqueId;
  private final Collection<Friend> friends;
  private final Instant cacheTime;

  public Profile(final UUID uniqueId, final Collection<Friend> friends) {
    this.uniqueId = Objects.requireNonNull(uniqueId, "uniqueId");
    Objects.requireNonNull(friends, "friends");
    for(final Friend friend : friends) {
      Objects.requireNonNull(friend, "friend");
    }
    this.friends = friends;
    this.cacheTime = Instant.now();
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public Collection<Friend> getFriends() {
    return Collections.unmodifiableCollection(this.friends);
  }

  public Optional<Friend> getFriend(final UUID uniqueId) {
    Objects.requireNonNull(uniqueId, "uniqueId");
    return this.friends.stream().filter(friend -> friend.getUniqueId().equals(uniqueId)).findFirst();
  }

  public Optional<Friend> getFriend(final String name) {
    return this.getFriend(name, true);
  }

  public Optional<Friend> getFriend(final String name, final boolean caseSensitive) {
    Objects.requireNonNull(name, "name");
    return this.friends.stream().filter(friend -> caseSensitive ? friend.getName().equals(name) : friend.getName().equalsIgnoreCase(name)).findFirst();
  }

  public Instant getCacheTime() {
    return this.cacheTime;
  }

  public boolean hasLikedServer(final Server server) {
    Objects.requireNonNull(server, "server");
    return server.hasLiked(this.uniqueId);
  }

  @Override
  public boolean equals(final Object object) {
    if(this == object) {
      return true;
    }
    if(object == null || this.getClass() != object.getClass()) {
      return false;
    }
    final Profile that = (Profile) object;
    return Objects.equals(this.uniqueId, that.uniqueId) &&
      Objects.equals(this.friends, that.friends) &&
      Objects.equals(this.cacheTime, that.cacheTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uniqueId, this.friends, this.cacheTime);
  }

  @Override
  public String toString() {
    return "Profile{" +
      "uniqueId=" + this.uniqueId +
      ", friends=" + this.friends +
      ", cacheTime=" + this.cacheTime +
      "}";
  }
}