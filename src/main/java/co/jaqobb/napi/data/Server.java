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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public final class Server {
  public static Server of(final String ip, final JSONArray jsonArray) {
    if(ip == null) {
      throw new NullPointerException("Ip cannot be null");
    }
    if(jsonArray == null) {
      throw new NullPointerException("Json array cannot be null");
    }
    final Collection<UUID> likes = new ArrayList<>(jsonArray.length());
    for(int index = 0; index < jsonArray.length(); index++) {
      likes.add(UUID.fromString(jsonArray.getString(index)));
    }
    return new Server(ip, likes);
  }

  private final String ip;
  private final Collection<UUID> likes;
  private final long cacheTime;

  private Server(final String ip, final Collection<UUID> likes) {
    this.ip = ip;
    this.likes = likes;
    this.cacheTime = System.currentTimeMillis();
  }

  public String getIp() {
    return this.ip;
  }

  public Collection<UUID> getLikes() {
    return Collections.unmodifiableCollection(this.likes);
  }

  public boolean hasLiked(final UUID uniqueId) {
    if(uniqueId == null) {
      throw new NullPointerException("Unique id cannot be null");
    }
    return this.likes.contains(uniqueId);
  }

  public long getCacheTime() {
    return this.cacheTime;
  }

  @Override
  public boolean equals(final Object object) {
    if(this == object) {
      return true;
    }
    if(object == null || this.getClass() != object.getClass()) {
      return false;
    }
    final Server server = (Server) object;
    return this.cacheTime == server.cacheTime && Objects.equals(this.ip, server.ip) && Objects.equals(this.likes, server.likes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.ip, this.likes, this.cacheTime);
  }
}