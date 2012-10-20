package org.jenkinsci.plugins.valgrind.util;

public interface ValgrindOption
{
	public String getName();
    public String getArgumentString(ValgrindVersion version) throws ValgrindOptionNotApplicableException;
}
