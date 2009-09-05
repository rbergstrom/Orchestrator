/*
 *  The MIT License
 * 
 *  Copyright 2009 Ryan Bergstrom.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.orchestrator.client.util;

public class Logging {
	public static enum LogLevel {
		DEBUG,
		INFO,
		WARNING,
		ERROR,
	}
	protected static LogLevel selectedLevel = LogLevel.WARNING;
	
	public static void setLogLevel(LogLevel newLevel) {
		selectedLevel = newLevel;
	}
	
	public static LogLevel getLogLevel() {
		return selectedLevel;
	}
	
	protected static void Log(LogLevel level, String msg, Object... args) {
		if (level.ordinal() >= selectedLevel.ordinal()) {
			System.err.println(String.format(msg, args));
		}
	}
	
	public static void Debug(String msg, Object... args) {
		Log(LogLevel.DEBUG, msg, args);
	}
	
	public static void Info(String msg, Object... args) {
		Log(LogLevel.INFO, msg, args);
	}
	
	public static void Warning(String msg, Object... args) {
		Log(LogLevel.WARNING, msg, args);
	}
	
	public static void Error(String msg, Object... args) {
		Log(LogLevel.ERROR, msg, args);
	}
}
