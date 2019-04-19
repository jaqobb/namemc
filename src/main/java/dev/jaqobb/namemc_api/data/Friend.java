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
import java.util.Objects;
import java.util.UUID;

public final class Friend {
  public static Friend of(final UUID uniqueId, final String name) {
    if(uniqueId == null) {
      throw new NullPointerException("uniqueId");
    }
    if(name == null) {
      throw new NullPointerException("name");
    }
    return new Friend(uniqueId, name);
  }

  private final UUID uniqueId;
  private final String name;
  private final long cacheTime;

  protected Friend(final UUID uniqueId, final String name) {
    this.uniqueId = uniqueId;
    this.name = name;
    this.cacheTime = Instant.now().toEpochMilli();
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public String getName() {
    return this.name;
  }

  public long getCacheTime() {
    return this.cacheTime;
  }

  public boolean isFriendOf(final Profile profile) {
    return this.isFriendOf(profile, true);
  }

  public boolean isFriendOf(final Profile profile, final boolean caseSensitive) {
    if(profile == null) {
      throw new NullPointerException("profile");
    }
    if(profile.getFriend(this.uniqueId).isPresent()) {
      return true;
    }
    return profile.getFriend(this.name, caseSensitive).isPresent();
  }

  public boolean hasLikedServer(final Server server) {
    if(server == null) {
      throw new NullPointerException("server");
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
    final Friend that = (Friend) object;
    return this.cacheTime == that.cacheTime &&
      Objects.equals(this.uniqueId, that.uniqueId) &&
      Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uniqueId, this.name, this.cacheTime);
  }

  @Override
  public String toString() {
    return "Friend{" +
      "uniqueId=" + this.uniqueId +
      ", name='" + this.name + "'" +
      ", cacheTime=" + this.cacheTime +
      "}";
  }
}