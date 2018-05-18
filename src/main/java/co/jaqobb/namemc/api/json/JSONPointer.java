/*
 * Copyright (c) 2002 JSON.org
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
 * The Software shall be used for Good, not Evil.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package co.jaqobb.namemc.api.json;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A JSON Pointer is a simple query language defined for JSON documents by
 * <a href="https://tools.ietf.org/html/rfc6901">RFC 6901</a>.
 * In a nutshell, JSONPointer allows the user to navigate into a JSON document
 * using strings, and retrieve targeted objects, like a simple form of XPATH.
 * Path segments are separated by the '/' char, which signifies the root of
 * the document when it appears as the first char of the string. Array
 * elements are navigated using ordinals, counting from 0. JSONPointer strings
 * may be extended to any arbitrary number of segments. If the navigation
 * is successful, the matched item is returned. A matched item may be a
 * JSONObject, a JSONArray, or a JSON value. If the JSONPointer string building
 * fails, an appropriate exception is thrown. If the navigation fails to find
 * a match, a JSONPointerException is thrown.
 *
 * @author JSON.org
 * @version 2016-05-14
 */
public class JSONPointer
{
	// used for URL encoding and decoding
	private static final String ENCODING = "UTF-8";

	/**
	 * This class allows the user to build a JSONPointer in steps, using
	 * exactly one segment in each step.
	 */
	public static class Builder
	{
		// Segments for the eventual JSONPointer string
		private final List<String> tokens = new ArrayList<>(10);

		/**
		 * Creates a {@code JSONPointer} instance using the tokens previously set using the
		 * {@link #append(String)} method calls.
		 */
		public JSONPointer build()
		{
			return new JSONPointer(this.tokens);
		}

		/**
		 * Adds an arbitrary token to the list of reference tokens. It can be any non-null value.
		 * Unlike in the case of JSON string or URI fragment representation of JSON pointers, the
		 * argument of this method MUST NOT be escaped. If you want to query the property called
		 * {@code "a~b"} then you should simply pass the {@code "a~b"} string as-is, there is no
		 * need to escape it as {@code "a~0b"}.
		 *
		 * @param token the new token to be appended to the list.
		 *
		 * @return {@code this}.
		 *
		 * @throws NullPointerException if {@code token} is null.
		 */
		public Builder append(String token)
		{
			if (token == null)
			{
				throw new NullPointerException("Token cannot be null");
			}
			this.tokens.add(token);
			return this;
		}

		/**
		 * Adds an integer to the reference token list. Although not necessarily, mostly this token will
		 * denote an array index.
		 *
		 * @param arrayIndex the array index to be added to the token list.
		 *
		 * @return {@code this}.
		 */
		public Builder append(int arrayIndex)
		{
			this.tokens.add(String.valueOf(arrayIndex));
			return this;
		}
	}

	/**
	 * Static factory method for {@link Builder}. Example usage:
	 * <pre><code>
	 * JSONPointer pointer = JSONPointer.builder()
	 *       .append("obj")
	 *       .append("other~key").append("another/key")
	 *       .append("\"")
	 *       .append(0)
	 *       .build();
	 * </code></pre>
	 *
	 * @return a builder instance which can be used to construct a {@code JSONPointer} instance by chained.
	 * {@link Builder#append(String)} calls.
	 */
	public static Builder builder()
	{
		return new Builder();
	}

	// Segments for the JSONPointer string
	private final List<String> tokens;

	/**
	 * Pre-parses and initializes a new {@code JSONPointer} instance. If you want to
	 * evaluate the same JSON Pointer on different JSON documents then it is recommended
	 * to keep the {@code JSONPointer} instances due to performance considerations.
	 *
	 * @param pointer the JSON String or URI Fragment representation of the JSON pointer.
	 *
	 * @throws IllegalArgumentException if {@code pointer} is not a valid JSON pointer
	 */
	public JSONPointer(String pointer)
	{
		if (pointer == null)
		{
			throw new NullPointerException("Pointer cannot be null");
		}
		if (pointer.isEmpty() || pointer.equals("#"))
		{
			this.tokens = Collections.emptyList();
			return;
		}
		String tokens;
		if (pointer.startsWith("#/"))
		{
			tokens = pointer.substring(2);
			try
			{
				tokens = URLDecoder.decode(tokens, ENCODING);
			}
			catch (UnsupportedEncodingException exception)
			{
				throw new RuntimeException(exception);
			}
		}
		else if (pointer.startsWith("/"))
		{
			tokens = pointer.substring(1);
		}
		else
		{
			throw new IllegalArgumentException("a JSON pointer should start with '/' or '#/'");
		}
		this.tokens = new ArrayList<>(10);
		int slashIndex = - 1;
		int previousSlashIndex;
		do
		{
			previousSlashIndex = slashIndex + 1;
			slashIndex = tokens.indexOf('/', previousSlashIndex);
			if (previousSlashIndex == slashIndex || previousSlashIndex == tokens.length())
			{
				// found 2 slashes in a row ( obj//next )
				// or single slash at the end of a string ( obj/test/ )
				this.tokens.add("");
			}
			else if (slashIndex >= 0)
			{
				final String token = tokens.substring(previousSlashIndex, slashIndex);
				this.tokens.add(this.unescape(token));
			}
			else
			{
				// last item after separator, or no separator at all.
				final String token = tokens.substring(previousSlashIndex);
				this.tokens.add(this.unescape(token));
			}
		}
		while (slashIndex >= 0);
		// using split does not take into account consecutive separators or "ending nulls"
		//for (String token : refs.split("/")) {
		//    this.tokens.add(unescape(token));
		//}
	}

