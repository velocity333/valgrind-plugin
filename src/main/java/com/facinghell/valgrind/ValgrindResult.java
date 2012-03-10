package com.facinghell.valgrind;

import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.Serializable;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.facinghell.valgrind.model.ValgrindError;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.util.ValgrindSummary;

public class ValgrindResult implements Serializable
{	
	private static final long serialVersionUID = -5347879997716170059L;
	
	private ValgrindReport report;
    private AbstractBuild<?, ?> owner;

    public ValgrindResult( AbstractBuild<?, ?> build, ValgrindReport report )
    {
    	this.owner = build;
    	this.report = report; 
    }
    
	public AbstractBuild<?, ?> getOwner()
	{
		return owner;
	}

	public ValgrindReport getReport()
	{
		return report;
	}

	public void setReport(ValgrindReport report)
	{
		this.report = report;
	}

	/**
	 * Renders the summary Valgrind report for the build result.
	 * 
	 * @return the HTML fragment of the summary Valgrind report
	 */
	public String getSummary()
	{
		return ValgrindSummary.createReportSummary(this);
	}

	/**
	 * Renders the detailed summary Valgrind report for the build result.
	 * 
	 * @return the HTML fragment of the summary Valgrind report
	 */
	public String getDetails()
	{
		return ValgrindSummary.createReportSummaryDetails(this);
	}
	
	public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response)
			throws IOException
	{
		System.err.println("getDynamic called, link: " + link);
		
		if ( !link.startsWith("id=") )
			return null;

		String id = link.substring(3);
		
		System.err.println("id: " + id);
		
		ValgrindError error = report.findErrorById(id);
		if ( error == null )
			return null;
		
		ValgrindDetail detail = new ValgrindDetail( owner, error.getStacktrace() );
		return detail;
	}

}
