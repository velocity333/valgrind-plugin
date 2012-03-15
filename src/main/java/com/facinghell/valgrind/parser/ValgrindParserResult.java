package com.facinghell.valgrind.parser;

import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.util.ValgrindLogger;

public class ValgrindParserResult implements FilePath.FileCallable<ValgrindReport>
{
	private static final long serialVersionUID = -5475538646374717099L;
	private final BuildListener listener;
	private String pattern;
	
	public ValgrindParserResult( final BuildListener listener, String pattern )
	{
		this.listener = listener;
		this.pattern = pattern;
	}

	public ValgrindReport invoke(File basedir, VirtualChannel channel) throws IOException, InterruptedException
	{
		ValgrindLogger.log(listener, "looking for valgrind files in '" + basedir.getAbsolutePath() + "' with pattern '" + pattern + "'");
		ValgrindReport valgrindReport = new ValgrindReport();
		
		for ( String fileName : findValgrindsReports( basedir ) )
		{
			ValgrindLogger.log(listener, "parsing " + fileName + "...");
			try
			{
				ValgrindReport report = new ValgrindXmlParser2().parse( new File(basedir, fileName) );
				valgrindReport.integrate( report );
			} 
			catch (Exception e)
			{
				ValgrindLogger.log(listener, "failed to parse " + fileName + ": " + e.getMessage());
			}
		}

		return valgrindReport;
	}
	
	private String[] findValgrindsReports(File parentPath)
	{
		FileSet fs = Util.createFileSet(parentPath, this.pattern);
		DirectoryScanner ds = fs.getDirectoryScanner();
		return ds.getIncludedFiles();
	}
}
