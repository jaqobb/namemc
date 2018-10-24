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
  public static Friend of(final UUID uniqueId, final String nick) {
    if(uniqueId == null) {
      throw new NullPointerException("Unique id cannot be null");
    }
    if(nick == null) {
      throw new NullPointerException("Nick cannot be null");
    }
    return new Friend(uniqueId, nick);
  }

  private final UUID uniqueId;
  private final String nick;
  private final long cacheTime;

  private Friend(final UUID uniqueId, final String nick) {
    this.uniqueId = uniqueId;
    this.nick = nick;
    this.cacheTime = System.currentTimeMillis();
  }

  public UUID getUniqueId() {
    return this.uniqueId;
  }

  public String getNick() {
    return this.nick;
  }

  public long getCacheTime() {
    return this.cacheTime;
  }

  public boolean isFriendOf(final Profile profile) {
    return this.isFriendOf(profile, true);
  }

  public boolean isFriendOf(final Profile profile, final boolean caseSensitive) {
    if(profile == null) {
      throw new NullPointerException("Profile cannot be null");
    }
    if(profile.getFriend(this.uniqueId) != null) {
      return true;
    }
    return profile.getFriend(this.nick, caseSensitive) != null;
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
    final Friend friend = (Friend) object;
    return this.cacheTime == friend.cacheTime && Objects.equals(this.uniqueId, friend.uniqueId) && Objects.equals(this.nick, friend.nick);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uniqueId, this.nick, this.cacheTime);
  }
}