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

package dev.jaqobb.namemc_api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

public final class IOHelper {

	private IOHelper() {
		throw new UnsupportedOperationException("Cannot create instance of utility class");
	}

	public static String getReaderContent(@NotNull BufferedReader reader) throws IOException {
		String content = "";
		String line;
		while((line = reader.readLine()) != null) {
			content += line;
			content += System.lineSeparator();
		}
		return content;
	}

	public static String getWebsiteContent(@NotNull String websiteUrl) throws IOException {
		if(websiteUrl.isBlank()) {
			throw new IllegalArgumentException("websiteUrl cannot be blank");
		}
		try(InputStream inputStream = new URL(websiteUrl).openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			return getReaderContent(reader);
		}
	}
}
