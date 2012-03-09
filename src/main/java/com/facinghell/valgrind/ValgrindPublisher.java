package com.facinghell.valgrind;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import com.facinghell.valgrind.config.ValgrindPublisherConfig;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.parser.ValgrindParserResult;
import com.facinghell.valgrind.util.ValgrindLogger;

/**
 * 
 * @author Johannes Ohlemacher
 *
 */
public class ValgrindPublisher extends Recorder
{
	private ValgrindPublisherConfig valgrindPublisherConfig;
	
	@Override
	public ValgrindPublisherDescriptor getDescriptor()
	{
		return DESCRIPTOR;
	}

	public BuildStepMonitor getRequiredMonitorService()
	{
		return BuildStepMonitor.BUILD;
	}
	
    protected boolean canContinue(final Result result) 
    {
        return result != Result.ABORTED && result != Result.FAILURE;
    }

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException
	{
		if ( !canContinue(build.getResult()) )
			return true;		
			
		ValgrindLogger.log(listener, "Analysing valgrind results");

		
		ValgrindParserResult parser = new ValgrindParserResult(listener, valgrindPublisherConfig.getPattern());
		ValgrindReport valgrindReport;
		
		valgrindReport = build.getWorkspace().act(parser);
		
		valgrindReport.print();
		
		ValgrindResult valgrindResult = new ValgrindResult( build, valgrindReport );
		
        ValgrindBuildAction buildAction = new ValgrindBuildAction(build, valgrindResult, valgrindPublisherConfig);
        build.addAction(buildAction);

        ValgrindLogger.log(listener, "Ending the valgrind analysis.");
		
		return true;
	}
	
	public ValgrindPublisherConfig getValgrindPublisherConfig()
	{
		return valgrindPublisherConfig;
	}
	
	public void setValgrindPublisherConfig( ValgrindPublisherConfig valgrindPublisherConfig )
	{
		this.valgrindPublisherConfig = valgrindPublisherConfig;
	}

	@Extension
	public static final ValgrindPublisherDescriptor DESCRIPTOR = new ValgrindPublisherDescriptor();

	private static final class ValgrindPublisherDescriptor extends BuildStepDescriptor<Publisher>
	{
		public ValgrindPublisherDescriptor()
		{
			super(ValgrindPublisher.class);
			load();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType)
		{
			return FreeStyleProject.class.isAssignableFrom(jobType)
					|| MatrixProject.class.isAssignableFrom(jobType);
		}

		@Override
		public String getDisplayName()
		{
			return "Publish Valgrind results";
		}

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData)
                throws hudson.model.Descriptor.FormException {

            ValgrindPublisher valgrindPublisher = new ValgrindPublisher();
            ValgrindPublisherConfig valgrindPublisherConfig = req.bindJSON(ValgrindPublisherConfig.class, formData);
            valgrindPublisher.setValgrindPublisherConfig( valgrindPublisherConfig );
            
            return valgrindPublisher;
        }
	}
}
