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

package co.jaqobb.namemc.api.json;

/**
 * The JSONException is thrown by the JSON.org classes when things are amiss.
 *
 * @author JSON.org
 * @version 2015-12-09
 */
public class JSONException extends RuntimeException {
	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = 0;

	/**
	 * Constructs a JSONException with an explanatory message.
	 *
	 * @param message detail about the reason for the exception.
	 */
	public JSONException(String message) {
		super(message);
	}

	/**
	 * Constructs a JSONException with an explanatory message and cause.
	 *
	 * @param message detail about the reason for the exception.
	 * @param cause   the cause.
	 */
	public JSONException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new JSONException with the specified cause.
	 *
	 * @param cause the cause.
	 */
	public JSONException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
}