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

package dev.jaqobb.namemcapi;

import dev.jaqobb.namemcapi.repository.ProfileRepository;
import dev.jaqobb.namemcapi.repository.ServerRepository;
import org.jetbrains.annotations.NotNull;

public class NameMCAPI {

	@NotNull
	private final ProfileRepository profileRepository;
	@NotNull
	private final ServerRepository serverRepository;

	public NameMCAPI() {
		this(new ProfileRepository(), new ServerRepository());
	}

	public NameMCAPI(@NotNull ProfileRepository profileRepository, @NotNull ServerRepository serverRepository) {
		this.profileRepository = profileRepository;
		this.serverRepository = serverRepository;
	}

	@NotNull
	public ProfileRepository getProfileRepository() {
		return this.profileRepository;
	}

	@NotNull
	public ServerRepository getServerRepository() {
		return this.serverRepository;
	}
}
