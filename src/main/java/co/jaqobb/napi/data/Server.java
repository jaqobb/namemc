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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * A server.
 */
public final class Server {
  /**
   * Creates a server with ip and array.
   *
   * @param ip the server ip
   * @param array the server array (likes)
   * @return the server
   */
  public static Server of(final @NotNull String ip, final @NotNull JSONArray array) {
    return new Server(ip, array);
  }

  /**
   * The ip.
   */
  private final String ip;
  /**
   * The likes.
   */
  private final Collection<UUID> likes;
  /**
   * The cache time.
   */
  private final long cacheTime;

  private Server(final String ip, final JSONArray array) {
    this.ip = ip;
    this.likes = new ArrayList<>(array.length());
    for(int index = 0; index < array.length(); index++) {
      this.likes.add(UUID.fromString(array.getString(index)));
    }
    this.cacheTime = System.currentTimeMillis();
  }

  /**
   * Gets the server ip.
   *
   * @return the server ip.
   */
  public String getIp() {
    return this.ip;
  }

  /**
   * Gets the server likes.
   *
   * @return the server likes
   */
  public Collection<UUID> getLikes() {
    return Collections.unmodifiableCollection(this.likes);
  }

  /**
   * Gets if a unique id has liked the server.
   *
   * @param uniqueId the unique id to check
   * @return {@code true} if the unique id has liked the server or {@code false} otherwise
   */
  public boolean hasLiked(final @NotNull UUID uniqueId) {
    return this.likes.contains(uniqueId);
  }

  /**
   * Gets the server cache time.
   *
   * @return the server cache time.
   */
  public long getCacheTime() {
    return this.cacheTime;
  }

  @Override
  public boolean equals(final @Nullable Object object) {
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