/*
 * This file is a part of namemc-api, licensed under the MIT License.
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
package co.jaqobb.namemc_api.repository;

import co.jaqobb.namemc_api.data.Server;
import co.jaqobb.namemc_api.util.IOUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class ServerRepository {
	private static final String SERVER_VOTES_URL = "https://api.namemc.com/server/%s/votes";

	private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
	private static final Executor EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMCAPI Server Query #" + ServerRepository.EXECUTOR_THREAD_COUNTER.getAndIncrement()));

	private final long duration;
	private final TimeUnit unit;
	private final Map<String, Server> servers;

	public ServerRepository() {
		this(10L, TimeUnit.SECONDS);
	}

	public ServerRepository(long duration, TimeUnit unit) {
		if (duration < 1) {
			throw new IllegalArgumentException("duration cannot be lower than 1");
		}
		if (unit == null) {
			throw new NullPointerException("unit cannot be null");
		}
		this.duration = duration;
		this.unit = unit;
		this.servers = Collections.synchronizedMap(new HashMap<>(1, 1.0F));
	}

	public long getDuration() {
		return this.duration;
	}

	public long getDurationMillis() {
		return this.unit.toMillis(this.duration);
	}

	public TimeUnit getUnit() {
		return this.unit;
	}

	public Collection<Server> getServers() {
		return Collections.unmodifiableCollection(this.servers.values());
	}

	public Collection<Server> getValidServers() {
		return this.servers.values().stream().filter(this::isServerValid).collect(Collectors.toUnmodifiableList());
	}

	public Collection<Server> getInvalidServers() {
		return this.servers.values().stream().filter(server -> !this.isServerValid(server)).collect(Collectors.toUnmodifiableList());
	}

	public void cacheServer(String address, boolean recache, BiConsumer<Server, Throwable> callback) {
		if (address == null) {
			throw new NullPointerException("address cannot be null");
		}
		if (callback == null) {
			throw new NullPointerException("callback cannot be null");
		}
		if (this.servers.containsKey(address.toLowerCase())) {
			Server server = this.servers.get(address.toLowerCase());
			if (this.isServerValid(server) && !recache) {
				callback.accept(server, null);
				return;
			}
		}
		ServerRepository.EXECUTOR.execute(() -> {
			String url = String.format(ServerRepository.SERVER_VOTES_URL, address.toLowerCase());
			try {
				Server server = new Server(address.toLowerCase(), new JSONArray(IOUtils.getWebsiteContent(url)));
				this.servers.put(address.toLowerCase(), server);
				callback.accept(server, null);
			} catch (IOException exception) {
				callback.accept(null, exception);
			}
		});
	}

	public boolean isServerValid(Server server) {
		if (server == null) {
			throw new NullPointerException("server cannot be null");
		}
		return System.currentTimeMillis() - server.getCacheTime() < this.getDurationMillis();
	}

	public void clearServers() {
		this.servers.clear();
	}
}