package org.jenkinsci.plugins.valgrind.util;

import hudson.FilePath;

public abstract class ValgrindUtil 
{
	public static String trimToNull( String s )
	{
		if ( s == null )
			return null;
		
		s = s.trim();
		
		if ( s.isEmpty() )
			return null;
		
		return s;
	}
	
	public static String join( FilePath[]  files, String sep )
	{
		String s = "";
		
		for( int i = 0; i <  files.length; ++i )
		{
			s +=  files[ i ].getName();
			
			if ( i + 1 <  files.length )
				s += sep;
		}
		
		return s;
	}
}
