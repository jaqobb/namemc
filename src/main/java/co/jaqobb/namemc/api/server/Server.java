package co.jaqobb.namemc.api.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import org.json.JSONArray;

/**
 * Class that represents a server.
 */
public final class Server {
	/**
	 * Ip of the server.
	 */
	private final String ip;
	/**
	 * Collection of unique ids that liked the server.
	 */
	private final Collection<UUID> likes;
	/**
	 * Time the server was cached at.
	 */
	private final long cacheTime = System.currentTimeMillis();

	/**
	 * Constructs a new Server class instance with the given ip and JSON array (likes).
	 *
	 * @param ip An ip of the server.
	 * @param array An array that contains information about the unique ids that liked the server.
	 */
	protected Server(String ip, JSONArray array) {
		this.ip = ip;
		Collection<UUID> likes = new ArrayList<>(array.length());
		for (int index = 0; index < array.length(); index++) {
			likes.add(UUID.fromString(array.getString(index)));
		}
		this.likes = likes;
	}

	/**
	 * Returns this server ip.
	 *
	 * @return This server ip.
	 */
	public String getIp() {
		return this.ip;
	}

	/**
	 * Returns an immutable collection of the unique ids that have liked this server.
	 *
	 * @return An immutable collection of the unique ids that have liked this server.
	 */
	public Collection<UUID> getLikes() {
		return Collections.unmodifiableCollection(this.likes);
	}

	/**
	 * Returns true if the given unique id has liked this server, and false otherwise.
	 *
	 * @param uniqueId A unique id to check.
	 *
	 * @return True if the given unique id has liked this server, and false otherwise.
	 *
	 * @throws NullPointerException If the given unique id is null.
	 */
	public boolean hasLiked(UUID uniqueId) {
		Objects.requireNonNull(uniqueId, "uniqueId");
		return this.likes.contains(uniqueId);
	}

	/**
	 * Returns this server cache time.
	 *
	 * @return This server cache time.
	 */
	public long getCacheTime() {
		return this.cacheTime;
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
		Server that = (Server) object;
		return Objects.equals(this.ip, that.ip) && Objects.equals(this.likes, that.likes);
	}

	/**
	 * Returns a hash code of this class.
	 *
	 * @return A hash code of this class.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.ip, this.likes);
	}

	/**
	 * Returns a nice looking representation of this class.
	 *
	 * @return A nice looking representation of this class.
	 */
	@Override
	public String toString() {
		return "Server{" + "ip=" + this.ip + ", likes=" + this.likes + "}";
	}
}