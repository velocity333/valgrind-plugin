package com.facinghell.valgrind.parser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.facinghell.valgrind.model.ValgrindError;
import com.facinghell.valgrind.model.ValgrindErrorKind;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.model.ValgrindStacktrace;
import com.facinghell.valgrind.model.ValgrindStacktraceFrame;

public class ValgrindXmlParser implements Serializable
{
	private static final long serialVersionUID = -3804982443628621529L;
	private XPath errorPath = XPath.newInstance("/valgrindoutput/error");
	private XPath errorStackPath = XPath.newInstance("stack");
	private XPath errorStackFramePath = XPath.newInstance("frame");
	private XPath functionNamePath = XPath.newInstance("fn");
	private XPath directoryNamePath = XPath.newInstance("dir");
	private XPath fileNamePath = XPath.newInstance("file");
	private XPath objectNamePath = XPath.newInstance("obj");
	private XPath lineNumberPath = XPath.newInstance("line");
	
	public ValgrindXmlParser() throws JDOMException
	{
	}	

	public ValgrindReport parse( final File file ) throws IOException, JDOMException
	{
		ValgrindReport valgrindReport = new ValgrindReport();
		
		Document doc = new SAXBuilder().build( file );	
    
	    for( Object object : errorPath.selectNodes( doc ) )
	    {
	    	try
	    	{
	    		valgrindReport.addError( parseError(object) );
	    	}
	    	catch( Exception e )
	    	{	    	
	    	}	    		    	
	    }	

		return valgrindReport;
	}	

	private ValgrindError parseError( Object object ) throws JDOMException
	{
		ValgrindError error = new ValgrindError();		
		error.setKind( ValgrindErrorKind.valueOf( stringOf(object, "kind") ) );		
		error.setUniqueId( stringOf(object, "unique") );
		error.setExecutable( stringOf(object, "/valgrindoutput/args/argv/exe") );		
		error.setLeakedBytes( integerOf(object, "xwhat/leakedbytes") );
		error.setLeakedBlocks( integerOf(object, "xwhat/leakedblocks") );		
		error.setDescription( stringOf(object, "what") );
		if ( error.getDescription() == null || error.getDescription().equals("") )
			error.setDescription( stringOf(object, "xwhat/text") );
		
		error.setStacktrace( parseStack( errorStackPath.selectSingleNode(object)));
		return error;	
	}
	
	private ValgrindStacktrace parseStack( Object object ) throws JDOMException
	{
		ValgrindStacktrace stacktrace = new ValgrindStacktrace();
		
		for( Object frame : errorStackFramePath.selectNodes( object ) )
			stacktrace.addFrame( parseStacktraceFrame(frame) );			
		
		return stacktrace;
	}
	
	private String stringOf( Object object, String path )
	{		
		try
		{
			XPath xpath = XPath.newInstance(path);
			return xpath.valueOf( object ).trim();
		}
		catch( JDOMException e )
		{
			return null;
		}				
	}
	
	private Integer integerOf( Object object, String path )
	{
		String s = stringOf( object, path );
		if ( s == null )
			return null;
		
		try
		{
			return Integer.valueOf( s );			
		}
		catch( NumberFormatException e )
		{
			return null;
		}		
	}
	
	private ValgrindStacktraceFrame parseStacktraceFrame( Object object ) throws JDOMException
	{
		ValgrindStacktraceFrame frame = new ValgrindStacktraceFrame();
		
		frame.setDirectoryName( directoryNamePath.valueOf(object) );
		frame.setFileName( fileNamePath.valueOf(object) );
		frame.setObjectName( objectNamePath.valueOf(object) );
		frame.setFunctionName( functionNamePath.valueOf(object) );
		
		String lineNumberString = lineNumberPath.valueOf(object).trim();
		if ( !lineNumberString.isEmpty() )
			frame.setLineNumber( Integer.valueOf(lineNumberString) );
		
		return frame;		
	}
}
