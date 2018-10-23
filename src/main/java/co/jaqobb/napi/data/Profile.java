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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * A profile.
 */
public final class Profile {
  /**
   * Creates a friend with unique id and array.
   *
   * @param uniqueId the profile unique id
   * @param array the profile array (friends)
   * @return the profile
   */
  public static Profile of(final @NotNull UUID uniqueId, final @NotNull JSONArray array) {
    return new Profile(uniqueId, array);
  }

  /**
   * The unique id.
   */
  private final UUID uniqueId;
  /**
   * The friends.
   */
  private final Collection<Friend> friends;
  /**
   * The cache time.
   */
  private final long cacheTime;

  private Profile(final UUID uniqueId, final JSONArray array) {
    this.uniqueId = uniqueId;
    this.friends = new ArrayList<>(array.length());
    for(int index = 0; index < array.length(); index++) {
      final JSONObject object = array.getJSONObject(index);
      this.friends.add(Friend.of(UUID.fromString(object.getString("uuid")), object.getString("name")));
    }
    this.cacheTime = System.currentTimeMillis();
  }

  /**
   * Gets the profile unique id.
   *
   * @return the profile unique id
   */
  public UUID getUniqueId() {
    return this.uniqueId;
  }

  /**
   * Gets the profile friends.
   *
   * @return the profile friends
   */
  public Collection<Friend> getFriends() {
    return Collections.unmodifiableCollection(this.friends);
  }

  /**
   * Gets a friend by a unique id.
   *
   * @param uniqueId the unique id of the friend to find
   * @return the friend or {@code null} otherwise
   */
  public Friend getFriend(final @NotNull UUID uniqueId) {
    return this.friends.stream().filter(friend -> friend.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
  }

  /**
   * Gets a friend by a nick
   *
   * @param nick the nick of the friend to find
   * @return the friend or {@code null} otherwise
   */
  public Friend getFriend(final @NotNull String nick) {
    return this.getFriend(nick, true);
  }

  /**
   * Gets a friend by a nick
   *
   * @param nick the nick of the friend to find
   * @param caseSensitive the state if the case sensitivity in the friend's nick should be checked
   * @return the friend or {@code null} otherwise
   */
  public Friend getFriend(final @NotNull String nick, final boolean caseSensitive) {
    return this.friends.stream().filter(friend -> caseSensitive ? friend.getNick().equals(nick) : friend.getNick().equalsIgnoreCase(nick)).findFirst().orElse(null);
  }

  /**
   * Gets the profile cache time.
   *
   * @return the profile cache time
   */
  public long getCacheTime() {
    return this.cacheTime;
  }

  /**
   * Gets if the profile has liked a server
   *
   * @param server the server to check
   * @return {@code true} if the profile has liked the server or {@code false} otherwise
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
    final Profile profile = (Profile) object;
    return this.cacheTime == profile.cacheTime && Objects.equals(this.uniqueId, profile.uniqueId) && Objects.equals(this.friends, profile.friends);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uniqueId, this.friends, this.cacheTime);
  }
}