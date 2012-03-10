package com.facinghell.valgrind.util;

import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;

import hudson.model.AbstractBuild;

import com.facinghell.valgrind.model.ValgrindError;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.model.ValgrindStacktraceFrame;

public abstract class ValgrindSource
{
	public static void updateSourceCode( AbstractBuild<?, ?> build, ValgrindStacktraceFrame frame )
	{
		frame.setSourceCode("my source code");
		
//		try
//		{
//			String sourceCode = build.getWorkspace().child( frame.getFileName() ).readToString();
//			frame.setSourceCode( StringEscapeUtils.escapeHtml(sourceCode) );
//		} 
//		catch (IOException e)
//		{
//			frame.setSourceCode( "Error: " + StringEscapeUtils.escapeHtml(e.getMessage()) );			
//		}	
	}
	
	public static void updateSourceCode( AbstractBuild<?, ?> build, ValgrindReport report )
	{
		if ( report == null || report.getAllErrors() == null )
			return;
			
		for ( ValgrindError error : report.getAllErrors() )
		{
			if ( error.getStacktrace() == null || error.getStacktrace().getFrames() == null )
				continue;
			
			for ( ValgrindStacktraceFrame frame : error.getStacktrace().getFrames() )
			{
				updateSourceCode(build, frame);
			}
		}		
	}
}
