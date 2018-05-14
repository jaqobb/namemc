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

package co.jaqobb.namemc.api.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import co.jaqobb.namemc.api.json.JSONArray;
import co.jaqobb.namemc.api.json.JSONObject;

/**
 * Class that holds all possible
 * information about the profile.
 */
public class Profile {
	/**
	 * Unique id of the profile.
	 */
	private final UUID uniqueId;
	/**
	 * Collection of friends.
	 */
	private final Collection<Friend> friends;
	/**
	 * Time the profile was cached at.
	 */
	private final long cacheTime;

	/**
	 * Constructs new {@code Profile} instance
	 * with the given unique id and array.
	 *
	 * @param uniqueId a unique id of the profile.
	 * @param array    an array that contains
	 *                 information about the friends.
	 */
	protected Profile(UUID uniqueId, JSONArray array) {
		this.uniqueId = uniqueId;
		Collection<Friend> friends = new ArrayList<>(array.length());
		for (int index = 0; index < array.length(); index++) {
			JSONObject object = array.getJSONObject(index);
			UUID friendUniqueId = UUID.fromString(object.getString("uuid"));
			String friendName = object.getString("name");
			friends.add(new Friend(friendUniqueId, friendName));
		}
		this.friends = friends;
		this.cacheTime = System.currentTimeMillis();
	}

	/**
	 * Returns a unique id of this profile.
	 *
	 * @return a unique id of this profile.
	 */
	public UUID getUniqueId() {
		return this.uniqueId;
	}

	/**
	 * Returns a collection of friends
	 * of this profile.
	 *
	 * @return a collection of friends of this profile.
	 */
	public Collection<Friend> getFriends() {
		return Collections.unmodifiableCollection(this.friends);
	}

	/**
	 * Returns a friend instance if
	 * this profile has a friend with
	 * the given unique id, null otherwise.
	 *
	 * @param uniqueId a unique id of the friend to be checked.
	 *
	 * @return a {@code Friend} instance if this
	 * profile has a friend with the given unique id,
	 * {@code null} otherwise.
	 */
	public Friend getFriend(UUID uniqueId) {
		return this.friends.stream().filter(friend -> friend.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
	}

	/**
	 * Returns a friend instance if
	 * this profile has a friend with
	 * the given name, null otherwise.
	 *
	 * @param name a name of the friend to
	 *             be checked (case sensitive).
	 *
	 * @return a {@code Friend} instance if this
	 * profile has a friend with the given name,
	 * {@code null} otherwise.
	 */
	public Friend getFriend(String name) {
		return this.getFriend(name, true);
	}

	/**
	 * Returns a friend instance if
	 * this profile has a friend with
	 * the given name, null otherwise.
	 *
	 * @param name          a name of the friend to
	 *                      be checked.
	 * @param caseSensitive a state which defines if
	 *                      case sensitivity in the
	 *                      name should be checked.
	 *
	 * @return a {@code Friend} instance if this
	 * profile has a friend with the given name,
	 * {@code null} otherwise.
	 */
	public Friend getFriend(String name, boolean caseSensitive) {
		return this.friends.stream().filter(friend -> caseSensitive ? friend.getName().equals(name) : friend.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	/**
	 * Checks if this profile has
	 * a friend with the unique id.
	 *
	 * @param uniqueId a unique id of the friend to be checked.
	 *
	 * @return {@code true} if this profile has a
	 * friend with the given unique id, {@code false}
	 * otherwise.
	 */
	public boolean hasFriend(UUID uniqueId) {
		return this.friends.stream().anyMatch(friend -> friend.getUniqueId().equals(uniqueId));
	}

	/**
	 * Checks if this profile has
	 * a friend with the given name.
	 *
	 * @param name a name of the friend to
	 *             be checked (case sensitive).
	 *
	 * @return {@code true} if this profile has a
	 * friend with the given name, {@code false}
	 * otherwise.
	 */
	public boolean hasFriend(String name) {
		return this.hasFriend(name, true);
	}

	/**
	 * Checks if this profile has
	 * a friend with the given name.
	 *
	 * @param name          a name of the friend to
	 *                      be checked.
	 * @param caseSensitive a state which defines if
	 *                      case sensitivity in the
	 *                      name should be checked.
	 *
	 * @return {@code true} if this profile has a
	 * friend with the given name, {@code false}
	 * otherwise.
	 */
	public boolean hasFriend(String name, boolean caseSensitive) {
		return this.friends.stream().anyMatch(friend -> caseSensitive ? friend.getName().equals(name) : friend.getName().equalsIgnoreCase(name));
	}

	/**
	 * Returns time this profile
	 * was cached at.
	 *
	 * @return time this profile
	 * was cached at.
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
		Profile that = (Profile) object;
		return this.cacheTime == that.cacheTime && Objects.equals(this.uniqueId, that.uniqueId) && Objects.equals(this.friends, that.friends);
	}

	/**
	 * Returns a hash code
	 * of this class.
	 *
	 * @return a hash code of this class.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.uniqueId, this.friends, this.cacheTime);
	}

	/**
	 * Returns a nice looking representation
	 * of this class.
	 *
	 * @return a nice looking representation
	 * of this class.
	 */
	@Override
	public String toString() {
		return "Profile{" + "uniqueId=" + this.uniqueId + ", friends=" + this.friends + ", cacheTime=" + this.cacheTime + "}";
	}

	/**
	 * Class that holds all possible
	 * information about the friend.
	 */
	public static class Friend {
		/**
		 * Unique id of the friend.
		 */
		private final UUID uniqueId;
		/**
		 * Name of the friend.
		 */
		private final String name;

		/**
		 * Constructs new {@code Friend} instance
		 * with the given unique id and name.
		 *
		 * @param uniqueId a unique id of the friend.
		 * @param name     a name of the friend.
		 */
		protected Friend(UUID uniqueId, String name) {
			this.uniqueId = uniqueId;
			this.name = name;
		}

		/**
		 * Returns a unique id of this friend.
		 *
		 * @return a unique id of this friend.
		 */
		public UUID getUniqueId() {
			return this.uniqueId;
		}

		/**
		 * Returns a name of this friend.
		 *
		 * @return a name of this friend.
		 */
		public String getName() {
			return this.name;
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
			Friend that = (Friend) object;
			return Objects.equals(this.uniqueId, that.uniqueId) && Objects.equals(this.name, that.name);
		}

		/**
		 * Returns a hash code
		 * of this class.
		 *
		 * @return a hash code of this class.
		 */
		@Override
		public int hashCode() {
			return Objects.hash(this.uniqueId, this.name);
		}

		/**
		 * Returns a nice looking representation of this class.
		 *
		 * @return a nice looking representation of this class.
		 */
		@Override
		public String toString() {
			return "Friend{" + "uniqueId=" + this.uniqueId + ", name=" + this.name + "}";
		}
	}
}