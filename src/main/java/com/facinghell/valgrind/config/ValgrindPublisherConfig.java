package com.facinghell.valgrind.config;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

public class ValgrindPublisherConfig implements Serializable
{
	private static final long serialVersionUID = 1335068144678253494L;
	
	private String pattern;
	private boolean invalidReads;
	private boolean invalidWrites;
	
	@DataBoundConstructor
	public ValgrindPublisherConfig(String pattern, boolean invalidReads, boolean invalidWrites)
	{
		this.pattern = pattern;
		this.invalidReads = invalidReads;
		this.invalidWrites = invalidWrites;		
	}

	public String getPattern()
	{
		return pattern;
	}

	public boolean isInvalidReads()
	{
		return invalidReads;
	}

	public boolean isInvalidWrites()
	{
		return invalidWrites;
	}

}
