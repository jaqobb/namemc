package co.jaqobb.namemc.api;

import java.util.Objects;

import co.jaqobb.namemc.api.profile.ProfileService;
import co.jaqobb.namemc.api.server.ServerService;

/**
 * Class where the whole API begins.
 */
public final class NameMC {
	/**
	 * Creates a new NameMC class instance with the default settings.
	 *
	 * @return A new NameMC class instance with the default settings.
	 */
	public static NameMC of() {
		return new NameMC();
	}

	/**
	 * Creates a new NameeMC instance with a possibly custom profile and server service.
	 *
	 * @return A new NameMC instance with a possibly custom profile and server service.
	 *
	 * @throws NullPointerException If the given profile or server service is null.
	 */
	public static NameMC of(ProfileService profileService, ServerService serverService) {
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
	private final ServerService serverService;

	/**
	 * Creates a new NameMC class instance with the default settings.
	 */
	private NameMC() {
		this.profileService = ProfileService.of();
		this.serverService = ServerService.of();
	}

	/**
	 * Creates a new NameeMC instance with a possibly custom profile and server service.
	 */
	private NameMC(ProfileService profileService, ServerService serverService) {
		this.profileService = profileService;
		this.serverService = serverService;
	}

	/**
	 * Returns the currently used profile service.
	 *
	 * @return The currently used profile service.
	 */
	public ProfileService getProfileService() {
		return this.profileService;
	}

	/**
	 * Returns the currently used server service.
	 *
	 * @return The currently used server service.
	 */
	public ServerService getServerService() {
		return this.serverService;
	}

	/**
	 * Clears all services cache.
	 */
	public void clearCaches() {
		this.profileService.clearProfiles();
		this.serverService.clearServers();
	}
}