package org.jenkinsci.plugins.valgrind.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValgrindReport implements Serializable
{
	private static final long serialVersionUID = -9036045639715893780L;
	
	private List<ValgrindError> errors;	
	private Set<String> executables;
	
	public static class ErrorsPerProcess
	{
		public List<ValgrindError> errors = new ArrayList<ValgrindError>();
		public String pid;
		public String ppid;
		public String executable;
		public List<ErrorsPerProcess> childs = new ArrayList<ErrorsPerProcess>();
		public ErrorsPerProcess parent;
		
		public List<ValgrindError> getOverlapErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.Overlap );
		}		
		
		public List<ValgrindError> getSyscallParamErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.SyscallParam );
		}		
		
		public List<ValgrindError> getInvalidFreeErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.InvalidFree );
		}		
		
		public List<ValgrindError> getMismatchedFreeErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.MismatchedFree );
		}	

		public List<ValgrindError> getUninitializedValueErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.UninitValue );
		}		
		
		public List<ValgrindError> getUninitializedConditionErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.UninitCondition );
		}	
		
		public List<ValgrindError> getInvalidReadErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.InvalidRead );
		}
		
		public List<ValgrindError> getInvalidWriteErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.InvalidWrite );
		}
		
		public List<ValgrindError> getLeakDefinitelyLostErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.Leak_DefinitelyLost );
		}
		
		public List<ValgrindError> getLeakPossiblyLostErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.Leak_PossiblyLost );
		}
		
		public List<ValgrindError> getLeakStillReachableErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.Leak_StillReachable );
		}
		
		public List<ValgrindError> getLeakIndirectlyLostErrors()
		{
			return getErrorsByKind( ValgrindErrorKind.Leak_IndirectlyLost );
		}
		
		private List<ValgrindError> getErrorsByKind( ValgrindErrorKind valgrindErrorKind )
		{
			if ( errors == null || errors.isEmpty() )
				return null;
			
			List<ValgrindError> result = new ArrayList<ValgrindError>();
			
			for ( ValgrindError error: errors )
				if ( error.getKind().equals( valgrindErrorKind ) )
					result.add( error );
			
			if ( result.isEmpty() )
				return null;
			
			return result;		
		}
	}
	
	public List<ErrorsPerProcess> getErrorsPerProcess()
	{
		Map<String, ErrorsPerProcess> lookup = new HashMap<String, ErrorsPerProcess>();
		
		for( ValgrindError error : errors )
		{
			if ( !lookup.containsKey(error.getPid()) )
			{
				ErrorsPerProcess errorsPerProcess = new ErrorsPerProcess();
				errorsPerProcess.pid = error.getPid();
				errorsPerProcess.ppid = error.getPpid();
				errorsPerProcess.executable = error.getExecutable();
				
				lookup.put(error.getPid(), errorsPerProcess);
			}

			lookup.get(error.getPid()).errors.add(error);
		}
		
		List<ErrorsPerProcess> res = new ArrayList<ErrorsPerProcess>(lookup.values());
		for( ErrorsPerProcess errorsPerProcess : res )
		{
			if ( lookup.containsKey(errorsPerProcess.ppid) )
			{
				errorsPerProcess.parent = lookup.get(errorsPerProcess.ppid);
				errorsPerProcess.parent.childs.add(errorsPerProcess);				
			}
		}
		
		return res;
	}
	
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
	
	public int getOverlapErrorCount()
	{		
		return getErrorCountByKind(ValgrindErrorKind.Overlap);
	}
	
	public int getOverlapErrorCountByExecutable( String executable )
	{		
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.Overlap, executable);
	}	
	
	public List<ValgrindError> getOverlapErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.Overlap );
	}	
		
	public int getSyscallParamErrorCount()
	{		
		return getErrorCountByKind(ValgrindErrorKind.SyscallParam);
	}
	
	public int getSyscallParamErrorCountByExecutable( String executable )
	{		
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.SyscallParam, executable);
	}	
	
	public List<ValgrindError> getSyscallParamErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.SyscallParam );
	}	
	
	public int getInvalidFreeErrorCount()
	{		
		return getErrorCountByKind(ValgrindErrorKind.InvalidFree);
	}
	
	public int getInvalidFreeErrorCountByExecutable( String executable )
	{		
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.InvalidFree, executable);
	}	
	
	public List<ValgrindError> getInvalidFreeErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.InvalidFree );
	}	
	
	public int getMismatchedFreeErrorCount()
	{		
		return getErrorCountByKind(ValgrindErrorKind.MismatchedFree);
	}
	
	public int getMismatchedFreeErrorCountByExecutable( String executable )
	{		
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.MismatchedFree, executable);
	}	
	
	public List<ValgrindError> getMismatchedFreeErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.MismatchedFree );
	}	
	
	public int getUninitializedValueErrorCount()
	{		
		return getErrorCountByKind(ValgrindErrorKind.UninitValue);
	}
	
	public int getUninitializedValueErrorCountByExecutable( String executable )
	{		
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.UninitValue, executable);
	}	
	
	public List<ValgrindError> getUninitializedValueErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.UninitValue );
	}	
	
	public int getUninitializedConditionErrorCount()
	{		
		return getErrorCountByKind(ValgrindErrorKind.UninitCondition);
	}
	
	public int getUninitializedConditionErrorCountByExecutable( String executable )
	{		
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.UninitCondition, executable);
	}	
	
	public List<ValgrindError> getUninitializedConditionErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.UninitCondition );
	}	
	
	public int getInvalidReadErrorCount()
	{		
		return getErrorCountByKind(ValgrindErrorKind.InvalidRead);
	}
	
	public int getInvalidReadErrorCountByExecutable( String executable )
	{		
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.InvalidRead, executable);
	}
	
	public List<ValgrindError> getInvalidReadErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.InvalidRead );
	}

	public int getInvalidWriteErrorCount()
	{
		return getErrorCountByKind(ValgrindErrorKind.InvalidWrite);
	}
	
	public int getInvalidWriteErrorCountByExecutable( String executable )
	{
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.InvalidWrite, executable);
	}
	
	public List<ValgrindError> getInvalidWriteErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.InvalidWrite );
	}
	
	public int getLeakDefinitelyLostErrorCount()
	{
		return getErrorCountByKind(ValgrindErrorKind.Leak_DefinitelyLost);
	}
	
	public int getLeakDefinitelyLostErrorCountByExecutable( String executable )
	{
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.Leak_DefinitelyLost, executable);
	}
	
	public List<ValgrindError> getLeakDefinitelyLostErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.Leak_DefinitelyLost );
	}
	
	public int getLeakPossiblyLostErrorCount()
	{
		return getErrorCountByKind(ValgrindErrorKind.Leak_PossiblyLost);
	}
	
	public int getLeakPossiblyLostErrorCountByExecutable( String executable )
	{
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.Leak_PossiblyLost, executable);
	}
	
	public List<ValgrindError> getLeakPossiblyLostErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.Leak_PossiblyLost );
	}
	
	public int getLeakStillReachableErrorCount()
	{
		return getErrorCountByKind(ValgrindErrorKind.Leak_StillReachable);
	}
	
	public int getLeakStillReachableErrorCountByExecutable( String executable )
	{
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.Leak_StillReachable, executable);
	}
	
	public List<ValgrindError> getLeakStillReachableErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.Leak_StillReachable );
	}
	
	public int getLeakIndirectlyLostErrorCount()
	{
		return getErrorCountByKind(ValgrindErrorKind.Leak_IndirectlyLost);
	}
	
	public int getLeakIndirectlyLostErrorCountByExecutable( String executable )
	{
		return getErrorCountByKindAndExecutable(ValgrindErrorKind.Leak_IndirectlyLost, executable);
	}
	
	public List<ValgrindError> getLeakIndirectlyLostErrors()
	{
		return getErrorsByKind( ValgrindErrorKind.Leak_IndirectlyLost );
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
		if ( errors == null || errors.isEmpty() )
			return null;
		
		List<ValgrindError> result = new ArrayList<ValgrindError>();
		
		for ( ValgrindError error: errors )
			if ( error.getKind().equals( valgrindErrorKind ) )
				result.add( error );
		
		if ( result.isEmpty() )
			return null;
		
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
