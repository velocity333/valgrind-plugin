package com.facinghell.valgrind;

import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.Actionable;
import hudson.model.HealthReportingAction;

import org.kohsuke.stapler.StaplerProxy;

import com.facinghell.valgrind.config.ValgrindPublisherConfig;

public class ValgrindBuildAction extends Actionable implements Action, HealthReportingAction, StaplerProxy
{
	private ValgrindResult result;
	private ValgrindPublisherConfig config;
	
	public ValgrindBuildAction( ValgrindResult result, ValgrindPublisherConfig config )
	{
		this.result = result;
		this.config = config;
	}
	
	public ValgrindResult getResult()
	{
		return result;
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

}
