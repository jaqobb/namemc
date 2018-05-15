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

package co.jaqobb.namemc.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Collection of useful methods
 * used in this project.
 */
public final class IOUtils {
	/**
	 * Useless constructor, just to make sure
	 * no one will initialize this class.
	 */
	private IOUtils() {
	}

	/**
	 * Returns a content of the given {@code url}.
	 *
	 * @param url a url;
	 *
	 * @return a content of the given {@code url}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static String getWebsiteContent(String url) throws IOException {
		try (InputStream inputStream = new URL(url).openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")))) {
			return IOUtils.getContent(reader);
		}
	}

	/**
	 * Returns a content of the given {@code reader}.
	 *
	 * @param reader a reader.
	 *
	 * @return a content of the given {@code reader}.
	 *
	 * @throws IOException if an I/O error occurs.
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