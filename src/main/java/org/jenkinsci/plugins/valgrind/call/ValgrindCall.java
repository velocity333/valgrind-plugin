package org.jenkinsci.plugins.valgrind.call;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.plugins.valgrind.util.ValgrindLogger;

public class ValgrindCall
{
	private ValgrindExecutable		valgrindExecutable;
	private EnvVars					env;
	private String					programName;
	private String					workingDirectory;	

	private List<ValgrindOption>	valgrindOptions		= new ArrayList<ValgrindOption>();		
	private List<String>			programArguments	= new ArrayList<String>();
	
	public ValgrindCall()
	{		
	}
	
	public void setValgrindExecutable( ValgrindExecutable valgrindExecutable )
	{
		this.valgrindExecutable = valgrindExecutable;
	}
	
	public void setEnv(EnvVars env)
	{
		this.env = env;
	}
	
	public void setProgramName(String programName)
	{
		this.programName = programName;		
		
		if ( this.programName != null && this.programName.isEmpty() )
			this.programName = null;
	}
	
	public void setWorkingDirectory(String workingDirectory)
	{
		this.workingDirectory = workingDirectory;		
	
		if ( this.workingDirectory != null && this.workingDirectory.isEmpty() )
			this.workingDirectory = null;
	}
	
	public void addValgrindOption(ValgrindOption option)
	{
		if (option != null)
			valgrindOptions.add(option);
	}
	
	public void addProgramArguments(String... arguments)
	{
		for (String arg : arguments)
		{
			if ( arg != null )
				programArguments.add(arg);
		}
	}	

	public int exec(BuildListener listener, Launcher launcher, ByteArrayOutputStream output) throws IOException, InterruptedException
	{
		if ( valgrindExecutable == null )
			throw new IllegalStateException("valgrind executable is null");
		
		if ( programName == null )
			throw new IllegalStateException("program name is null");
		
		if ( env == null )
			throw new IllegalStateException("environment is null");
		
		if ( workingDirectory == null )
			throw new IllegalStateException("workingDirectory is null");
		
		List<String> cmds = new ArrayList<String>();

		cmds.add(valgrindExecutable.getExecutable());

		for (ValgrindOption option : valgrindOptions)
		{
			try
			{
				cmds.add(env.expand(option.getArgumentString(valgrindExecutable.getVersion())));				
			}
			catch(ValgrindOptionNotApplicableException e)
			{
				ValgrindLogger.log(listener, "WARNING, option '" + option.getName() + "' is not applicable: " + e.getMessage());				
			}			
		}

		cmds.add(programName);

		for (String argument : programArguments)
			cmds.add(env.expand(argument));

		Launcher.ProcStarter starter = launcher.launch();
		starter = starter.pwd(workingDirectory);
		starter = starter.stdout(output);
		starter = starter.stderr(output);
		starter = starter.cmds(cmds);

		return starter.join();
	}

}
