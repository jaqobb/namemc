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
  public static Profile of(UUID uuid, JSONArray array) {
    if(uuid == null) {
      throw new NullPointerException("uuid cannot be null");
    }
    if(array == null) {
      throw new NullPointerException("array cannot be null");
    }
    Collection<Friend> friends = new ArrayList<>(array.length());
    for(int index = 0; index < array.length(); index++) {
      JSONObject object = array.getJSONObject(index);
      friends.add(Friend.of(UUID.fromString(object.getString("uuid")), object.getString("name")));
    }
    return new Profile(uuid, friends);
  }

  private UUID uuid;
  private Collection<Friend> friends;
  private long cacheTime;

  private Profile(UUID uuid, Collection<Friend> friends) {
    this.uuid = uuid;
    this.friends = friends;
    this.cacheTime = System.currentTimeMillis();
  }

  public UUID getUUID() {
    return this.uuid;
  }

  public Collection<Friend> getFriends() {
    return Collections.unmodifiableCollection(this.friends);
  }

  public Friend getFriend(UUID uuid) {
    if(uuid == null) {
      throw new NullPointerException("uuid cannot be null");
    }
    return this.friends.stream().filter(friend -> friend.getUUID().equals(uuid)).findFirst().orElse(null);
  }

  public Friend getFriend(String name) {
    return this.getFriend(name, true);
  }

  public Friend getFriend(String name, boolean caseSensitive) {
    if(name == null) {
      throw new NullPointerException("name cannot be null");
    }
    return this.friends.stream().filter(friend -> caseSensitive ? friend.getName().equals(name) : friend.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
  }

  public long getCacheTime() {
    return this.cacheTime;
  }

  public boolean hasLikedServer(Server server) {
    if(server == null) {
      throw new NullPointerException("server cannot be null");
    }
    return server.hasLiked(this.uuid);
  }

  @Override
  public boolean equals(Object object) {
    if(this == object) {
      return true;
    }
    if(object == null || this.getClass() != object.getClass()) {
      return false;
    }
    Profile that = (Profile) object;
    return this.cacheTime == that.cacheTime && Objects.equals(this.uuid, that.uuid) && Objects.equals(this.friends, that.friends);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uuid, this.friends, this.cacheTime);
  }
}