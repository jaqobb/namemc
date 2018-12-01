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

import java.util.Objects;
import java.util.UUID;

public final class Friend {
  public static Friend of(UUID uuid, String name) {
    if(uuid == null) {
      throw new NullPointerException("UUID cannot be null");
    }
    if(name == null) {
      throw new NullPointerException("Name cannot be null");
    }
    return new Friend(uuid, name);
  }

  private UUID uuid;
  private String name;
  private long cacheTime;

  private Friend(UUID uuid, String name) {
    this.uuid = uuid;
    this.name = name;
    this.cacheTime = System.currentTimeMillis();
  }

  public UUID getUUID() {
    return this.uuid;
  }

  public String getName() {
    return this.name;
  }

  public long getCacheTime() {
    return this.cacheTime;
  }

  public boolean isFriendOf(Profile profile) {
    return this.isFriendOf(profile, true);
  }

  public boolean isFriendOf(Profile profile, boolean caseSensitive) {
    if(profile == null) {
      throw new NullPointerException("Profile cannot be null");
    }
    if(profile.getFriend(this.uuid) != null) {
      return true;
    }
    return profile.getFriend(this.name, caseSensitive) != null;
  }

  public boolean hasLikedServer(Server server) {
    if(server == null) {
      throw new NullPointerException("Server cannot be null");
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
    Friend that = (Friend) object;
    return this.cacheTime == that.cacheTime && Objects.equals(this.uuid, that.uuid) && Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uuid, this.name, this.cacheTime);
  }
}