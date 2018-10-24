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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public final class Profile {
  public static Profile of(final UUID uniqueId, final JSONArray jsonArray) {
    if(uniqueId == null) {
      throw new NullPointerException("Unique id cannot be null");
    }
    if(jsonArray == null) {
      throw new NullPointerException("Json array cannot be null");
    }
    final Collection<Friend> friends = new ArrayList<>(jsonArray.length());
    for(int index = 0; index < jsonArray.length(); index++) {
      final JSONObject jsonObject = jsonArray.getJSONObject(index);
      friends.add(Friend.of(UUID.fromString(jsonObject.getString("uuid")), jsonObject.getString("name")));
    }
    return new Profile(uniqueId, friends);
  }

  private final UUID uniqueId;
  private final Collection<Friend> friends;
  private final long cacheTime;

  private Profile(final UUID uniqueId, final Collection<Friend> friends) {
    this.uniqueId = uniqueId;
    this.friends = friends;
    this.cacheTime = System.currentTimeMillis();
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public Collection<Friend> getFriends() {
    return Collections.unmodifiableCollection(this.friends);
  }

  public Friend getFriend(final UUID uniqueId) {
    if(uniqueId == null) {
      throw new NullPointerException("Unique id cannot be null");
    }
    return this.friends.stream().filter(friend -> friend.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
  }

  public Friend getFriend(final String nick) {
    return this.getFriend(nick, true);
  }

  public Friend getFriend(final String nick, final boolean caseSensitive) {
    if(nick == null) {
      throw new NullPointerException("Nick cannot be null");
    }
    return this.friends.stream().filter(friend -> caseSensitive ? friend.getNick().equals(nick) : friend.getNick().equalsIgnoreCase(nick)).findFirst().orElse(null);
  }

  public long getCacheTime() {
    return this.cacheTime;
  }

  public boolean hasLikedServer(final Server server) {
    if(server == null) {
      throw new NullPointerException("Server cannot be null");
    }
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
    final Profile profile = (Profile) object;
    return this.cacheTime == profile.cacheTime && Objects.equals(this.uniqueId, profile.uniqueId) && Objects.equals(this.friends, profile.friends);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uniqueId, this.friends, this.cacheTime);
  }
}