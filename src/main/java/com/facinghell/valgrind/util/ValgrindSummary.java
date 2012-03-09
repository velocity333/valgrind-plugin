package com.facinghell.valgrind.util;

import com.facinghell.valgrind.ValgrindResult;

public class ValgrindSummary
{
	/**
	 * Creates an HTML valgrind summary.
	 * 
	 * @param result
	 *            the valgrind result object
	 * @return the HTML fragment representing the valgrind report summary
	 */
	public static String createReportSummary(ValgrindResult result)
	{

		StringBuilder summary = new StringBuilder();
		int errorCount = result.getReport().getErrorCount();

		if (errorCount == 0)
		{
			summary.append("no errors");
		} else
		{
			summary.append("<a href=\"valgrindResult\">");

			if (errorCount == 1)
				summary.append("one error");
			else
				summary.append(Integer.toString(errorCount) + " errors");

			summary.append("</a>:");
		}

		return summary.toString();
	}

	/**
	 * Creates an HTML Valgrind detailed summary.
	 * 
	 * @param result
	 *            the valgrind result object
	 * @return the HTML fragment representing the valgrind report details
	 *         summary
	 */
	public static String createReportSummaryDetails(ValgrindResult result)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<li>");
		builder.append( Integer.toString( result.getReport().getInvalidReadErrorCount() ) + " invalid reads");
		builder.append("</li><li>");
		builder.append( Integer.toString( result.getReport().getInvalidWriteErrorCount() ) + " invalid write");
		builder.append("</li><li>");
		builder.append( Integer.toString( result.getReport().getLeakDefinitelyLostErrorCount() ) + " leaks (definitely lost)");
		builder.append("</li><li>");
		builder.append( Integer.toString( result.getReport().getLeakPossiblyLostErrorCount() ) + " leaks (possibly lost)");		
		builder.append("</li>");
		return builder.toString();
	}

}
