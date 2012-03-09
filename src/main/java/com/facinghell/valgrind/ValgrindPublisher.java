package com.facinghell.valgrind;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import com.facinghell.valgrind.config.ValgrindPublisherConfig;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

/**
 * 
 * @author Johannes Ohlemacher
 *
 */
public class ValgrindPublisher extends Publisher
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

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException
	{
		listener.getLogger().println("[Valgrind] collecting valgrind results");
		listener.getLogger().println("[Valgrind] pattern:        " + valgrindPublisherConfig.getPattern());
		listener.getLogger().println("[Valgrind] invalid reads:  " + valgrindPublisherConfig.isInvalidReads());
		listener.getLogger().println("[Valgrind] invalid writes: " + valgrindPublisherConfig.isInvalidWrites());
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
