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
package co.jaqobb.napi.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * A friend.
 */
public final class Friend {
  /**
   * Creates a friend with unique id and nick.
   *
   * @param uniqueId the friend unique id
   * @param nick the friend nick
   * @return the friend
   */
  public static Friend of(final @NotNull UUID uniqueId, final @NotNull String nick) {
    return new Friend(uniqueId, nick);
  }

  /**
   * The unique id.
   */
  private final UUID uniqueId;
  /**
   * The nick.
   */
  private final String nick;
  /**
   * The cache time.
   */
  private final long cacheTime;

  private Friend(final UUID uniqueId, final String nick) {
    this.uniqueId = uniqueId;
    this.nick = nick;
    this.cacheTime = System.currentTimeMillis();
  }

  /**
   * Gets the friend unique id.
   *
   * @return the friend unique id
   */
  public UUID getUniqueId() {
    return this.uniqueId;
  }

  /**
   * Gets the friend nick.
   *
   * @return the friend nick
   */
  public String getNick() {
    return this.nick;
  }

  /**
   * Gets the friend cache time.
   *
   * @return the friend cache time
   */
  public long getCacheTime() {
    return this.cacheTime;
  }

  /**
   * Gets if the friend is a friend with a profile
   *
   * @param profile the profile to check
   * @return {@code true} if the friend is a friend with the profile or {@code false} otherwise
   */
  public boolean isFriendOf(final @NotNull Profile profile) {
    return this.isFriendOf(profile, true);
  }

  /**
   * Gets if the friend is a friend with a profile
   *
   * @param profile the profile to check
   * @param caseSensitive the state if the case sensitivity in the profile's nick should be checked
   * @return {@code true} if the friend is a friend with the profile or {@code false} otherwise
   */
  public boolean isFriendOf(final @NotNull Profile profile, final boolean caseSensitive) {
    if(profile.getFriend(this.uniqueId) != null) {
      return true;
    }
    return profile.getFriend(this.nick, caseSensitive) != null;
  }

  /**
   * Gets if the friend has liked a server
   *
   * @param server the server to check
   * @return {@code true} if the friend has liked the server or {@code false} otherwise
   */
  public boolean hasLikedServer(final @NotNull Server server) {
    return server.hasLiked(this.uniqueId);
  }

  @Override
  public boolean equals(final @Nullable Object object) {
    if(this == object) {
      return true;
    }
    if(object == null || this.getClass() != object.getClass()) {
      return false;
    }
    final Friend friend = (Friend) object;
    return this.cacheTime == friend.cacheTime && Objects.equals(this.uniqueId, friend.uniqueId) && Objects.equals(this.nick, friend.nick);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uniqueId, this.nick, this.cacheTime);
  }
}