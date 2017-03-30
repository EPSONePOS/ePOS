package com.example.eposeasyselectsample.printqrcode;

import android.app.Activity;
import android.os.AsyncTask;

import com.epson.easyselect.EasySelect;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.example.eposeasyselectsample.common.CustomProgressDialog;
import com.example.eposeasyselectsample.printer.ComPrint;
import com.example.eposeasyselectsample.printer.PrintCallback;

// ------------------------------------------------------------------------------------------------
public class PrintQrCodeAsyncTask
	extends AsyncTask<Void, Void, Boolean>
{
	// --------------------------------------------------------------------------------------------
	private static final String		PRINT_TEXT_DEVICE		= "Device:";
	private static final String		PRINT_TEXT_INTERFACE	= "Interface:";
	private static final String		PRINT_TEXT_ADDRESS		= "Address:";

	// --------------------------------------------------------------------------------------------
	private CustomProgressDialog	mProgressDialog	= null;
	private int						mWidth			= 5;		// QR code size

	private Activity				mActivity 		= null;

	private String					mPrinterName	= null;
	private int						mInterfaceType	= 0;
	private String					mAddress		= null;

	private PrintCallback			mPrintCallback	= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	constructer
	 *
	 *	@param	activity Activity
	 *	@param	printerName		printer name
	 *	@param	interfaceType	interface type
	 *	@param	address			address
	 *	@param	width QR Code widht
	 *
	 */
	protected PrintQrCodeAsyncTask(
			Activity	activity,
			String		printerName,
			int			interfaceType,
			String		address,
			int			width )
	{
		mActivity		= activity;

		mPrinterName	= printerName;
		mInterfaceType	= interfaceType;
		mAddress		= address;

		mWidth			= width;

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
		return printQrCode( mWidth );
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
	 *	pritn QR Code
	 *
	 *	@param	qrCodeSize	QR Code size
	 *
	 */
	private Boolean printQrCode(
			int	qrCodeSize )
	{
		ComPrint	comPrinter	= new ComPrint();
		Builder		builder		= comPrinter.getBuilder( mPrinterName, Builder.LANG_EN );
		int			retval		= ComPrint.RESULT_SUCCESS;

		// header
		builder = makeHeader( builder );
		if (null == builder) {
			return false;
		}

		// QR code
		builder = makeQrCode( builder, qrCodeSize );
		if (null == builder) {
			return false;
		}

		retval = comPrinter.open( mInterfaceType, mAddress, 0, 1000 );
		if (retval != ComPrint.RESULT_SUCCESS) {
			return false;
		}

		retval = comPrinter.send( builder, 5000 );
		comPrinter.close();

		if (retval != ComPrint.RESULT_SUCCESS) {
			return false;
		}

		return true;
    }

	// --------------------------------------------------------------------------------------------
	/**
	 *	printer information
	 *
	 *	@param	builder
	 *
	 *	@return	boolean
	 *
	 */
	private Builder makeHeader(
			Builder	builder )
	{
		if (null == builder) {
			return null;
		}

		try {	// header
			// device name
			builder.addText( PRINT_TEXT_DEVICE );
			builder.addText( mPrinterName );
			builder.addFeedLine( 1 );

			// interface
			String	value	= null;
			switch (mInterfaceType) {
			case Print.DEVTYPE_TCP:			value = "Network"; 		break;
			case Print.DEVTYPE_BLUETOOTH:	value = "Bluetooth";	break;
			default:												break; }

			builder.addText( PRINT_TEXT_INTERFACE );
			builder.addText( value );
			builder.addFeedLine( 1 );

			// MAC address
			builder.addText( PRINT_TEXT_ADDRESS );
			builder.addText( mAddress );
			builder.addFeedLine( 1 );

			builder.addFeedLine( 1 );

		} catch (EposException e) {
			return null;
		}

		return builder;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	make QR Code
	 *
	 *	@param	builder
	 *	@param	width
	 *
	 *	@return	Builder
	 *
	 */
	private Builder	makeQrCode(
			Builder	builder,
			int		width )
	{
    	String qrCode	= new String();

		if (null == builder) {
			return null;
		}

		try {
			EasySelect easySelect	= new EasySelect();

			// create QR code data from EasySelect library
			qrCode = easySelect.createQR(	mPrinterName,
											mInterfaceType,
											mAddress );
			if (null == qrCode) {
				return null;
			}

			builder.addTextAlign( Builder.ALIGN_CENTER );

			// QR Code
			builder.addSymbol(	qrCode,
								Builder.SYMBOL_QRCODE_MODEL_2,
								Builder.LEVEL_L,
								width,
								width,
								0 );

			// feed & cut
			builder.addFeedLine( 1 );
			builder.addCut( Builder.CUT_FEED );

		} catch (EposException e) {
			return null;
		}

		return builder;
	}
}
