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
		} 
		else
		{
			summary.append("<a href=\"valgrindResult\">");

			if (errorCount == 1)
				summary.append("one error, ");
			else
				summary.append(Integer.toString(errorCount) + " errors, ");

			summary.append(result.getReport().getDefinitelyLeakedBytes());
			summary.append(" bytes definitely lost");
			
			summary.append("</a>");
		}

		return summary.toString();
	}
	
}
