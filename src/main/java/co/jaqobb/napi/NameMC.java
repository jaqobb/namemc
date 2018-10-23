/*
 * This file is a part of napi, licensed under the MIT License.
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
package co.jaqobb.napi;

import co.jaqobb.napi.repository.ProfileRepository;
import co.jaqobb.napi.repository.ServerRepository;
import org.jetbrains.annotations.NotNull;

/**
 * A namemc.
 */
public final class NameMC {
  /**
   * Creates a namemc.
   *
   * @return the namemc
   */
  public static NameMC of() {
    return new NameMC(ProfileRepository.of(), ServerRepository.of());
  }

  /**
   * Creates a namemc with profile and server repository.
   *
   * @param profileRepository the namemc profile repository
   * @param serverRepository the namemc server repository
   * @return the namemc
   */
  public static NameMC of(final @NotNull ProfileRepository profileRepository, final @NotNull ServerRepository serverRepository) {
    return new NameMC(profileRepository, serverRepository);
  }

  /**
   * The profile repository.
   */
  private final ProfileRepository profileRepository;
  /**
   * The server repository.
   */
  private final ServerRepository serverRepository;

  private NameMC(final ProfileRepository profileRepository, final ServerRepository serverRepository) {
    this.profileRepository = profileRepository;
    this.serverRepository = serverRepository;
  }

  /**
   * Gets the namemc profile repository.
   *
   * @return the namemc profile repository
   */
  public ProfileRepository getProfileRepository() {
    return this.profileRepository;
  }

  /**
   * Gets the namemc server repository.
   *
   * @return the namemc server repository
   */
  public ServerRepository getServerRepository() {
    return this.serverRepository;
  }

  /**
   * Clears all repositories' cache.
   */
  public void clearCaches() {
    this.profileRepository.clearProfiles();
    this.serverRepository.clearServers();
  }
}