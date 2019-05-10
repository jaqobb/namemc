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

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Friend {

	@NotNull
	private UUID uniqueId;
	@NotNull
	private String name;

	public Friend(@NotNull UUID uniqueId, @NotNull String name) {
		this.uniqueId = uniqueId;
		this.name = name;
	}

	@NotNull
	public UUID getUniqueId() {
		return this.uniqueId;
	}

	@NotNull
	public String getName() {
		return this.name;
	}

	public boolean isFriendOf(@NotNull Profile profile) {
		return isFriendOf(profile, true);
	}

	public boolean isFriendOf(@NotNull Profile profile, boolean caseSensitive) {
		if (profile.getFriend(this.uniqueId) != null) {
			return true;
		}
		return profile.getFriend(this.name, caseSensitive) != null;
	}

	public boolean hasLikedServer(@NotNull Server server) {
		return server.hasLiked(this.uniqueId);
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		Friend that = (Friend) object;
		return Objects.equals(this.uniqueId, that.uniqueId) && Objects.equals(this.name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uniqueId, this.name);
	}
}