	public JSONPointer(List<String> tokens)
	{
		this.tokens = new ArrayList<>(tokens);
	}

	private String unescape(String token)
	{
		return token.replace("~1", "/").replace("~0", "~").replace("\\\"", "\"").replace("\\\\", "\\");
	}

	/**
	 * Evaluates this JSON Pointer on the given {@code document}. The {@code document}
	 * is usually a {@link JSONObject} or a {@link JSONArray} instance, but the empty
	 * JSON Pointer ({@code ""}) can be evaluated on any JSON values and in such case the
	 * returned value will be {@code document} itself.
	 *
	 * @param document the JSON document which should be the subject of querying.
	 *
	 * @return the result of the evaluation.
	 *
	 * @throws JSONPointerException if an error occurs during evaluation.
	 */
	public Object queryFrom(Object document) throws JSONPointerException
	{
		if (this.tokens.isEmpty())
		{
			return document;
		}
		Object current = document;
		for (String token : this.tokens)
		{
			if (current instanceof JSONObject)
			{
				current = ((JSONObject) current).opt(this.unescape(token));
			}
			else if (current instanceof JSONArray)
			{
				current = this.readByIndexToken(current, token);
			}
			else
			{
				throw new JSONPointerException("value [" + current + "] is not an array or object therefore its key " + token + " cannot be resolved");
			}
		}
		return current;
	}

	/**
	 * Matches a JSONArray element by ordinal position
	 *
	 * @param current    the JSONArray to be evaluated.
	 * @param indexToken the array index in string form.
	 *
	 * @return the matched object.
	 *
	 * @throws JSONPointerException if the index is out of bounds.
	 */
	private Object readByIndexToken(Object current, String indexToken) throws JSONPointerException
	{
		try
		{
			int index = Integer.parseInt(indexToken);
			JSONArray currentArray = (JSONArray) current;
			if (index >= currentArray.length())
			{
				throw new JSONPointerException("index " + index + " is out of bounds - the array has " + currentArray.length() + " elements");
			}
			try
			{
				return currentArray.get(index);
			}
			catch (JSONException exception)
			{
				throw new JSONPointerException("Error reading value at index position " + index, exception);
			}
		}
		catch (NumberFormatException exception)
		{
			throw new JSONPointerException(indexToken + " is not an array index", exception);
		}
	}

	/**
	 * Returns a string representing the JSONPointer path value using string
	 * representation
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (String token : this.tokens)
		{
			builder.append('/').append(this.escape(token));
		}
		return builder.toString();
	}

	/**
	 * Escapes path segment values to an unambiguous form.
	 * The escape char to be inserted is '~'. The chars to be escaped
	 * are ~, which maps to ~0, and /, which maps to ~1. Backslashes
	 * and double quote chars are also escaped.
	 *
	 * @param token the JSONPointer segment value to be escaped.
	 *
	 * @return the escaped value for the token.
	 */
	private String escape(String token)
	{
		return token.replace("~", "~0").replace("/", "~1").replace("\\", "\\\\").replace("\"", "\\\"");
	}

	/**
	 * Returns a string representing the JSONPointer path value using URI
	 * fragment identifier representation
	 */
	public String toURIFragment()
	{
		try
		{
			StringBuilder value = new StringBuilder("#");
			for (String token : this.tokens)
			{
				value.append('/').append(URLEncoder.encode(token, ENCODING));
			}
			return value.toString();
		}
		catch (UnsupportedEncodingException exception)
		{
			throw new RuntimeException(exception);
		}
	}
}