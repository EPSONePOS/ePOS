package com.example.eposeasyselectsample.printer;

import android.content.Context;
import android.os.AsyncTask;

import com.epson.epsonio.DevType;
import com.epson.epsonio.DeviceInfo;
import com.example.eposeasyselectsample.common.CustomProgressDialog;

// ------------------------------------------------------------------------------------------------
public class PrinterInfoAsyncTask
	extends AsyncTask<Void, Void, Integer>
{
	// --------------------------------------------------------------------------------------------
	public static final int REQUEST_PRINTER_INFO = 1;

	// --------------------------------------------------------------------------------------------
	private Context					mContext		= null;
	private CustomProgressDialog	mProgressDialog	= null;
	private DeviceInfo				mDeviceInfo		= null;
	private PrinterInformation		mPrinterInfo	= null;

	private PrinterInfoCallback		mCallback		= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	constructer
	 *
	 *	@param	context
	 *	@param	callback
	 *	@param	deviceInfo
	 *	@param	progress
	 *
	 */
	protected PrinterInfoAsyncTask(
			Context				context,
			PrinterInfoCallback	callback,
			DeviceInfo			deviceInfo,
			boolean				progress )
	{
		mContext	= context;
		mCallback	= callback;
		mDeviceInfo	= deviceInfo;

		try {
			if (false != progress) {
				mProgressDialog = new CustomProgressDialog( mContext );
				mProgressDialog.setCancelable( false );
				mProgressDialog.show();
			}
		} catch (NullPointerException e) {
			// nothing
		} catch (Exception e) {
			// nothing
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	doInBackground
	 *
	 *	@param	params
	 *
	 */
	@Override
	protected Integer doInBackground(
			Void...	params )
	{
		return getPrinterInfo();
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
			Integer	result )
	{
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
		}

		if (null != mCallback) {
			mCallback.printerInfoCallback( REQUEST_PRINTER_INFO, result, mPrinterInfo );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get printer info
	 *
	 *	@return	int
	 *
	 */
	private int getPrinterInfo()
	{
		ComEpsonIo	tmComm			= new ComEpsonIo();
		String[]	printerName		= {""};
		String[]	printerLanguage	= {""};

		int			deviceType	= DevType.ANY;
		int			result		= ComEpsonIo.RESULT_ERROR_UNKNOWN;

		if (null == mDeviceInfo) {
			return ComEpsonIo.RESULT_ERROR_PARAMETER;
		}

		deviceType	= mDeviceInfo.getDeviceType();

		result = tmComm.open( deviceType, mDeviceInfo.getDeviceName() );
		if (ComEpsonIo.RESULT_SUCCESS != result) {
			return ComEpsonIo.RESULT_ERROR_OPEN;
		}

		// get printer information
		result = tmComm.getPrinterInfo( ComEpsonIo.GS_I_PRINTER_NAME, printerName );
		if (ComEpsonIo.RESULT_SUCCESS != result) {
			tmComm.close();
			return result;
		}

		result = tmComm.getPrinterInfo( ComEpsonIo.GS_I_PRINTER_LANGUAGE, printerLanguage );
		if (ComEpsonIo.RESULT_SUCCESS != result) {
			tmComm.close();
			return result;
		}

		tmComm.close();

		if (0 == printerName.length) {
			return result;
		}

		mPrinterInfo = new PrinterInformation();

		mPrinterInfo.setDeviceType( mDeviceInfo.getDeviceType() );
		mPrinterInfo.setPrinterName( printerName[0] );
		mPrinterInfo.setLanguage( tmComm.getPrinterLanguageId( printerLanguage[0] ) );

		if (DevType.TCP  == mDeviceInfo.getDeviceType()) {
			mPrinterInfo.setAddress( mDeviceInfo.getMacAddress() );

		} else if (DevType.BLUETOOTH  == mDeviceInfo.getDeviceType()) {
			mPrinterInfo.setAddress( mDeviceInfo.getDeviceName() );

		} else {
			mPrinterInfo.setAddress( "" );
		}

		return result;
	}
}
