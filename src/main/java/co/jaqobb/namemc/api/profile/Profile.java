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

import co.jaqobb.namemc.api.server.Server;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class that holds all possible information about the profile.
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
  private final long cacheTime = System.currentTimeMillis();

  /**
   * Constructs new profile instance with the given unique id and JSON array.
   *
   * @param uniqueId A unique id of the profile
   * @param array An array that contains information about the friends.
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
   * Returns a unique id of this profile.
   *
   * @return A unique id of this profile.
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
   * Returns a friend instance if this profile has a friend with the given unique id and null otherwise.
   *
   * @param uniqueId A unique id of the friend to check.
   * @return A friend instance if this profile has a friend with the given unique id and null otherwise.
   * @throws NullPointerException If the given unique id is null.
   */
  public Friend getFriend(UUID uniqueId) {
    Objects.requireNonNull(uniqueId, "uniqueId");
    return this.friends.stream().filter(friend -> friend.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
  }

  /**
   * Returns a friend instance if this profile has a friend with the given name and null otherwise.
   *
   * @param name A name of the friend to check (case sensitive).
   * @return A friend instance if this profile has a friend with the given name and null otherwise.
   * @throws NullPointerException If the given name is null.
   */
  public Friend getFriend(String name) {
    return this.getFriend(name, true);
  }

  /**
   * Returns a friend instance if this profile has a friend with the given name and null otherwise.
   *
   * @param name A name of the friend to check.
   * @param caseSensitive A state which defines if case sensitivity in the given name should be checked.
   * @return A friend instance if this profile has a friend with the given name and null otherwise.
   * @throws NullPointerException if the given name is null.
   */
  public Friend getFriend(String name, boolean caseSensitive) {
    Objects.requireNonNull(name, "name");
    return this.friends.stream().filter(friend -> caseSensitive ? friend.getName().equals(name) : friend.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
  }

  /**
   * Returns true if this profile has a friend with the given unique id and false otherwise.
   *
   * @param uniqueId A unique id of the friend to check.
   * @return True if this profilee has a friend with the given unique id and false otherwise.
   * @throws NullPointerException If the given unique id is null.
   */
  public boolean hasFriend(UUID uniqueId) {
    Objects.requireNonNull(uniqueId, "uniqueId");
    return this.friends.stream().anyMatch(friend -> friend.getUniqueId().equals(uniqueId));
  }

  /**
   * Returns true if this profile has a friend with the given name and false otherwise.
   *
   * @param name A name of the friend to check (case sensitive).
   * @return True if this profile has a friend with the given name and false otherwise.
   * @throws NullPointerException If the given name is null.
   */
  public boolean hasFriend(String name) {
    return this.hasFriend(name, true);
  }

  /**
   * Returns true if this profile has a friend with the given name and false otherwise.
   *
   * @param name A name of the friend to check.
   * @param caseSensitive A state which defines if case sensitivity in the given name should be checked.
   * @return True if this profile has a friend with the given name and false otherwise.
   * @throws NullPointerException If the given name is null.
   */
  public boolean hasFriend(String name, boolean caseSensitive) {
    Objects.requireNonNull(name, "name");
    return this.friends.stream().anyMatch(friend -> caseSensitive ? friend.getName().equals(name) : friend.getName().equalsIgnoreCase(name));
  }

  /**
   * Returns time this profile was cached at.
   *
   * @return time this profile was cached at.
   */
  public long getCacheTime() {
    return this.cacheTime;
  }

  /**
   * Returns true if this profile has liked the given server and false otherwise.
   *
   * @param server A server to check.
   * @return True if this profile has liked the given server and false otherwise.
   * @throws NullPointerException If the given server is null.
   */
  public boolean hasLiked(Server server) {
    Objects.requireNonNull(server, "server");
    return server.hasLiked(this.uniqueId);
  }

  /**
   * Returns true if the given object is the same as this class and false otherwise.
   *
   * @param object An object to check.
   * @return True if the given object is the same as this class and false otherwise.
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

  /**
   * Class that holds all possible information about the friend.
   */
  public class Friend {

    /**
     * Unique id of the friend.
     */
    private final UUID uniqueId;
    /**
     * Name of the friend.
     */
    private final String name;
    /**
     * Time the friend was cached at.
     */
    private final long cacheTime = System.currentTimeMillis();

    /**
     * Constructs new friend instance with the given unique id and name.
     *
     * @param uniqueId A unique id of the friend.
     * @param name A name of the friend.
     */
    protected Friend(UUID uniqueId, String name) {
      this.uniqueId = uniqueId;
      this.name = name;
    }

    /**
     * Returns a unique id of this friend.
     *
     * @return A unique id of this friend.
     */
    public UUID getUniqueId() {
      return this.uniqueId;
    }

    /**
     * Returns a name of this friend.
     *
     * @return A name of this friend.
     */
    public String getName() {
      return this.name;
    }

    /**
     * Returns time this friend was cached at.
     *
     * @return Time this friend was cached at.
     */
    public long getCacheTime() {
      return this.cacheTime;
    }

    /**
     * Returns true if this friend is the given profile's friend and false otherwise. Profile's name is case sensitive.
     *
     * @param profile A profile to check.
     * @return True if this friend is the given profile's friend.
     * @throws NullPointerException If the given profile is null.
     */
    public boolean isFriendOf(Profile profile) {
      return this.isFriendOf(profile, true);
    }

    /**
     * Returns true if this friend is the given profile's friend and false otherwise.
     *
     * @param profile A profile to check.
     * @param caseSensitive A state which defines if case sensitivity in the given profile's name should be checked.
     * @return True if this friend is the given profile's friend and false otherwise.
     * @throws NullPointerException If the given profile is null.
     */
    public boolean isFriendOf(Profile profile, boolean caseSensitive) {
      Objects.requireNonNull(profile, "profile");
      if (profile.hasFriend(this.uniqueId)) {
        return true;
      }
      return profile.hasFriend(this.name, caseSensitive);
    }

    /**
     * Returns true if this friend has liked the given server and false otherwise.
     *
     * @param server A server to check.
     * @return True if this friend has liked the given server and false otherwise.
     * @throws NullPointerException If the given server is null.
     */
    public boolean hasLiked(Server server) {
      Objects.requireNonNull(server, "server");
      return server.hasLiked(this.uniqueId);
    }

    /**
     * Returns true if the given object is the same as this class and false otherwise.
     *
     * @param object An object to check.
     * @return True if the given object is the same as this class and false otherwise.
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
     * Returns a hash code of this class.
     *
     * @return A hash code of this class.
     */
    @Override
    public int hashCode() {
      return Objects.hash(this.uniqueId, this.name);
    }

    /**
     * Returns a nice looking representation of this class.
     *
     * @return A nice looking representation of this class.
     */
    @Override
    public String toString() {
      return "Friend{" + "uniqueId=" + this.uniqueId + ", name=" + this.name + "}";
    }
  }

}