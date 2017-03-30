package com.example.eposeasyselectsample.printer;

import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;

// ------------------------------------------------------------------------------------------------
public class ComPrint
{
	// --------------------------------------------------------------------------------------------
	public static final int RESULT_SUCCESS			= 0;
	public static final int RESULT_ERROR_PARAMETER	= 1;
	public static final int RESULT_ERROR_OPEN		= 2;
	public static final int RESULT_ERROR_NOT_OPEN	= 3;
	public static final int RESULT_ERROR_TIME_OUT	= 4;
	public static final int RESULT_ERROR_FAILER		= 5;
	public static final int RESULT_ERROR_NOT_SUPPORT= 5;
	public static final int RESULT_ERROR_UNKNOWN	= 255;

	// --------------------------------------------------------------------------------------------
	private	Print	mPort	= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	check open
	 *
	 *
	 *	@return	boolean
	 *
	 */
	public boolean isOpen()
	{
		if (null == mPort) {
			return false;
		}

		return true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	open printer
	 *
	 *	@param	type
	 *	@param	device
	 *	@param	statusMonitor
	 *	@param	interval
	 *
	 *	@return	int
	 *
	 */
	public int open(
			int		type,
			String	device,
			int		statusMonitor,
			int		interval )
	{
		int result	= RESULT_ERROR_UNKNOWN;

		if (null != mPort) {
			close();
		}

		try {
			mPort = new  Print();

			mPort.openPrinter( type, device, statusMonitor, interval );

			result = RESULT_SUCCESS;

		} catch (EposException ie) {
			mPort = null;

			result = RESULT_ERROR_OPEN;
		}

		return result;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	close printer
	 *
	 *	@return	int
	 *
	 */
	public int close()
	{
		int result	= RESULT_ERROR_UNKNOWN;

		if (null == mPort) {
			return RESULT_ERROR_NOT_OPEN;
		}

		try {
			mPort.closePrinter();

			mPort = null;

			result = RESULT_SUCCESS;

		} catch (EposException ie) {
			result = RESULT_ERROR_FAILER;
		}

		return result;
	}


	// --------------------------------------------------------------------------------------------
	/**
	 *	get Builder class
	 *
	 *	@param	printerName	printer name
	 *	@param	language	printer language
	 *
	 *	@return	Builder
	 *
	 */
	public Builder getBuilder(
		String	printerName,
		int		language )
	{
		Builder builder	= null;

		try {
			builder = new Builder( printerName, language );

		} catch (EposException e) {
			builder = null;
		}

		return builder;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	send
	 *
	 *	@param	builder : send data
	 *	@param	timeout : timeout (ms)
	 *
	 *	@return	int
	 *
	 */
	public int send(
			Builder	builder,
			int		timeout )
	{
		int result	= RESULT_ERROR_UNKNOWN;

		if (null == mPort) {
			return RESULT_ERROR_NOT_OPEN;
		}

		try {
			int[] status = new int[1];

			mPort.sendData( builder, timeout, status );

			result = RESULT_SUCCESS;

		} catch (EposException ie) {
			if (ie.getErrorStatus() == EposException.ERR_TIMEOUT) {
				result = RESULT_ERROR_TIME_OUT;
			} else {
				result = RESULT_ERROR_FAILER;
			}
		}

		return result;
	}
}
