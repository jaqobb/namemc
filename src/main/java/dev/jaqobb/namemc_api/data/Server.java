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

public final class Server {
  private final String address;
  private final Collection<UUID> likes;
  private final Instant cacheTime;

  public Server(final String address, final Collection<UUID> likes) {
    this.address = Objects.requireNonNull(address, "address").toLowerCase();
    Objects.requireNonNull(likes, "likes");
    for(final UUID like : likes) {
      Objects.requireNonNull(like, "like");
    }
    this.likes = likes;
    this.cacheTime = Instant.now();
  }

  public String getAddress() {
    return this.address;
  }

  public Collection<UUID> getLikes() {
    return Collections.unmodifiableCollection(this.likes);
  }

  public boolean hasLiked(final UUID uniqueId) {
    Objects.requireNonNull(uniqueId, "uniqueId");
    return this.likes.contains(uniqueId);
  }

  public Instant getCacheTime() {
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
    final Server that = (Server) object;
    return Objects.equals(this.address, that.address) &&
      Objects.equals(this.likes, that.likes) &&
      Objects.equals(this.cacheTime, that.cacheTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.address, this.likes, this.cacheTime);
  }

  @Override
  public String toString() {
    return "Server{" +
      "address='" + this.address + "'" +
      ", likes=" + this.likes +
      ", cacheTime=" + this.cacheTime +
      "}";
  }
}