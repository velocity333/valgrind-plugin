package com.facinghell.valgrind;

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

import org.kohsuke.stapler.StaplerRequest;

import com.facinghell.valgrind.config.ValgrindPublisherConfig;
import com.facinghell.valgrind.model.ValgrindError;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.model.ValgrindStacktraceFrame;
import com.facinghell.valgrind.parser.ValgrindParserResult;
import com.facinghell.valgrind.util.ValgrindEvaluator;
import com.facinghell.valgrind.util.ValgrindLogger;
import com.facinghell.valgrind.util.ValgrindSourceFile;


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
				throws hudson.model.Descriptor.FormException
		{

			ValgrindPublisher valgrindPublisher = new ValgrindPublisher();
			ValgrindPublisherConfig valgrindPublisherConfig = req.bindJSON(ValgrindPublisherConfig.class,
					formData);
			valgrindPublisher.setValgrindPublisherConfig(valgrindPublisherConfig);

			return valgrindPublisher;
		}
	}
}
