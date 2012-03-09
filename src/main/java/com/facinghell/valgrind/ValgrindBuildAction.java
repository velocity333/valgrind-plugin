package com.facinghell.valgrind;

import hudson.model.HealthReport;
import hudson.model.AbstractBuild;

import java.io.IOException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;


import com.facinghell.valgrind.config.ValgrindPublisherConfig;
import com.facinghell.valgrind.util.AbstractValgrindBuildAction;

public class ValgrindBuildAction extends AbstractValgrindBuildAction
{
	private ValgrindResult result;
	private ValgrindPublisherConfig config;
	
	public ValgrindBuildAction( AbstractBuild<?, ?> owner, ValgrindResult result, ValgrindPublisherConfig config )
	{
		super(owner);
		this.result = result;
		this.config = config;
	}
	
	public ValgrindResult getResult()
	{
		return result;
	}
	
	public ValgrindPublisherConfig getConfig()
	{
		return config;
	}
	
	public String getSearchUrl()
	{
		return getUrlName();
	}

	public Object getTarget()
	{
		return result;
	}

	public HealthReport getBuildHealth()
	{
		return new HealthReport();
	}

	public String getIconFileName()
	{
		return "/plugin/cppcheck/icons/cppcheck-24.png";
	}

	public String getDisplayName()
	{
		return "Valgrind Result";
	}

	public String getUrlName()
	{
		return "valgrindResult";
	}

	@Override
	public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException
	{
		// TODO Auto-generated method stub
		
	}

}
