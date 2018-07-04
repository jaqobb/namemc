/*
 * This file is a part of namemc-api, licensed under the MIT License.
 *
 * Copyright (c) jaqobb (Jakub Zagórski) <jaqobb@jaqobb.co>
 * Copyright (c) contributors
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

package co.jaqobb.namemc.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Collection of useful methods related to the I/O.
 */
public final class IOUtils {
	/**
	 * Private constructor to make sure no one will initialize this class.
	 */
	private IOUtils() {
	}

	/**
	 * Returns a content of the given url.
	 *
	 * @param url A url.
	 *
	 * @return A content of the given url.
	 *
	 * @throws IOException If the error occured while trying to read url content.
	 */
	public static String getWebsiteContent(String url) throws IOException {
		try (InputStream inputStream = new URL(url).openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			return IOUtils.getContent(reader);
		}
	}

	/**
	 * Returns a content of the given reader.
	 *
	 * @param reader A reader.
	 *
	 * @return A content of the given reader.
	 *
	 * @throws IOException If the error occured while trying to read reader content.
	 */
	public static String getContent(Reader reader) throws IOException {
		StringBuilder content = new StringBuilder();
		int character;
		while ((character = reader.read()) != -1) {
			content.append((char) character);
		}
		return content.toString();
	}
}