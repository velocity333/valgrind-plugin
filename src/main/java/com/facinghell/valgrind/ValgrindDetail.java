package com.facinghell.valgrind;

import hudson.model.AbstractBuild;

import com.facinghell.valgrind.model.ValgrindError;
import com.facinghell.valgrind.model.ValgrindStacktraceFrame;
import com.facinghell.valgrind.util.ValgrindSourceFile;

public class ValgrindDetail
{
	private ValgrindError error;
	final private AbstractBuild<?, ?> owner;
	
	ValgrindDetail( AbstractBuild<?, ?> owner, ValgrindError error, ValgrindSourceFile valgrindSourceFile )
	{
		this.owner = owner;
		this.error = error;		
		
		if ( error != null && error.getStacktrace() != null && error.getStacktrace().getFrames() != null )
		{
			for ( ValgrindStacktraceFrame frame : error.getStacktrace().getFrames() )
			{
				if ( frame == null )
					continue;					

				frame.setSourceCode( valgrindSourceFile.getSnippet( frame.getFilePath(), frame.getLineNumber() ) );
			}
		}
	}

	public ValgrindError getError()
	{
		return error;
	}

	public void setError(ValgrindError error)
	{
		this.error = error;
	}

	public AbstractBuild<?, ?> getOwner()
	{
		return owner;
	}
}