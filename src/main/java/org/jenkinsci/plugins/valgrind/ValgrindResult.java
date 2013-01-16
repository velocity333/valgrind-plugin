package org.jenkinsci.plugins.valgrind;

import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.jenkinsci.plugins.valgrind.config.ValgrindPublisherConfig;
import org.jenkinsci.plugins.valgrind.model.ValgrindError;
import org.jenkinsci.plugins.valgrind.model.ValgrindReport;
import org.jenkinsci.plugins.valgrind.util.ValgrindSourceFile;
import org.jenkinsci.plugins.valgrind.util.ValgrindSummary;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;


public class ValgrindResult implements Serializable
{	
	private static final long serialVersionUID = -5347879997716170059L;
	
	private ValgrindReport report;
    private AbstractBuild<?, ?> owner;
    private Map<String, String> sourceFiles;
     

    public ValgrindResult( AbstractBuild<?, ?> build, ValgrindReport report )
    {
    	this.owner = build;
    	this.report = report;
    }
    
	public AbstractBuild<?, ?> getOwner()
	{
		return owner;
	}
	
	public ValgrindPublisherConfig getConfig()
	{
		ValgrindBuildAction action = (ValgrindBuildAction)owner.getAction(ValgrindBuildAction.class);
    	if ( action == null )
    		return null;
    	
		return action.getConfig();    			
	}

	public ValgrindReport getReport()
	{
		return report;		
	}

	public void setReport(ValgrindReport report)
	{
		this.report = report;
	}
	
	public Map<String, String> getSourceFiles()
	{
		return sourceFiles;
	}
	
	public void setSourceFiles(Map<String, String> sourceFiles)
	{
		this.sourceFiles = sourceFiles;
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
	 * 
	 * @param link expected to be in format "id=<executable name>,<unique error id>"
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response)
			throws IOException
	{    	
		if ( !link.startsWith("pid=") )
			return null;
		
		int sep = link.indexOf(",");		
		if ( sep < 4 )
			return null;

		String pid = link.substring(4, sep);
		String uniqueId = link.substring( sep + 1 );

		ValgrindError error = report.findError(pid, uniqueId);
		if ( error == null )
			return null;		

		ValgrindSourceFile sourceFile = new ValgrindSourceFile( ValgrindPublisher.DESCRIPTOR.getLinesBefore(), ValgrindPublisher.DESCRIPTOR.getLinesAfter(), sourceFiles, owner );
 
		return new ValgrindDetail( owner, error, sourceFile );
	}

}
