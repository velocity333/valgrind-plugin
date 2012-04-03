package com.facinghell.valgrind;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.facinghell.valgrind.util.ValgrindLogger;
import com.facinghell.valgrind.util.ValgrindUtil;

/**
 * Sample {@link Builder}.
 * 
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link ValgrindBuilder} is created. The created instance is persisted to the
 * project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #name}) to remember the configuration.
 * 
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
 * invoked.
 * 
 * @author Kohsuke Kawaguchi
 */
public class ValgrindBuilder extends Builder
{
	private final String workingDirectory;
	private final String includePattern;
	private final String outputDirectory;
	private final String outputFileEnding;
	private final Boolean showReachable;
	private final Boolean undefinedValueErrors;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public ValgrindBuilder(String workingDirectory, 
			String includePattern, 
			String outputDirectory,
			String outputFileEnding,
			Boolean showReachable,
			Boolean undefinedValueErrors)
	{
		this.workingDirectory = workingDirectory;
		this.includePattern = includePattern;
		this.outputDirectory = outputDirectory;
		this.outputFileEnding = outputFileEnding;
		this.showReachable = showReachable;
		this.undefinedValueErrors = undefinedValueErrors;
	}
	
	private String boolean2argument( String name, Boolean value )
	{
		if ( value != null && value.booleanValue() )
			return name + "=yes";
		
		return name + "=no";			
	}

	@SuppressWarnings("rawtypes")
	private int callValgrind(AbstractBuild build, Launcher launcher, BuildListener listener, FilePath file)
			throws IOException, InterruptedException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try
		{
			FilePath workDir = build.getWorkspace().child(workingDirectory);
			if (!workDir.exists() || !workDir.isDirectory())
				workDir.mkdirs();

			FilePath outDir = build.getWorkspace().child(outputDirectory);
			if (!outDir.exists() || !outDir.isDirectory())
				outDir.mkdirs();

			List<String> cmds = new ArrayList<String>();
			cmds.add("valgrind");
			cmds.add("--tool=memcheck");
			cmds.add("--leak-check=full");			
			
			cmds.add( boolean2argument("--show-reachable", showReachable) );
			cmds.add( boolean2argument("--undef-value-errors", undefinedValueErrors) );			
		
			cmds.add("--xml=yes");
			cmds.add("--xml-file=" + outDir.child(file.getName() + outputFileEnding).getRemote());
			cmds.add(file.getRemote());

			Launcher.ProcStarter starter = launcher.launch();
			starter = starter.pwd(workDir);
			starter = starter.stdout(os);
			starter = starter.stderr(os);
			starter = starter.cmds(cmds);

			return starter.join();
		} finally
		{
			ValgrindLogger.log(listener, os.toString());
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
	{
		try
		{
			FilePath[] files = build.getWorkspace().child(workingDirectory).list(includePattern);

			ValgrindLogger.log( listener, "executable files: " + ValgrindUtil.join(files, ", "));

			for (FilePath file : files)
			{
				int exitCode = callValgrind(build, launcher, listener, file);
				if (exitCode != 0)
					return false;
			}
		} 
		catch (Exception e)
		{
			ValgrindLogger.log(listener, "ERROR: " + e.getMessage());
			return false;
		}

		return true;
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor()
	{
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Descriptor for {@link ValgrindBuilder}. Used as a singleton. The class is
	 * marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See
	 * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
	{
		/**
		 * To persist global configuration information, simply store it in a
		 * field and call save().
		 * 
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */
		private boolean useFrench;

		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 * 
		 * @param value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 */
		public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException
		{
			if (value.length() == 0)
				return FormValidation.error("Please set a name");
			if (value.length() < 4)
				return FormValidation.warning("Isn't the name too short?");
			return FormValidation.ok();
		}

		@SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> aClass)
		{
			// Indicates that this builder can be used with all kinds of project
			// types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName()
		{
			return "Run Valgrind";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
		{
			// To persist global configuration information,
			// set that to properties and call save().
			useFrench = formData.getBoolean("useFrench");
			// ^Can also use req.bindJSON(this, formData);
			// (easier when there are many fields; need set* methods for this,
			// like setUseFrench)
			save();
			return super.configure(req, formData);
		}

		/**
		 * This method returns true if the global configuration says we should
		 * speak French.
		 */
		public boolean useFrench()
		{
			return useFrench;
		}
	}

	public String getWorkingDirectory()
	{
		return workingDirectory;
	}

	public String getIncludePattern()
	{
		return includePattern;
	}

	public String getOutputDirectory()
	{
		return outputDirectory;
	}

	public String getOutputFileEnding()
	{
		return outputFileEnding;
	}

	public Boolean getShowReachable()
	{
		return showReachable;
	}

	public Boolean getUndefinedValueErrors()
	{
		return undefinedValueErrors;
	}


}
