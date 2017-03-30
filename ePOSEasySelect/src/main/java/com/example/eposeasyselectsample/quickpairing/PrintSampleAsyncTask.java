package com.example.eposeasyselectsample.quickpairing;

import android.app.Activity;
import android.os.AsyncTask;

import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.example.eposeasyselectsample.common.CustomProgressDialog;
import com.example.eposeasyselectsample.printer.ComPrint;
import com.example.eposeasyselectsample.printer.PrintCallback;

// ------------------------------------------------------------------------------------------------
public class PrintSampleAsyncTask
	extends AsyncTask<Void, Void, Boolean>
{
	// --------------------------------------------------------------------------------------------
	// print text
	private static final String		PRINT_LINE				= "--------------------";
	private static final String		PRINT_TITLE				= "Sample Print";
	private static final String		PRINT_WIFI_ADDRESS		= "Network Address:";
	private static final String		PRINT_BLUETOOTH_ADDRESS	= "Bluetooth Address:";
	private static final String		PRINT_MESSAGE			= "Print successfully!!";

	// --------------------------------------------------------------------------------------------
	private CustomProgressDialog	mProgressDialog	= null;

	private Activity				mActivity		= null;

	private ComPrint				mComPrint		= null;

	private String					mPrinterName	= null;
	private int						mInterfaceType	= 0;
	private String					mAddress		= null;

	private PrintCallback			mPrintCallback	= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	constructer
	 *
	 *	@param	activity		Activity
	 *	@param	printerName		printer name
	 *	@param	interfaceType	DevType
	 *	@param	address			address
	 *
	 */
	protected PrintSampleAsyncTask(
			Activity	activity,
			String		printerName,
			int			interfaceType,
			String		address )
	{
		mActivity		= activity;

		mPrinterName	= printerName;
		mInterfaceType	= interfaceType;
		mAddress		= address;

		try {
			mProgressDialog = new CustomProgressDialog( mActivity );

			mProgressDialog.setCancelable( false );
			mProgressDialog.show();

		} catch (Exception e) {
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	doInBackground
	 *
	 *	@param	params
	 *
	 *	@return	Boolean
	 *
	 */
	@Override
	protected Boolean doInBackground(
			Void...	params )
	{
		if (false != isCancelled()) {
			return false;
		}

		return printSample();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onPostExecute
	 *
	 *	@param	result
	 *
	 */
	@Override
	protected void onPostExecute(
			Boolean	result )
	{
		// close progress dialog
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
		}

		if (null != mPrintCallback) {
			mPrintCallback.onPrintCallback( result );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	set print callback
	 *
	 *	@param	printCallbac
	 *
	 */
	protected void setCallback(
			PrintCallback	printCallbac )
	{
		mPrintCallback = printCallbac;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	print sample text
	 *
	 */
	private Boolean printSample()
	{
		int	retval	= ComPrint.RESULT_ERROR_UNKNOWN;

		mComPrint = new ComPrint();

		retval = mComPrint.open( mInterfaceType, mAddress, 0, 1000 );
		if (retval != ComPrint.RESULT_SUCCESS) {
			return false;
		}

		Builder	builder	= mComPrint.getBuilder( mPrinterName, Builder.LANG_EN );

		builder = makePritText( builder );
		if (null == builder) {
			return false;
		}

		// print
		retval = mComPrint.send( builder, 5000 );

		mComPrint.close();

		mComPrint = null;

		if (retval != ComPrint.RESULT_SUCCESS) {
			return false;
		}

		return true;
    }

	// --------------------------------------------------------------------------------------------
	/**
	 *	make print text
	 *
	 *	@param	builder
	 *
	 *	@return	boolean
	 *
	 */
	private Builder makePritText(
			Builder	builder )
	{
		if (null == builder) {
			return null;
		}

		try {
			// header text
			builder = makeHeaderText( builder );
			if (null == builder) {
				return null;
			}

			// doby
			builder = makeBodyText( builder );
			if (null == builder) {
				return null;
			}

			// feed & cut
			builder.addCut( Builder.CUT_FEED );

		} catch (EposException e) {
			return null;
		}

		return builder;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	make print header text
	 *
	 *	@param	builder
	 *
	 *	@return	boolean
	 *
	 */
	private Builder makeHeaderText(
			Builder	builder )
	{
		if (null == builder) {
			return null;
		}

		try {
			builder.addText( PRINT_LINE );
			builder.addFeedLine( 1 );

			builder.addText( PRINT_TITLE );
			builder.addFeedLine( 1 );

			builder.addText( PRINT_LINE );
			builder.addFeedLine( 2 );

		} catch (EposException e) {
			return null;
		}

		return builder;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	make body text
	 *
	 *	@param	builder
	 *
	 *	@return	boolean
	 *
	 */
	private Builder makeBodyText(
			Builder	builder )
	{
		if (null == builder) {
			return null;
		}

		try {
			builder.addText( mPrinterName );
			builder.addFeedLine( 1 );

			// port type
			switch (mInterfaceType) {
			case Print.DEVTYPE_TCP:			builder.addText( PRINT_WIFI_ADDRESS );		break;
			case Print.DEVTYPE_BLUETOOTH:	builder.addText( PRINT_BLUETOOTH_ADDRESS );	break;
			default:																	break; }

			// address
			builder.addText( mAddress );
			builder.addFeedLine( 5 );

			builder.addTextAlign( Builder.ALIGN_CENTER );
			builder.addText( PRINT_MESSAGE );
			builder.addFeedLine( 2 );

		} catch (EposException e) {
			return null;
		}

		return builder;
	}
}
