package org.jenkinsci.plugins.valgrind;

import hudson.model.AbstractBuild;

import org.jenkinsci.plugins.valgrind.model.ValgrindError;
import org.jenkinsci.plugins.valgrind.util.ValgrindSourceFile;

/**
 * 
 * @author Johannes Ohlemacher
 * 
 */
public class ValgrindDetail
{
	private ValgrindError error;
	final private AbstractBuild<?, ?> owner;
	
	public ValgrindDetail( AbstractBuild<?, ?> owner, ValgrindError error, ValgrindSourceFile valgrindSourceFile )
	{
		this.owner = owner;
		this.error = error;	
		
		if ( error != null )
			error.setSourceCode( valgrindSourceFile );
	}

	public ValgrindError getError()
	{
		return error;
	}

	public AbstractBuild<?, ?> getOwner()
	{
		return owner;
	}
}