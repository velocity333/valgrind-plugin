package org.jenkinsci.plugins.valgrind;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.jenkinsci.plugins.valgrind.util.ValgrindLogger;
import org.jenkinsci.plugins.valgrind.util.ValgrindUtil;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;


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
	public static enum LeakCheckLevel {
		full, yes, summary, no
	}
	private final String valgrindExecutable;
	private final String workingDirectory;
	private final String includePattern;
	private final String outputDirectory;
	private final String outputFileEnding;
	private final boolean showReachable;
	private final boolean undefinedValueErrors;
	private final LeakCheckLevel leakCheckLevel;
	private final String programOptions;
	private final String valgrindOptions;
	private final boolean trackOrigins;

	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public ValgrindBuilder(String valgrindExecutable,
			String workingDirectory, 
			String includePattern, 
			String outputDirectory,
			String outputFileEnding,
			boolean showReachable,
			boolean undefinedValueErrors,
			LeakCheckLevel leakCheckLevel,
			String programOptions,
			String valgrindOptions,
			boolean trackOrigins)
	{
		this.valgrindExecutable = valgrindExecutable.trim();
		this.workingDirectory = workingDirectory.trim();
		this.includePattern = includePattern.trim();
		this.outputDirectory = outputDirectory.trim();
		this.outputFileEnding = outputFileEnding.trim();
		this.showReachable = showReachable;
		this.undefinedValueErrors = undefinedValueErrors;
		this.leakCheckLevel = leakCheckLevel;
		this.programOptions = programOptions;
		this.valgrindOptions = valgrindOptions;
		this.trackOrigins = trackOrigins;
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
			EnvVars env = build.getEnvironment(null);
			env.put("PROGRAM_NAME", file.getName());
			
			FilePath workDir = build.getWorkspace().child(env.expand(workingDirectory));
			if (!workDir.exists() || !workDir.isDirectory())
				workDir.mkdirs();

			FilePath outDir = build.getWorkspace().child(env.expand(outputDirectory));
			if (!outDir.exists() || !outDir.isDirectory())
				outDir.mkdirs();

			List<String> cmds = new ArrayList<String>();
			
			String valgrindExecutable = env.expand(this.valgrindExecutable);
			String leakCheckLevelString = LeakCheckLevel.full.toString();
			if ( this.leakCheckLevel != null )
				leakCheckLevelString = this.leakCheckLevel.toString();
			
			if( valgrindExecutable == null || valgrindExecutable.isEmpty() )
				cmds.add("valgrind"); //use valgrind from path
			else
				cmds.add(valgrindExecutable); //use specified valgrind			
			
			cmds.add("--tool=memcheck");
			cmds.add("--leak-check=" + leakCheckLevelString);
			
			cmds.add( boolean2argument("--show-reachable", showReachable) );
			cmds.add( boolean2argument("--undef-value-errors", undefinedValueErrors) );			
			cmds.add( boolean2argument("--track-origins", trackOrigins) );
		
			cmds.add("--xml=yes");
			cmds.add("--xml-file=" + outDir.child(file.getName() + env.expand(outputFileEnding)).getRemote());
			
			if ( valgrindOptions != null && !valgrindOptions.isEmpty() )
			{
				String[] args = env.expand(valgrindOptions).split(" ");
				for( String arg : args )
				{
					arg = arg.trim();
					
					if ( arg != null && !arg.isEmpty() )
						cmds.add(arg);
				}
			}
			
			cmds.add(file.getRemote());
			
			if ( programOptions != null && !programOptions.isEmpty() )
			{
				String[] args = env.expand(programOptions).split(" ");
				for( String arg : args )
				{
					arg = arg.trim();
					
					if ( arg != null && !arg.isEmpty() )
						cmds.add(arg);
				}
			}

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
			EnvVars env = build.getEnvironment(null);
			
			FilePath[] files = build.getWorkspace().child(env.expand(workingDirectory)).list(env.expand(includePattern));

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
	
	public String getValgrindExecutable()
	{
		return valgrindExecutable;
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

	public boolean isShowReachable()
	{
		return showReachable;
	}

	public boolean isUndefinedValueErrors()
	{
		return undefinedValueErrors;
	}
	
	public LeakCheckLevel getLeakCheckLevel()
	{
		return leakCheckLevel;
	}
	
	public String getProgramOptions()
	{
		return programOptions;
	}
	
	public String getValgrindOptions()
	{
		return valgrindOptions;
	}

	@Override
	public DescriptorImpl getDescriptor()
	{
		return (DescriptorImpl) super.getDescriptor();
	}
	
	@SuppressWarnings("unused")
	private ListBoxModel doFillLeakCheckLevelItems() 
	{
		ListBoxModel items = new ListBoxModel();
		
		for (LeakCheckLevel level : LeakCheckLevel.values()) 
			items.add(level.name(), String.valueOf(level.ordinal()));

		return items;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder>
	{
		@SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> aClass)
		{
			return true;
		}

		public LeakCheckLevel[] getLeakCheckLevels() {
			return LeakCheckLevel.values();
		}

		public FormValidation doCheckIncludePattern(@QueryParameter String includePattern) throws IOException, ServletException
		{
			if (includePattern.length() == 0)
				return FormValidation.error("Please set a pattern");
			
			return FormValidation.ok();
		}
		
		public FormValidation doCheckOutputFileEnding(@QueryParameter String value) throws IOException, ServletException
		{
			if (value.length() == 0)
				return FormValidation.error("Please set a file ending for generated xml reports");
			if (value.charAt(0) != '.' )
				return FormValidation.warning("File ending does not start with a dot");
			
			return FormValidation.ok();
		}		

		public String getDisplayName()
		{
			return "Run Valgrind";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
		{
			return super.configure(req, formData);
		}
	}	
}
