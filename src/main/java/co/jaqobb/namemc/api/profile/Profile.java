package co.jaqobb.namemc.api.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import co.jaqobb.namemc.api.server.Server;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class that represents a profile.
 */
public final class Profile {
	/**
	 * Unique id of the profile.
	 */
	private final UUID uniqueId;
	/**
	 * Collection of the profile friends.
	 */
	private final Collection<Friend> friends;
	/**
	 * Time the profile was cached at.
	 */
	private final long cacheTime = System.currentTimeMillis();

	/**
	 * Constructs a new Profile instance with the given unique id and JSON array (friends).
	 *
	 * @param uniqueId A unique id of the profile
	 * @param array An array that contains information about friends.
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
	}

	/**
	 * Returns this profile unique id.
	 *
	 * @return This profile unique id.
	 */
	public UUID getUniqueId() {
		return this.uniqueId;
	}

	/**
	 * Returns an immutable collection of friends of this profile.
	 *
	 * @return An immutable collection of friends of this profile.
	 */
	public Collection<Friend> getFriends() {
		return Collections.unmodifiableCollection(this.friends);
	}

	/**
	 * Returns a Friend class instance if this profile has a friend with the given unique id, and null otherwise.
	 *
	 * @param uniqueId A unique id to check.
	 *
	 * @return A Friend class instance if this profile has a friend with the given unique id, and null otherwise.
	 *
	 * @throws NullPointerException If the given unique id is null.
	 */
	public Friend getFriend(UUID uniqueId) {
		Objects.requireNonNull(uniqueId, "uniqueId");
		return this.friends.stream().filter(friend -> friend.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
	}

	/**
	 * Returns a Friend class instance if this profile has a friend with the given name, and null otherwise.
	 *
	 * @param name A name to check (case sensitive).
	 *
	 * @return A Friend class instance if this profile has a friend with the given name, and null otherwise.
	 *
	 * @throws NullPointerException If the given name is null.
	 */
	public Friend getFriend(String name) {
		return this.getFriend(name, true);
	}

	/**
	 * Returns a Friend class instance if this profile has a friend with the given name, and null otherwise.
	 *
	 * @param name A name to check.
	 * @param caseSensitive A state which defines if case sensitivity in the given name should be checked.
	 *
	 * @return A Friend class instance if this profile has a friend with the given name, and null otherwise.
	 *
	 * @throws NullPointerException if the given name is null.
	 */
	public Friend getFriend(String name, boolean caseSensitive) {
		Objects.requireNonNull(name, "name");
		return this.friends.stream().filter(friend -> caseSensitive ? friend.getName().equals(name) : friend.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	/**
	 * Returns true if this profile has a friend with the given unique id, and false otherwise.
	 *
	 * @param uniqueId A unique id to check.
	 *
	 * @return True if this profile has a friend with the given unique id, and false otherwise.
	 *
	 * @throws NullPointerException If the given unique id is null.
	 */
	public boolean hasFriend(UUID uniqueId) {
		Objects.requireNonNull(uniqueId, "uniqueId");
		return this.friends.stream().anyMatch(friend -> friend.getUniqueId().equals(uniqueId));
	}

	/**
	 * Returns true if this profile has a friend with the given name, and false otherwise.
	 *
	 * @param name A name to check (case sensitive).
	 *
	 * @return True if this profile has a friend with the given name, and false otherwise.
	 *
	 * @throws NullPointerException If the given name is null.
	 */
	public boolean hasFriend(String name) {
		return this.hasFriend(name, true);
	}

	/**
	 * Returns true if this profile has a friend with the given name, and false otherwise.
	 *
	 * @param name A name to check.
	 * @param caseSensitive A state which defines if case sensitivity in the given name should be checked.
	 *
	 * @return True if this profile has a friend with the given name, and false otherwise.
	 *
	 * @throws NullPointerException If the given name is null.
	 */
	public boolean hasFriend(String name, boolean caseSensitive) {
		Objects.requireNonNull(name, "name");
		return this.friends.stream().anyMatch(friend -> caseSensitive ? friend.getName().equals(name) : friend.getName().equalsIgnoreCase(name));
	}

	/**
	 * Returns this profile cache time.
	 *
	 * @return This profile cache time.
	 */
	public long getCacheTime() {
		return this.cacheTime;
	}

	/**
	 * Returns true if this profile has liked the given server and false otherwise.
	 *
	 * @param server A server to check.
	 *
	 * @return True if this profile has liked the given server and false otherwise.
	 *
	 * @throws NullPointerException If the given server is null.
	 */
	public boolean hasLiked(Server server) {
		Objects.requireNonNull(server, "server");
		return server.hasLiked(this.uniqueId);
	}

	/**
	 * Returns true if the given object is the same as this class, and false otherwise.
	 *
	 * @param object An object to check.
	 *
	 * @return True if the given object is the same as this class, and false otherwise.
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
		return Objects.equals(this.uniqueId, that.uniqueId) && Objects.equals(this.friends, that.friends);
	}

	/**
	 * Returns a hash code of this class.
	 *
	 * @return A hash code of this class.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.uniqueId, this.friends);
	}

	/**
	 * Returns a nice looking representation of this class.
	 *
	 * @return A nice looking representation of this class.
	 */
	@Override
	public String toString() {
		return "Profile{" + "uniqueId=" + this.uniqueId + ", friends=" + this.friends + "}";
	}
}