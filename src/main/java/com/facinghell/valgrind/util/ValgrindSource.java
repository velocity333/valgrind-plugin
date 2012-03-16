package com.facinghell.valgrind.util;

import hudson.FilePath;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tools.ant.filters.StringInputStream;

import com.facinghell.valgrind.model.ValgrindError;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.model.ValgrindStacktraceFrame;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

public abstract class ValgrindSource
{
	private static final int GENERATED_HTML_SOURCE_HEADER_SIZE = 12;
	private static final int GENERATED_HTML_SOURCE_FOOTER_SIZE = 9;
	private static final String ERROR_LINE_COLOR = "#FCAF3E";
	private static final String SOURCE_NOT_AVAIABLE_MESSAGE = "<b>Source code not available</b>";
	private static final int prefixLines = 10;
	private static final int suffixLines = 5;
	
	public static void updateSourceCode(AbstractBuild<?, ?> build, ValgrindStacktraceFrame frame)
	{
		if ( frame.getLineNumber() == null || frame.getLineNumber().intValue() == 0 )
		{
			frame.setSourceCode( SOURCE_NOT_AVAIABLE_MESSAGE );
			return;
		}
		
		if ( frame.getFileName() == null || frame.getFileName().isEmpty() )
		{
			frame.setSourceCode( SOURCE_NOT_AVAIABLE_MESSAGE );
			return;
		}		
		
		try
		{
			FilePath filePath = build.getWorkspace().child(frame.getFilePath());
			if ( !filePath.exists() )
			{
				frame.setSourceCode( SOURCE_NOT_AVAIABLE_MESSAGE );
				return;
			}
	
			String sourceCode = highlightSource(filePath.readToString());
			
			@SuppressWarnings("unchecked")
			List<String> lines = IOUtils.readLines( new StringInputStream(sourceCode) );
			StringBuilder output = new StringBuilder();
			ListIterator<String> it = lines.listIterator();
			int currentLine = 0;
			int errorLine = frame.getLineNumber().intValue() + GENERATED_HTML_SOURCE_HEADER_SIZE;
			
			//System.err.println( "line of interest: " + frame.getLineNumber().intValue() );
			while( it.hasNext() )
			{
				currentLine++;
				String line = it.next();
				
				boolean append = false;
				
				//html header
				if ( currentLine <= GENERATED_HTML_SOURCE_HEADER_SIZE )
					append = true;					
				
				//lines of interest
				if ( currentLine >= errorLine - prefixLines &&  
					 currentLine <= errorLine + suffixLines )
				{
					append = true;
				}

				//html footer
				if ( currentLine > lines.size() - GENERATED_HTML_SOURCE_FOOTER_SIZE )
					append = true;
				

				if ( currentLine == errorLine )
				{
			        output.append("</code></td></tr>\n");
			        output.append("<tr><td bgcolor=\"" + ERROR_LINE_COLOR + "\">\n");
			        output.append("<code>\n");
			        output.append(line + "\n");
			        output.append("</code></td></tr>\n");
			        output.append("<tr><td>\n");
			        output.append("<code>\n");			        
				}
				else if ( append )
					output.append( line + "\n" );
			}

			frame.setSourceCode( output.toString() );			
		} 
		catch (Exception e)
		{
			frame.setSourceCode("Error: " + StringEscapeUtils.escapeHtml(e.getMessage()));
		}
	}

	public static void updateSourceCode(AbstractBuild<?, ?> build, ValgrindReport report)
	{
		if (report == null || report.getAllErrors() == null)
			return;

		for (ValgrindError error : report.getAllErrors())
		{
			if (error.getStacktrace() == null || error.getStacktrace().getFrames() == null)
				continue;

			for (ValgrindStacktraceFrame frame : error.getStacktrace().getFrames())
			{
				updateSourceCode(build, frame);
			}
		}
	}

	public static String highlightSource( String src ) throws IOException
	{
		JavaSource source = new JavaSourceParser().parse( src );
		JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
		StringWriter writer = new StringWriter();
		JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
		options.setShowLineNumbers(true);
		options.setAddLineAnchors(true);
		converter.convert(source, options, writer);
		return writer.toString();
	}
}
