package com.facinghell.valgrind.model;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;

public class ValgrindReport
{
	private List<ValgrindInvalidReadError> invalidReadErrors;	
	private List<ValgrindInvalidWriteError> invalidWriteErrors;
	private List<ValgrindLeakError> leakDefinitelyLostErrors;
	private List<ValgrindLeakError> leakPossiblyLostErrors;
	
	public void print()
	{
		System.out.println("invalid reads: ");
		if (invalidReadErrors != null)
			for (ValgrindInvalidReadError error : invalidReadErrors)
				System.out.println(error.toString());

		System.out.println("invalid writes: ");
		if (invalidWriteErrors != null)
			for (ValgrindInvalidWriteError error : invalidWriteErrors)
				System.out.println(error.toString());

		System.out.println("leaks (definitely lost): ");
		if (leakDefinitelyLostErrors != null)
			for (ValgrindLeakError error : leakDefinitelyLostErrors)
				System.out.println(error.toString());
		
		System.out.println("leaks (possibly lost): ");
		if (leakPossiblyLostErrors != null)
			for (ValgrindLeakError error : leakPossiblyLostErrors)
				System.out.println(error.toString());		
	}
	
	public void integrate( ValgrindReport valgrindReport )
	{
		if ( valgrindReport.invalidReadErrors != null )
		{
			for( ValgrindInvalidReadError error : valgrindReport.invalidReadErrors )
				addInvalidReadError( error );
		}
		
		if ( valgrindReport.invalidWriteErrors != null )
		{
			for( ValgrindInvalidWriteError error : valgrindReport.invalidWriteErrors )
				addInvalidWriteError( error );
		}
		
		if ( valgrindReport.leakDefinitelyLostErrors != null )
		{
			for( ValgrindLeakError error : valgrindReport.leakDefinitelyLostErrors )
				addLeakDefinitelyLostError( error );
		}
		
		if ( valgrindReport.leakPossiblyLostErrors != null )
		{
			for( ValgrindLeakError error : valgrindReport.leakPossiblyLostErrors )
				addLeakPossiblyLostError( error );
		}
	}
	
	@Exported
	public int getErrorCount()
	{
		return 
		getInvalidReadErrorCount() + 
		getInvalidWriteErrorCount() + 
		getLeakDefinitelyLostErrorCount() + 
		getLeakPossiblyLostErrorCount();
	}
	
	@Exported
	public int getInvalidReadErrorCount()
	{
		if ( invalidReadErrors != null )
			return invalidReadErrors.size();
		
		return 0;
	}
	
	@Exported
	public int getInvalidWriteErrorCount()
	{
		if ( invalidWriteErrors != null )
			return invalidWriteErrors.size();
		
		return 0;
	}
	
	@Exported
	public int getLeakDefinitelyLostErrorCount()
	{
		if ( leakDefinitelyLostErrors != null )
			return leakDefinitelyLostErrors.size();
		
		return 0;
	}
	
	@Exported
	public int getLeakPossiblyLostErrorCount()
	{
		if ( leakPossiblyLostErrors != null )
			return leakPossiblyLostErrors.size();
		
		return 0;
	}	
	
	public void addInvalidReadError( ValgrindInvalidReadError invalidReadError )
	{
		if ( invalidReadErrors == null )
			invalidReadErrors = new ArrayList<ValgrindInvalidReadError>();
		
		invalidReadErrors.add( invalidReadError );
	}
	
	public void addInvalidWriteError( ValgrindInvalidWriteError invalidWriteError )
	{
		if ( invalidWriteErrors == null )
			invalidWriteErrors = new ArrayList<ValgrindInvalidWriteError>();
		
		invalidWriteErrors.add( invalidWriteError );
	}
	
	public void addLeakDefinitelyLostError( ValgrindLeakError leakError )
	{
		if ( leakDefinitelyLostErrors == null )
			leakDefinitelyLostErrors = new ArrayList<ValgrindLeakError>();
		
		leakDefinitelyLostErrors.add( leakError );
	}
	
	public void addLeakPossiblyLostError( ValgrindLeakError leakError )
	{
		if ( leakPossiblyLostErrors == null )
			leakPossiblyLostErrors = new ArrayList<ValgrindLeakError>();
		
		leakPossiblyLostErrors.add( leakError );
	}

	public List<ValgrindInvalidReadError> getInvalidReadErrors()
	{
		return invalidReadErrors;
	}

	public void setInvalidReadErrors(List<ValgrindInvalidReadError> invalidReadErrors)
	{
		this.invalidReadErrors = invalidReadErrors;
	}

	public List<ValgrindInvalidWriteError> getInvalidWriteErrors()
	{
		return invalidWriteErrors;
	}

	public void setInvalidWriteErrors(List<ValgrindInvalidWriteError> invalidWriteErrors)
	{
		this.invalidWriteErrors = invalidWriteErrors;
	}

	public List<ValgrindLeakError> getLeakDefinitelyLostErrors()
	{
		return leakDefinitelyLostErrors;
	}

	public void setLeakDefinitelyLostErrors(List<ValgrindLeakError> leakDefinitelyLostErrors)
	{
		this.leakDefinitelyLostErrors = leakDefinitelyLostErrors;
	}

	public List<ValgrindLeakError> getLeakPossiblyLostErrors()
	{
		return leakPossiblyLostErrors;
	}

	public void setLeakPossiblyLostErrors(List<ValgrindLeakError> leakPossiblyLostErrors)
	{
		this.leakPossiblyLostErrors = leakPossiblyLostErrors;
	}	
}
