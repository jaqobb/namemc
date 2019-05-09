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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Server {

	@NotNull
	private String _address;
	@NotNull
	private Collection<UUID> _likes;
	@NotNull
	private Instant _cacheTime;

	public Server(@NotNull String address, @NotNull Collection<UUID> likes) {
		_address = address.toLowerCase();
		_likes = likes;
		_cacheTime = Instant.now();
	}

	@NotNull
	public String getAddress() {
		return _address;
	}

	@NotNull
	public Collection<UUID> getLikes() {
		return Collections.unmodifiableCollection(_likes);
	}

	public boolean hasLiked(@NotNull UUID uniqueId) {
		return _likes.contains(uniqueId);
	}

	@NotNull
	public Instant getCacheTime() {
		return _cacheTime;
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		Server that = (Server) object;
		return Objects.equals(_address, that._address) &&
			Objects.equals(_likes, that._likes) &&
			Objects.equals(_cacheTime, that._cacheTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_address, _likes, _cacheTime);
	}
}
