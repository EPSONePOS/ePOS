package com.example.eposeasyselectsample.printer;


// ------------------------------------------------------------------------------------------------
public interface PrinterInfoCallback
{
	// --------------------------------------------------------------------------------------------
	/**
	 *	callback
	 *
	 *	@param	requestCode
	 *	@param	resultCode
	 *	@param	resultPrinterInfo
	 *
	 */
	public void printerInfoCallback(
			final int					requestCode,
			final int					resultCode,
			final PrinterInformation	resultPrinterInfo );
}
