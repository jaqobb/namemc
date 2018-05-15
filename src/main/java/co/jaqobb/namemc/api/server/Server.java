/*
 * MIT License
 *
 * Copyright (c) 2018 Jakub Zagórski
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

package co.jaqobb.namemc.api.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import co.jaqobb.namemc.api.json.JSONArray;

/**
 * Class that holds all possible
 * information about the server.
 */
public class Server {
	/**
	 * Ip of the {@code Server}.
	 */
	private final String ip;
	/**
	 * Collection of unique ids that liked the {@code Server}.
	 */
	private final Collection<UUID> likes;
	/**
	 * Time the {@code Server} was cached at.
	 */
	private final long cacheTime;

	/**
	 * Constructs new {@code Server} instance
	 * with the given {@code ip} and {@code array{}.
	 *
	 * @param ip    an ip of the server.
	 * @param array an array that contains
	 *              information about the
	 *              unique ids that liked
	 *              the {@code Server}.
	 */
	protected Server(String ip, JSONArray array) {
		this.ip = ip;
		Collection<UUID> likes = new ArrayList<>(array.length());
		for (int index = 0; index < array.length(); index++) {
			likes.add(UUID.fromString(array.getString(index)));
		}
		this.likes = likes;
		this.cacheTime = System.currentTimeMillis();
	}

	/**
	 * Returns this {@code Server} ip.
	 *
	 * @return this {@code Server} ip.
	 */
	public String getIp() {
		return this.ip;
	}

	/**
	 * Returns an immutable collection of the unique ids
	 * that have liked this {@code Server}.
	 *
	 * @return an immutable collection of the unique ids
	 * that have liked this {@code Server}.
	 */
	public Collection<UUID> getLikes() {
		return Collections.unmodifiableCollection(this.likes);
	}

	/**
	 * Checks if the given {@code uniqueId}
	 * has liked this {@code Server}.
	 *
	 * @param uniqueId a unique id to be checked.
	 *
	 * @return {@code true} if the given {@code uniqueId} has
	 * liked this {@code Server}, {@code false} otherwise.
	 *
	 * @throws NullPointerException if the {@code uniqueId} is null.
	 */
	public boolean hasLiked(UUID uniqueId) {
		Objects.requireNonNull(uniqueId, "uniqueId");
		return this.likes.contains(uniqueId);
	}

	/**
	 * Returns time this {@code Server} was cached at.
	 *
	 * @return time this {@code Server} was cached at.
	 */
	public long getCacheTime() {
		return this.cacheTime;
	}

	/**
	 * Checks if the given object
	 * is the same as this class.
	 *
	 * @param object an object to be checked.
	 *
	 * @return {@code true} if both objects
	 * are the same, {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || this.getClass() != object.getClass()) {
			return false;
		}
		Server that = (Server) object;
		return this.cacheTime == that.cacheTime && Objects.equals(this.ip, that.ip) && Objects.equals(this.likes, that.likes);
	}

	/**
	 * Returns a hash code of this class.
	 *
	 * @return a hash code of this class.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.ip, this.likes, this.cacheTime);
	}

	/**
	 * Returns a nice looking representation of this class.
	 *
	 * @return a nice looking representation of this class.
	 */
	@Override
	public String toString() {
		return "Server{" + "ip=" + this.ip + ", likes=" + this.likes + ", cacheTime=" + this.cacheTime + "}";
	}
}