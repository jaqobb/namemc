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

import co.jaqobb.namemc.api.profile.ProfileService;
import co.jaqobb.namemc.api.server.ServerService;

/**
 * Class where the fun
 * with the API begins.
 */
public class NameMC
{
	/**
	 * Creates new {@code NameMC} instance
	 * with the default settings.
	 *
	 * @return new {@code NameMC} instance
	 * with the default settings.
	 */
	public static NameMC newDefault()
	{
		return new NameMC();
	}

	/**
	 * Creates new {@code NameMC} instance with the
	 * ability to customize {@code ProfileService}
	 * and {@code ServerService}.
	 *
	 * @return new {@code NameMC} instance with the
	 * possibly custom {@code ProfileService} and
	 * {@code ServerService}.
	 *
	 * @throws NullPointerException if the {@code profileServer}
	 *                              or the {@code serverService}
	 *                              is null.
	 */
	public static NameMC newCustom(ProfileService profileService, ServerService serverService)
	{
		if (profileService == null)
		{
			throw new NullPointerException("Profile service cannot be null");
		}
		if (serverService == null)
		{
			throw new NullPointerException("Server service cannot be null");
		}
		return new NameMC(profileService, serverService);
	}

	/**
	 * Currently used {@code ProfileService}.
	 */
	private final ProfileService profileService;
	/**
	 * Currently used {@code ServerService}.
	 */
	private final ServerService  serverService;

	/**
	 * Creates new {@code NameMC} instance
	 * with the default settings.
	 *
	 * @return new {@code NameMC} instance
	 * with the default settings.
	 */
	private NameMC()
	{
		this.profileService = ProfileService.newDefault();
		this.serverService = ServerService.newDefault();
	}

	/**
	 * Creates new {@code NameMC} instance with the
	 * ability to customize {@code ProfileService}
	 * and {@code ServerService}.
	 *
	 * @return new {@code NameMC} instance with the
	 * possibly custom {@code ProfileService} and
	 * {@code ServerService}.
	 */
	private NameMC(ProfileService profileService, ServerService serverService)
	{
		this.profileService = profileService;
		this.serverService = serverService;
	}

	/**
	 * Returns currently used {@code ProfileService}.
	 *
	 * @return currently used {@code ProfileService}.
	 */
	public ProfileService getProfileService()
	{
		return this.profileService;
	}

	/**
	 * Returns currently used {@code ServerService}.
	 *
	 * @return currently used {@code ServerService}.
	 */
	public ServerService getServerService()
	{
		return this.serverService;
	}

	/**
	 * Clears the {@code profileService} and
	 * the {@code serverService} cache.
	 */
	public void clearCaches()
	{
		this.profileService.clearProfiles();
		this.serverService.clearServers();
	}
}