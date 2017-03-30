package com.example.eposeasyselectsample.printer;

import java.io.Serializable;

public class PrinterInformation
		implements Serializable
{
	// --------------------------------------------------------------------------------------------
	public static final String	CLASS_NAME	= "printer_information";

	// --------------------------------------------------------------------------------------------
	private static final long serialVersionUID = -8013518944558095823L;

	// --------------------------------------------------------------------------------------------
	private String	mPrinterName;
	private int		mDeviceType;
	private String	mAddress	= null;
	private int		mLanguage;

	// ----------------------------------------------------------------------------------------
	/**
	 *	get device type
	 *
	 *	@return	int	device type
	 *
	 */
	public int getDeviceType()
	{
		return mDeviceType;
	}

	// ----------------------------------------------------------------------------------------
	/**
	 *	get printer name
	 *
	 *	@return String	printer name
	 *
	 */
	public String getPrinterName()
	{
		return mPrinterName;
	}

	// ----------------------------------------------------------------------------------------
	/**
	 *	get address
	 *
	 * @return String	address
	 *
	 */
	public String getAddress()
	{
	    return mAddress;
	}

	// ----------------------------------------------------------------------------------------
	/**
	 *	get language
	 *
	 * @return int	language
	 */
	public int getLanguage()
	{
		return mLanguage;
	}

	// ----------------------------------------------------------------------------------------
	/**
	 *	set device type
	 *
	 * @param deviceType	device type
	 *
	 */
	protected void setDeviceType(
			int	deviceType )
	{
		mDeviceType = deviceType;
	}

	// ----------------------------------------------------------------------------------------
	/**
	 *	set printer name
	 *
	 * @param printerName	printer name
	 *
	 */
	protected void setPrinterName(
			String	printerName )
	{
		mPrinterName = printerName;
	}

	// ----------------------------------------------------------------------------------------
	/**
	 *	set address
	 *
	 * @param address	address
	 *
	 */
	protected void setAddress(
			String	address )
	{
		mAddress = address;
	}

	// ----------------------------------------------------------------------------------------
	/**
	 *	set language
	 *
	 * @param language	language
	 *
	 */
	protected void setLanguage(
			int	language )
	{
		mLanguage = language;
	}
}
