package org.jenkinsci.plugins.valgrind;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.jenkinsci.plugins.valgrind.config.ValgrindPublisherConfig;
import org.jenkinsci.plugins.valgrind.model.ValgrindError;
import org.jenkinsci.plugins.valgrind.model.ValgrindReport;
import org.jenkinsci.plugins.valgrind.model.ValgrindStacktraceFrame;
import org.jenkinsci.plugins.valgrind.parser.ValgrindParserResult;
import org.jenkinsci.plugins.valgrind.util.ValgrindEvaluator;
import org.jenkinsci.plugins.valgrind.util.ValgrindLogger;
import org.jenkinsci.plugins.valgrind.util.ValgrindSourceFile;
import org.kohsuke.stapler.StaplerRequest;

/**
 * 
 * @author Johannes Ohlemacher
 * 
 */
public class ValgrindPublisher extends Recorder
{
	private ValgrindPublisherConfig valgrindPublisherConfig;	

	public ValgrindPublisher(ValgrindPublisherConfig valgrindPublisherConfig)
	{
		this.valgrindPublisherConfig = valgrindPublisherConfig;
	}

	@Override
	public ValgrindPublisherDescriptor getDescriptor()
	{
		return DESCRIPTOR;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project)
	{
		return new ValgrindProjectAction(project);
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
		if (!canContinue(build.getResult()))
			return true;
		
		if ( valgrindPublisherConfig.getPattern() == null || valgrindPublisherConfig.getPattern().isEmpty() )
		{
			ValgrindLogger.log(listener, "ERROR: no pattern for valgrind xml files configured");
			return false;
		}

		ValgrindLogger.log(listener, "Analysing valgrind results");		

		ValgrindParserResult parser = new ValgrindParserResult(listener, valgrindPublisherConfig.getPattern());
		ValgrindReport valgrindReport;

		valgrindReport = build.getWorkspace().act(parser);		
		ValgrindResult valgrindResult = new ValgrindResult(build, valgrindReport);		
		
		new ValgrindEvaluator(valgrindPublisherConfig, listener).evaluate(valgrindReport, build); 
		
		if ( valgrindReport.getAllErrors() != null && !valgrindReport.getAllErrors().isEmpty() )
		{
			Map<String, String> sourceFiles = retrieveSourceFiles( listener, build.getRootDir(), launcher.getChannel(), valgrindReport, build.getModuleRoot() );	
			valgrindResult.setSourceFiles( sourceFiles );		
		}

		ValgrindBuildAction buildAction = new ValgrindBuildAction(build, valgrindResult,
				valgrindPublisherConfig);
		build.addAction(buildAction);

		ValgrindLogger.log(listener, "Ending the valgrind analysis.");

		return true;
	}

	public ValgrindPublisherConfig getValgrindPublisherConfig()
	{
		return valgrindPublisherConfig;
	}

	public void setValgrindPublisherConfig(ValgrindPublisherConfig valgrindPublisherConfig)
	{
		this.valgrindPublisherConfig = valgrindPublisherConfig;
	}
	
	
	private Map<String, String> retrieveSourceFiles( BuildListener listener, File localRoot, VirtualChannel channel, ValgrindReport valgrindReport, FilePath basedir )
	{		
        File directory = new File(localRoot, ValgrindSourceFile.SOURCE_DIRECTORY);
        
        if ( !directory.exists() ) 
        {
            if ( !directory.mkdirs() )
            {
            	ValgrindLogger.log(listener, "ERROR: failed to create local directory for source files: '" + directory.getAbsolutePath() + "'");
            	return null;
            }
        }		
		
		Map<String, String> sourceFiles = new HashMap<String, String>();
		
		int index = 0;
		
		for( ValgrindError valgrindError : valgrindReport.getAllErrors() )
		{
			if ( valgrindError == null || valgrindError.getStacktrace() == null || valgrindError.getStacktrace().getFrames() == null )
				continue;
			
			for ( ValgrindStacktraceFrame frame : valgrindError.getStacktrace().getFrames() )
			{
				if ( frame == null )
					continue;
				
				String filePath =  frame.getFilePath();
				
				if ( filePath == null || filePath.isEmpty() || sourceFiles.containsKey( filePath ) )
					continue;				
				
				FilePath file = new FilePath( basedir, filePath );
				
				index++;				
				sourceFiles.put( filePath, retrieveSourceFile( listener, directory, channel, file, index ) );
			}
		}
		
		if ( sourceFiles.isEmpty() )
			return null;		
		
		return sourceFiles;
	}
	
	private String retrieveSourceFile( BuildListener listener, File localDirectory, VirtualChannel channel, FilePath file, int index )
	{		
		try
		{			
			if ( !file.exists() )
			{
				ValgrindLogger.log(listener, "'" + file.getRemote() + "' does not exist, source code won't be available");
				return null;
			}
			
			if ( file.isDirectory() )
			{
				ValgrindLogger.log(listener, "WARN: '" + file.getRemote() + "' is a directory, source code won't be available");
				return null;
			}		
			
			String fileName = "source_" + index + ".tmp";
			File masterFile = new File( localDirectory, fileName );
			
			ValgrindLogger.log(listener, "copying source file '" + file.getRemote() + "' to '" + fileName + "'...");
			
			if ( masterFile.exists() )
			{
				ValgrindLogger.log(listener, "WARN: local file '" + fileName + "' already exists");
				return null;
			}
            
            FileOutputStream outputStream = new FileOutputStream(masterFile);
            
            file.copyTo(outputStream);            
            
			return fileName;			
		}
		catch (Exception e)
		{
			ValgrindLogger.log(listener, "ERROR: failed to retrieve '" + file.getRemote() + "', " + e.getMessage() );
		}
		
		return null;		
	}

	@Extension
	public static final ValgrindPublisherDescriptor DESCRIPTOR = new ValgrindPublisherDescriptor();

	public static final class ValgrindPublisherDescriptor extends BuildStepDescriptor<Publisher>
	{
		private int linesBefore = 10;
		private int linesAfter = 5;
		
		public ValgrindPublisherDescriptor()
		{
			super(ValgrindPublisher.class);
			load();
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
		{
			linesBefore = formData.getInt("linesBefore");
			linesAfter = formData.getInt("linesAfter");
			save();
			return super.configure(req, formData);
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

		public int getLinesBefore()
		{
			return linesBefore;
		}

		public int getLinesAfter()
		{
			return linesAfter;
		}
		
		public ValgrindPublisherConfig getConfig()
		{
			return new ValgrindPublisherConfig();
		}	
		
        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData)
                throws hudson.model.Descriptor.FormException {     
        	
        	formData.remove("kind");
        	formData.remove("stapler-class");
            
            ValgrindPublisherConfig config = req.bindJSON(ValgrindPublisherConfig.class, formData);
            return new ValgrindPublisher(config);
        }
	}
}
