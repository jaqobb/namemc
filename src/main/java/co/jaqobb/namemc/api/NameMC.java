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
 * Class where the whole API begins.
 */
public final class NameMC
{
	/**
	 * Creates a new NameMC class instance with the default settings.
	 *
	 * @return A new NameMC class instance with the default settings.
	 */
	public static NameMC ofDefault()
	{
		return new NameMC();
	}

	/**
	 * Creates a new NameeMC instance with a possibly custom profile and server service.
	 *
	 * @return A new NameMC instance with a possibly custom profile and server service.
	 *
	 * @throws NullPointerException If the given profile or server service is null.
	 */
	public static NameMC ofCustom(ProfileService profileService, ServerService serverService)
	{
		Objects.requireNonNull(profileService, "profileService");
		Objects.requireNonNull(serverService, "serverService");
		return new NameMC(profileService, serverService);
	}

	/**
	 * Currently used profile service.
	 */
	private final ProfileService profileService;
	/**
	 * Currently used server service.
	 */
	private final ServerService  serverService;

	/**
	 * Creates a new NameMC class instance with the default settings.
	 *
	 * @return A new NameMC class instance with the default settings.
	 */
	private NameMC()
	{
		this.profileService = ProfileService.ofDefault();
		this.serverService = ServerService.ofDefault();
	}

	/**
	 * Creates a new NameeMC instance with a possibly custom profile and server service.
	 *
	 * @return A new NameMC instance with a possibly custom profile and server service.
	 */
	private NameMC(ProfileService profileService, ServerService serverService)
	{
		this.profileService = profileService;
		this.serverService = serverService;
	}

	/**
	 * Returns the currently used profile service.
	 *
	 * @return The currently used profile service.
	 */
	public ProfileService getProfileService()
	{
		return this.profileService;
	}

	/**
	 * Returns the currently used server service.
	 *
	 * @return The currently used server service.
	 */
	public ServerService getServerService()
	{
		return this.serverService;
	}

	/**
	 * Clears all services cache.
	 */
	public void clearCaches()
	{
		this.profileService.clearProfiles();
		this.serverService.clearServers();
	}
}