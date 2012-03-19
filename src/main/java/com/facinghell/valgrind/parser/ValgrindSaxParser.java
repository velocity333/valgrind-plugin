package com.facinghell.valgrind.parser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.facinghell.valgrind.model.ValgrindError;
import com.facinghell.valgrind.model.ValgrindErrorKind;
import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.model.ValgrindStacktrace;
import com.facinghell.valgrind.model.ValgrindStacktraceFrame;

public class ValgrindSaxParser implements Serializable
{
	private static final long serialVersionUID = -6889121223670989851L;	
	
	private ValgrindSourceCache sourceCache;
	
	private class Handler extends DefaultHandler
	{
		private ValgrindReport currentReport;
		private ValgrindError currentError;
		private ValgrindStacktrace currentStacktrace;
		private ValgrindStacktraceFrame currentStacktraceFrame;
		private StringBuilder data;
		private String currentExecutable = null;		
		private String path = "";
		
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException
		{
			path += "/" + qName;
			
			if ( path.equalsIgnoreCase("/valgrindoutput") )
				currentReport = new ValgrindReport();
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error") )
				currentError = new ValgrindError();
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/unique") )
				data = new StringBuilder();
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/kind") )
				data = new StringBuilder();
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/what") )
				data = new StringBuilder();
			
			if ( path.equalsIgnoreCase("/valgrindoutput/args/argv/exe") )
				data = new StringBuilder();
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/xwhat/text") )
				data = new StringBuilder();				
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/xwhat/leakedbytes") )
				data = new StringBuilder();				
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/xwhat/leakedblocks") )
				data = new StringBuilder();				
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/stack") && currentError.getStacktrace() == null )
				currentStacktrace = new ValgrindStacktrace();
			
			if ( currentStacktrace != null )
			{
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame") )
					currentStacktraceFrame = new ValgrindStacktraceFrame();
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/obj")  )
					data = new StringBuilder();
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/fn") )
					data = new StringBuilder();
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/dir") )
					data = new StringBuilder();
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/file") )
					data = new StringBuilder();
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/line") )
					data = new StringBuilder();
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException
		{			
			if ( path.equalsIgnoreCase("/valgrindoutput/error") )
			{
				currentError.setExecutable( currentExecutable );
				
				if ( currentError.getKind() != null )
					currentReport.addError( currentError );
				
				currentError = null;
			}	
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/unique") )
				currentError.setUniqueId( data.toString() );
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/kind") )
				currentError.setKind( ValgrindErrorKind.valueOf( data.toString() ) );							
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/what") )
				currentError.setDescription( data.toString() );
			
			if ( path.equalsIgnoreCase("/valgrindoutput/args/argv/exe") )
				currentExecutable = data.toString();
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/xwhat/text") )
				currentError.setDescription( data.toString() );				
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/xwhat/leakedbytes") )
				currentError.setLeakedBytes( Integer.valueOf(data.toString()) );				
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/xwhat/leakedblocks") )
				currentError.setLeakedBlocks( Integer.valueOf(data.toString()) );
			
			if ( path.equalsIgnoreCase("/valgrindoutput/error/stack") && currentStacktrace != null )
			{
				currentError.setStacktrace( currentStacktrace );
				currentStacktrace = null;				
			}
			
			if ( currentStacktraceFrame != null )
			{
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame") )
				{
					if ( currentStacktraceFrame.getLineNumber() != null )
						currentStacktraceFrame.setSourceCode( sourceCache.get(currentStacktraceFrame.getFilePath(), currentStacktraceFrame.getLineNumber().intValue() ) );
					
					currentStacktrace.addFrame( currentStacktraceFrame );
					currentStacktraceFrame = null;
				}
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/obj")  )
					currentStacktraceFrame.setObjectName( data.toString() );
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/fn") )
					currentStacktraceFrame.setFunctionName( data.toString() );
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/dir") )
					currentStacktraceFrame.setDirectoryName( data.toString() );
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/file") )
					currentStacktraceFrame.setFileName( data.toString() );
				
				if ( path.equalsIgnoreCase("/valgrindoutput/error/stack/frame/line") )
				{		
					try
					{
						currentStacktraceFrame.setLineNumber( Integer.valueOf( data.toString() ) );
					}
					catch( NumberFormatException e )
					{
					}
				}	
			}
			
			
			data = null;
			path = path.substring(0, path.length() - ( qName.length() + 1 ) );
		}

		public void characters(char ch[], int start, int length) throws SAXException
		{
			if ( data == null )
				return;
			
			data.append(new String(ch,start,length));
		}
		
		public ValgrindReport getReport()
		{
			return currentReport;
		}
	}
	
	public ValgrindSaxParser( ValgrindSourceCache sourceCache )
	{
		this.sourceCache = sourceCache;
	}
	
	public ValgrindReport parse( final File file ) throws ParserConfigurationException, SAXException, IOException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser saxParser = factory.newSAXParser();
		
		Handler handler = new Handler();
		
		saxParser.parse(file, handler);
		
		return handler.getReport();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			new ValgrindSaxParser(new ValgrindSourceCache(10, 5)).parse(new File("core_test.memcheck") );
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
