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

package co.jaqobb.namemc.api.server;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import co.jaqobb.namemc.api.json.JSONArray;
import co.jaqobb.namemc.api.util.IOUtils;

/**
 * Class used to store cached {@code Server},
 * and to cache new ones.
 */
public class ServerService
{
	/**
	 * Creates new {@code ServerService} instance
	 * with the default values being 10 as a time
	 * and minutes, as a time unit.
	 */
	public static ServerService newDefault()
	{
		return new ServerService();
	}

	/**
	 * Creates new {@code ServerService} instance
	 * with the given time and time unit.
	 *
	 * @param time a time.
	 * @param unit a time unit.
	 */
	public static ServerService newCustom(long time, TimeUnit unit)
	{
		return new ServerService(time, unit);
	}

	/**
	 * Url used to cache {@code Server}.
	 */
	private static final String SERVER_VOTES_URL = "https://api.namemc.com/server/%s/votes";

	/**
	 * Counter used to determine thread number.
	 */
	private static final AtomicInteger EXECUTOR_THREAD_COUNTER = new AtomicInteger();
	/**
	 * Executor used to cache {@code Server}.
	 */
	private static final Executor      EXECUTOR                = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "NameMC API Server Query #" + EXECUTOR_THREAD_COUNTER.getAndIncrement()));

	/**
	 * Time that indicates whenever {@code Server} should be recached.
	 */
	private final long                time;
	/**
	 * Time unit used to describe a unit of {@code time}.
	 */
	private final TimeUnit            unit;
	/**
	 * Collection of currently cached {@code Server}s.
	 */
	private final Map<String, Server> servers = Collections.synchronizedMap(new WeakHashMap<>(100));

	/**
	 * Creates new {@code ServerService} instance
	 * with the default values being 10 as a time
	 * and minutes, as a time unit.
	 */
	private ServerService()
	{
		this(10, TimeUnit.MINUTES);
	}

	/**
	 * Creates new {@code ServerService} instance
	 * with the given time and time unit.
	 *
	 * @param time a time.
	 * @param unit a time unit.
	 */
	private ServerService(long time, TimeUnit unit)
	{
		this.time = time;
		this.unit = unit;
	}

	/**
	 * Returns time that indicates whenever {@code Server} should be recached.
	 *
	 * @return time that indicates whenever {@code Server} should be recached.
	 */
	public long getTime()
	{
		return this.time;
	}

	/**
	 * Returns unit of the tracked {@code time}.
	 *
	 * @return unit of the tracked {@code} time.
	 */
	public TimeUnit getUnit()
	{
		return this.unit;
	}

	/**
	 * Returns time in milliseconds that indicates
	 * whenever {@code Server} should be recached
	 *
	 * @return time in milliseconds that indicated
	 * whenever {@code Server} should be recached
	 */
	public long getTimeInMillis()
	{
		return TimeUnit.MILLISECONDS.convert(this.time, this.unit);
	}

	/**
	 * Returns an immutable collection
	 * of currently cached {@code Server}s.
	 *
	 * @return an immutable collection of currently cached {@code Server}s.
	 */
	public Collection<Server> getServers()
	{
		return Collections.unmodifiableCollection(this.servers.values());
	}

	/**
	 * Delegates cached {@code Server} or
	 * caches new {@code Server} with the
	 * given ip and then delegates
	 * it to the {@code callback}.
	 *
	 * @param ip       an ip to cache (case insensitive).
	 * @param recache  a state which defines
	 *                 if the recache should
	 *                 be forced.
	 * @param callback a callback where cached
	 *                 {@code Server} and exception
	 *                 (null if everything went good)
	 *                 will be delegated to.
	 *
	 * @throws NullPointerException if the {@code ip} or the {@code callback} is null.
	 */
	public void getServer(String ip, boolean recache, BiConsumer<Server, Exception> callback)
	{
		Objects.requireNonNull(ip, "ip");
		Objects.requireNonNull(callback, "callback");
		synchronized (this.servers)
		{
			Server server = this.servers.get(ip.toLowerCase());
			if (this.isServerValid(server) && !recache)
			{
				callback.accept(server, null);
			}
		}
		EXECUTOR.execute(() ->
		{
			String url = String.format(SERVER_VOTES_URL, ip);
			try
			{
				String content = IOUtils.getWebsiteContent(url);
				JSONArray array = new JSONArray(content);
				Server server = new Server(ip, array);
				this.servers.put(ip.toLowerCase(), server);
				callback.accept(server, null);
			}
			catch (IOException exception)
			{
				callback.accept(null, exception);
			}
		});
	}

	/**
	 * Returns {@code true} if the given {@code server} is
	 * not {@code null} and does not need to be recached,
	 * {@code false} otherwise.
	 *
	 * @param server a {@code Server} to check.
	 *
	 * @return {@code true} if the given {@code server} is
	 * not {@code null} and does not need to be recached,
	 * {@code false} otherwise.
	 */
	public boolean isServerValid(Server server)
	{
		return server != null && System.currentTimeMillis() - server.getCacheTime() < this.getTimeInMillis();
	}

	/**
	 * Clears {@code Server}s cache.
	 */
	public void clearServers()
	{
		synchronized (this.servers)
		{
			this.servers.clear();
		}
	}
}