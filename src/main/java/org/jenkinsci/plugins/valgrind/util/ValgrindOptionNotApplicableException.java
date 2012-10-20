package org.jenkinsci.plugins.valgrind.util;

@SuppressWarnings("serial")
public class ValgrindOptionNotApplicableException extends Exception
{
	public ValgrindOptionNotApplicableException(String message)
	{
		super(message);
	}
}
