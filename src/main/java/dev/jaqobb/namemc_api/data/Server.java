/*
 * This file is a part of namemc-api, licensed under the MIT License.
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
	private final long cacheTime;

	public Server(String address, Collection<UUID> likes) {
		if (address == null) {
			throw new NullPointerException("address cannot be null");
		}
		if (likes == null) {
			throw new NullPointerException("likes cannot be null");
		}
		this.address = address;
		this.likes = likes;
		this.cacheTime = Instant.now().toEpochMilli();
	}

	public String getAddress() {
		return this.address;
	}

	public Collection<UUID> getLikes() {
		return Collections.unmodifiableCollection(this.likes);
	}

	public boolean hasLiked(UUID uniqueId) {
		if (uniqueId == null) {
			throw new NullPointerException("uniqueId cannot be null");
		}
		return this.likes.contains(uniqueId);
	}

	public long getCacheTime() {
		return this.cacheTime;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || this.getClass() != object.getClass()) {
			return false;
		}
		Server that = (Server) object;
		return this.cacheTime == that.cacheTime &&
			Objects.equals(this.address, that.address) &&
			Objects.equals(this.likes, that.likes);
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