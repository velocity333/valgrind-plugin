package org.jenkinsci.plugins.valgrind.util;

import hudson.model.BuildListener;

public abstract class ValgrindLogger
{
	public static void log(BuildListener listener, final String message)
	{
		listener.getLogger().println("[Valgrind] " + message);
	}
}
