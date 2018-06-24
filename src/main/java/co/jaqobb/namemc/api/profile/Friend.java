/*
 * The MIT License.
 *
 * Copyright (c) jaqobb (Jakub Zagórski) <jaqobb@jaqobb.co>
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

import java.util.Objects;
import java.util.UUID;

import co.jaqobb.namemc.api.server.Server;

/**
 * Class that represents a friend.
 */
public class Friend
{
	/**
	 * Unique id of the friend.
	 */
	private final UUID   uniqueId;
	/**
	 * Name of the friend.
	 */
	private final String name;
	/**
	 * Time the friend was cached at.
	 */
	private final long   cacheTime = System.currentTimeMillis();

	/**
	 * Constructs a new Friend class instance with the given unique id and name.
	 *
	 * @param uniqueId A unique id of the friend.
	 * @param name     A name of the friend.
	 */
	protected Friend(UUID uniqueId, String name)
	{
		this.uniqueId = uniqueId;
		this.name = name;
	}

	/**
	 * Returns this friend unique id.
	 *
	 * @return This friend unique id.
	 */
	public UUID getUniqueId()
	{
		return this.uniqueId;
	}

	/**
	 * Returns this friend name.
	 *
	 * @return This friend name.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Returns this friend cache time.
	 *
	 * @return Time friend cache time.
	 */
	public long getCacheTime()
	{
		return this.cacheTime;
	}

	/**
	 * Returns true if this friend is the given profile friend and false otherwise. Profile name is case sensitive.
	 *
	 * @param profile A profile to check.
	 *
	 * @return True if this friend is the given profile friend.
	 *
	 * @throws NullPointerException If the given profile is null.
	 */
	public boolean isFriendOf(Profile profile)
	{
		return this.isFriendOf(profile, true);
	}

	/**
	 * Returns true if this friend is the given profile friend and false otherwise.
	 *
	 * @param profile       A profile to check.
	 * @param caseSensitive A state which defines if case sensitivity in the given profile name should be checked.
	 *
	 * @return True if this friend is the given profile friend and false otherwise.
	 *
	 * @throws NullPointerException If the given profile is null.
	 */
	public boolean isFriendOf(Profile profile, boolean caseSensitive)
	{
		Objects.requireNonNull(profile, "profile");
		if (profile.hasFriend(this.uniqueId))
		{
			return true;
		}
		return profile.hasFriend(this.name, caseSensitive);
	}

	/**
	 * Returns true if this friend has liked the given server and false otherwise.
	 *
	 * @param server A server to check.
	 *
	 * @return True if this friend has liked the given server and false otherwise.
	 *
	 * @throws NullPointerException If the given server is null.
	 */
	public boolean hasLiked(Server server)
	{
		Objects.requireNonNull(server, "server");
		return server.hasLiked(this.uniqueId);
	}

	/**
	 * Returns true if the given object is the same as this class and false otherwise.
	 *
	 * @param object An object to check.
	 *
	 * @return True if the given object is the same as this class and false otherwise.
	 */
	@Override
	public boolean equals(Object object)
	{
		if (this == object)
		{
			return true;
		}
		if (object == null || this.getClass() != object.getClass())
		{
			return false;
		}
		Friend that = (Friend) object;
		return Objects.equals(this.uniqueId, that.uniqueId) && Objects.equals(this.name, that.name);
	}

	/**
	 * Returns a hash code of this class.
	 *
	 * @return A hash code of this class.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(this.uniqueId, this.name);
	}

	/**
	 * Returns a nice looking representation of this class.
	 *
	 * @return A nice looking representation of this class.
	 */
	@Override
	public String toString()
	{
		return "Friend{" + "uniqueId=" + this.uniqueId + ", name=" + this.name + "}";
	}
}