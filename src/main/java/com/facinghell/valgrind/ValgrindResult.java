package com.facinghell.valgrind;

import java.io.Serializable;

import com.facinghell.valgrind.model.ValgrindReport;
import com.facinghell.valgrind.parser.ValgrindSummary;

public class ValgrindResult implements Serializable
{	
	private static final long serialVersionUID = -5347879997716170059L;
	
	private ValgrindReport report;

	public ValgrindReport getReport()
	{
		return report;
	}

	public void setReport(ValgrindReport report)
	{
		this.report = report;
	}

	/**
	 * Renders the summary Valgrind report for the build result.
	 * 
	 * @return the HTML fragment of the summary Valgrind report
	 */
	public String getSummary()
	{
		System.err.println("create report summary");
		return ValgrindSummary.createReportSummary(this);
	}

	/**
	 * Renders the detailed summary Valgrind report for the build result.
	 * 
	 * @return the HTML fragment of the summary Valgrind report
	 */
	public String getDetails()
	{
		System.err.println("create report details");
		return ValgrindSummary.createReportSummaryDetails(this);
	}
}
