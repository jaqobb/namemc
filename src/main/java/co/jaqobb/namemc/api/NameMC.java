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

package co.jaqobb.namemc.api;

import java.util.Objects;

import co.jaqobb.namemc.api.profile.ProfileService;
import co.jaqobb.namemc.api.server.ServerService;

/**
 * Class where the fun with the API begins.
 */
public class NameMC {

  /**
   * Creates new name mc instance with the default settings.
   *
   * @return New name mc instance with the default settings.
   */
  public static NameMC ofDefault() {
    return new NameMC();
  }

  /**
   * Creates new name mc instance with the possibly custom profile service and server service.
   *
   * @return New name mc instance with the possibly custom profile service and server service.
   * @throws NullPointerException If the given profile service or the given server service is null.
   */
  public static NameMC ofCustom(ProfileService profileService, ServerService serverService) {
    return new NameMC(Objects.requireNonNull(profileService, "profileService"), Objects.requireNonNull(serverService, "serverService"));
  }

  /**
   * Currently used profile service.
   */
  private final ProfileService profileService;
  /**
   * Currently used server service.
   */
  private final ServerService serverService;

  /**
   * Creates new name mc instance with the default settings.
   *
   * @return New name mc instance with the default settings.
   */
  private NameMC() {
    this.profileService = ProfileService.ofDefault();
    this.serverService = ServerService.ofDefault();
  }

  /**
   * Creates new namemc instance with the possibly custom profile service and server service.
   *
   * @return New namemc instance with the possibly custom profile service and server service.
   */
  private NameMC(ProfileService profileService, ServerService serverService) {
    this.profileService = profileService;
    this.serverService = serverService;
  }

  /**
   * Returns currently used profile service.
   *
   * @return Currently used profile service.
   */
  public ProfileService getProfileService() {
    return this.profileService;
  }

  /**
   * Returns currently used server service.
   *
   * @return Currently used server service.
   */
  public ServerService getServerService() {
    return this.serverService;
  }

  /**
   * Clears the profile service and the server service cache.
   */
  public void clearCaches() {
    this.profileService.clearProfiles();
    this.serverService.clearServers();
  }

}