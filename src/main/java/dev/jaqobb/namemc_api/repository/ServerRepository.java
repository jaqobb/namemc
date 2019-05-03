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

package dev.jaqobb.namemc_api.repository;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import dev.jaqobb.namemc_api.data.Server;
import dev.jaqobb.namemc_api.util.IOHelper;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

public class ServerRepository {

	private static final String SERVER_VOTES_URL = "https://api.namemc.com/server/%s/votes";

	private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
	private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMCAPI Server Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

	@NotNull
	private Duration cacheDuration;
	@NotNull
	private Map<String, Server> servers = Collections.synchronizedMap(new HashMap<>(1, 1.0F));

	public ServerRepository() {
		this(10, ChronoUnit.MINUTES);
	}

	public ServerRepository(long duration, @NotNull TemporalUnit unit) {
		if(duration < 1) {
			throw new IllegalArgumentException("duration cannot be smaller than 1");
		}
		this.cacheDuration = Duration.of(duration, unit);
	}

	@NotNull
	public Duration getCacheDuration() {
		return this.cacheDuration;
	}

	@NotNull
	public Collection<Server> getServers() {
		return Collections.unmodifiableCollection(this.servers.values());
	}

	@NotNull
	public Collection<Server> getValidServers() {
		return this.servers.values().stream()
			.filter(this::isServerValid)
			.collect(Collectors.toUnmodifiableList());
	}

	@NotNull
	public Collection<Server> getInvalidServers() {
		return this.servers.values().stream()
			.filter(server -> !this.isServerValid(server))
			.collect(Collectors.toUnmodifiableList());
	}

	public void addServer(@NotNull Server server) {
		this.servers.putIfAbsent(server.getAddress().toLowerCase(), server);
	}

	public void removeServer(@NotNull Server server) {
		this.servers.remove(server.getAddress().toLowerCase());
	}

	public void cacheServer(@NotNull String address, boolean recache, @NotNull BiConsumer<Server, Throwable> callback) {
		if(this.servers.containsKey(address.toLowerCase())) {
			Server server = this.servers.get(address.toLowerCase());
			if(this.isServerValid(server) && !recache) {
				callback.accept(server, null);
				return;
			}
		}
		EXECUTOR.execute(() -> {
			String url = String.format(SERVER_VOTES_URL, address.toLowerCase());
			try {
				JSONArray array = new JSONArray(IOHelper.getWebsiteContent(url));
				Collection<UUID> likes = IntStream.range(0, array.length())
					.boxed()
					.map(index -> UUID.fromString(array.getString(index)))
					.collect(Collectors.toUnmodifiableList());
				Server server = new Server(address.toLowerCase(), likes);
				this.servers.put(address.toLowerCase(), server);
				callback.accept(server, null);
			} catch(IOException | JSONException exception) {
				callback.accept(null, exception);
			}
		});
	}

	public boolean isServerValid(@NotNull Server server) {
		Duration difference = Duration.between(server.getCacheTime(), Instant.now());
		return difference.compareTo(this.cacheDuration) < 0;
	}

	public void clearServers() {
		this.servers.clear();
	}
}
