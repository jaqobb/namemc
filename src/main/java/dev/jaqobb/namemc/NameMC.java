package dev.jaqobb.namemc;

import dev.jaqobb.namemc.profile.ProfileRepository;
import dev.jaqobb.namemc.server.ServerRepository;

public class NameMC {
    
    private final ProfileRepository profileRepository;
    private final ServerRepository serverRepository;
    
    public NameMC() {
        this(new ProfileRepository(), new ServerRepository());
    }
    
    public NameMC(ProfileRepository profileRepository) {
        this(profileRepository, new ServerRepository());
    }
    
    public NameMC(ServerRepository serverRepository) {
        this(new ProfileRepository(), serverRepository);
    }
    
    public NameMC(ProfileRepository profileRepository, ServerRepository serverRepository) {
        if (profileRepository == null) {
            throw new NullPointerException("profileRepository");
        }
        if (serverRepository == null) {
            throw new NullPointerException("serverRepository");
        }
        this.profileRepository = profileRepository;
        this.serverRepository = serverRepository;
    }
    
    public ProfileRepository getProfileRepository() {
        return this.profileRepository;
    }
    
    public ServerRepository getServerRepository() {
        return this.serverRepository;
    }
}
