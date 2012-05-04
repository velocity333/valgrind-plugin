package org.jenkinsci.plugins.valgrind.config;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

public class ValgrindPublisherConfig implements Serializable
{
	private static final long serialVersionUID = 1335068144678253494L;
	
	private String pattern;
	private String failThresholdInvalidReadWrite;
	private String failThresholdDefinitelyLost;
	private String failThresholdTotal;
	private String unstableThresholdInvalidReadWrite;
	private String unstableThresholdDefinitelyLost;
	private String unstableThresholdTotal;	
	
	@DataBoundConstructor
	public ValgrindPublisherConfig( String pattern, 
			String failThresholdInvalidReadWrite, 
			String failThresholdDefinitelyLost, 
			String failThresholdTotal,
			String unstableThresholdInvalidReadWrite, 
			String unstableThresholdDefinitelyLost, 
			String unstableThresholdTotal)
	{
		this.pattern = pattern.trim();
		this.failThresholdInvalidReadWrite = failThresholdInvalidReadWrite.trim();
		this.failThresholdDefinitelyLost = failThresholdDefinitelyLost.trim();
		this.failThresholdTotal = failThresholdTotal.trim();		
		this.unstableThresholdInvalidReadWrite = unstableThresholdInvalidReadWrite.trim();
		this.unstableThresholdDefinitelyLost = unstableThresholdDefinitelyLost.trim();
		this.unstableThresholdTotal = unstableThresholdTotal.trim();		
	}

	public String getPattern()
	{
		return pattern;
	}

	public String getFailThresholdInvalidReadWrite()
	{
		return failThresholdInvalidReadWrite;
	}

	public String getFailThresholdDefinitelyLost()
	{
		return failThresholdDefinitelyLost;
	}

	public String getFailThresholdTotal()
	{
		return failThresholdTotal;
	}

	public String getUnstableThresholdInvalidReadWrite()
	{
		return unstableThresholdInvalidReadWrite;
	}

	public String getUnstableThresholdDefinitelyLost()
	{
		return unstableThresholdDefinitelyLost;
	}

	public String getUnstableThresholdTotal()
	{
		return unstableThresholdTotal;
	}

}
