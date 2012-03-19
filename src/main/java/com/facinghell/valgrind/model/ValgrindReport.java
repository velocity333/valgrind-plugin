package com.facinghell.valgrind.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ValgrindReport implements Serializable
{
	private static final long serialVersionUID = -9036045639715893780L;
	
	private List<ValgrindError> errors;	
	private Set<String> executables;
	
	public void addError( ValgrindError error )
	{
		if ( error == null )
			return;
		
		if ( errors == null )
			errors = new ArrayList<ValgrindError>();
		
		errors.add( error );
		addExecutable( error.getExecutable() );
	}
	
	public void addExecutable( String executable )
	{
		if ( executables == null )
			executables = new HashSet<String>();			
		
		executables.add( executable );
	}
	
	public void integrate( ValgrindReport valgrindReport )
	{
		if ( valgrindReport.errors != null )
		{
			for( ValgrindError error : valgrindReport.errors )
			{
				addError( error );
			}
		}		
	}
	
	public ValgrindError findError( String executable, String id )
	{
		for ( ValgrindError error : getAllErrors() )
			if ( error.getUniqueId().equals( id ) && error.getExecutable().equals( executable ) )
				return error;
		
		return null;
	}
	
	public List<ValgrindError> getAllErrors()
	{
		return errors;
	}
	
	public int getDefinitelyLeakedBytes()
	{
		if ( errors == null )
			return 0;
		
		int bytes = 0;
		
		for ( ValgrindError error : errors )
		{
			if ( error.getKind() != ValgrindErrorKind.Leak_DefinitelyLost )
				continue;
			
			if ( error.getLeakedBytes() == null )
				continue;
			
			bytes += error.getLeakedBytes().intValue();
		}
		
		return bytes;
	}
	
	public int getPossiblyLeakedBytes()
	{
		if ( errors == null )
			return 0;
		
		int bytes = 0;
		
		for ( ValgrindError error : errors )
		{
			if ( error.getKind() != ValgrindErrorKind.Leak_PossiblyLost )
				continue;
			
			if ( error.getLeakedBytes() == null )
				continue;
			
			bytes += error.getLeakedBytes().intValue();
		}
		
		return bytes;
	}
	
	public int getLeakedBytes( ValgrindErrorKind kind, String executable )
	{
		if ( errors == null )
			return 0;
		
		int bytes = 0;
		
		for ( ValgrindError error : errors )
		{
			if ( error.getKind() != kind )
				continue;
			
			if ( !error.getExecutable().equals(executable) )
				continue;			
			
			if ( error.getLeakedBytes() == null )
				continue;
			
			bytes += error.getLeakedBytes().intValue();
		}
		
		return bytes;
	}

	/*
	 * Special error counts
	 */
	
	public int getInvalidReadErrorCount()
	{		
		return getErrorCountByKind(ValgrindErrorKind.InvalidRead);
	}
	
	public int getInvalidReadErrorCountByExecutable( String executable )
	{		
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.InvalidRead, executable);
	}

	public int getInvalidWriteErrorCount()
	{
		return getErrorCountByKind(ValgrindErrorKind.InvalidWrite);
	}
	
	public int getInvalidWriteErrorCountByExecutable( String executable )
	{
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.InvalidWrite, executable);
	}
	
	public int getLeakDefinitelyLostErrorCount()
	{
		return getErrorCountByKind(ValgrindErrorKind.Leak_DefinitelyLost);
	}
	
	public int getLeakDefinitelyLostErrorCountByExecutable( String executable )
	{
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.Leak_DefinitelyLost, executable);
	}
	
	public int getLeakPossiblyLostErrorCount()
	{
		return getErrorCountByKind(ValgrindErrorKind.Leak_PossiblyLost);
	}
	
	public int getLeakPossiblyLostErrorCountByExecutable( String executable )
	{
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.Leak_PossiblyLost, executable);
	}
	
	/*
	 * General error count 
	 */
	
	public int getErrorCount()
	{
		if ( errors == null )
			return 0;
		
		return errors.size();
	}
	
	public int getErrorCountByKind( ValgrindErrorKind valgrindErrorKind )
	{
		if ( errors == null )
			return 0;
		
		int count = 0;
		for ( ValgrindError error: errors )
		{
			if ( error.getKind() == null )
				continue;
			
			if ( error.getKind().equals( valgrindErrorKind ) )
				count++;
		}
		
		return count;
	}
	
	public int getErrorCountByKindAndExecutable( ValgrindErrorKind valgrindErrorKind, String executable )
	{
		if ( errors == null )
			return 0;
		
		int count = 0;
		for ( ValgrindError error: errors )
		{
			if ( error.getKind() == null || error.getExecutable() == null )
				continue;
			
			if ( error.getKind().equals( valgrindErrorKind ) && error.getExecutable().equals(executable) )
				count++;
		}
		
		return count;
	}	
	
	public List<ValgrindError> getErrorsByKind( ValgrindErrorKind valgrindErrorKind )
	{		
		if ( errors == null )
			return null;
		
		List<ValgrindError> result = new ArrayList<ValgrindError>();
		
		for ( ValgrindError error: errors )
			if ( error.getKind().equals( valgrindErrorKind ) )
				result.add( error );
		
		return result;		
	}
	
	public List<ValgrindExaminedExecutable> getExaminedExecutables()
	{
		if ( executables == null || executables.isEmpty() )
			return null;
		
		List<ValgrindExaminedExecutable> list = new ArrayList<ValgrindExaminedExecutable>();
		
		for( String name : executables )
			list.add( new ValgrindExaminedExecutable(name, this) );			
		
		return list;
	}	
}
