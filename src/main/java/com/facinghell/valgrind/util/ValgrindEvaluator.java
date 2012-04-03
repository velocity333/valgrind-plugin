package com.facinghell.valgrind.util;

import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;

import com.facinghell.valgrind.config.ValgrindPublisherConfig;
import com.facinghell.valgrind.model.ValgrindReport;

public class ValgrindEvaluator
{
	private ValgrindPublisherConfig config;
	private BuildListener listener;
	
	public ValgrindEvaluator( ValgrindPublisherConfig config, BuildListener listener )
	{
		this.config = config;
		this.listener = listener;
	}
	
	public void evaluate( ValgrindReport report, AbstractBuild<?, ?> build )
	{
		build.setResult( evaluate(
				report.getLeakDefinitelyLostErrorCount(), 
				config.getUnstableThresholdDefinitelyLost(), 
				config.getFailThresholdDefinitelyLost() ) );

		build.setResult( evaluate(
				report.getInvalidReadErrorCount() + report.getInvalidWriteErrorCount(), 
				config.getUnstableThresholdInvalidReadWrite(), 
				config.getFailThresholdInvalidReadWrite() ) );
		
		build.setResult( evaluate(
				report.getErrorCount(), 
				config.getUnstableThresholdTotal(), 
				config.getFailThresholdTotal() ) );
	}	
	
	private boolean exceedsThreshold( int errorCount, String threshold )
	{
		if ( threshold == null || threshold.isEmpty() )
			return false;
		
		try
		{
			Integer i = Integer.valueOf(threshold);	
			return errorCount >= i.intValue();
		}
		catch( NumberFormatException e )
		{
			ValgrindLogger.log( listener, "ERROR: '" + threshold + "' is not a valid threshold" );
		}
		
		return false;
	}
	
	private Result evaluate( int errorCount, String unstableThreshold, String failThreshold )
	{
		if ( exceedsThreshold( errorCount, failThreshold ) )
			return Result.FAILURE;
		
		if ( exceedsThreshold( errorCount, unstableThreshold ) )
			return Result.UNSTABLE;
		
		return Result.SUCCESS;
	}
	
}
