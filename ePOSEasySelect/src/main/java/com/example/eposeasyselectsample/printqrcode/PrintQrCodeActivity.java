package com.example.eposeasyselectsample.printqrcode;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.epson.epsonio.DevType;
import com.example.eposeasyselectsample.R;
import com.example.eposeasyselectsample.common.ActivityInfo;
import com.example.eposeasyselectsample.common.MessageBox;
import com.example.eposeasyselectsample.printer.ChoosePrinterActivity;
import com.example.eposeasyselectsample.printer.InterfaceType;
import com.example.eposeasyselectsample.printer.PrintCallback;
import com.example.eposeasyselectsample.printer.PrinterInformation;

// ------------------------------------------------------------------------------------------------
public class PrintQrCodeActivity extends Activity
	implements	OnClickListener,
				PrintCallback
{
	// --------------------------------------------------------------------------------------------
	private static final int	QR_CODE_WIDTH	= 5;

	// --------------------------------------------------------------------------------------------
	private Button				mBtnPrintQrCode	= null;

	private PrinterInformation	mPrinterInfo	= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	onCreate
	 *
	 *	@param	savedInstanceState
	 *
	 */
	@Override
	protected void onCreate(
			Bundle	savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_print_qr_code_sample );

		EditText	edtPrinterName	= (EditText)findViewById( R.id.PQR_edt_PrinterName );
		EditText	edtInterface	= (EditText)findViewById( R.id.PQR_edt_Interface );
		EditText	edtAddress		= (EditText)findViewById( R.id.PQR_edt_MacAddress );

		edtPrinterName.setEnabled( false );
		edtInterface.setEnabled( false );
		edtAddress.setEnabled( false );

		mBtnPrintQrCode = (Button)findViewById( R.id.PQR_btn_Print );

		mBtnPrintQrCode.setEnabled( false );

		registClickListener();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onCreate
	 *
	 *	@param	savedInstanceState
	 *
	 */
	@Override
	protected void onActivityResult(
			int		requestCode,
			int		resultCode,
			Intent	intent )
	{
		if (RESULT_OK == resultCode) {
			mPrinterInfo = (PrinterInformation)intent.getSerializableExtra( PrinterInformation.CLASS_NAME );

			if (false != setPrinterInfo( mPrinterInfo )) {
				mBtnPrintQrCode.setEnabled( true );
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onResume
	 *
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onClick
	 *
	 *	@param	v
	 *
	 */
	@Override
	public void onClick(
			View	v )
	{
		switch (v.getId()) {
		case R.id.PQR_btn_SelectPrinter:
			runSelectPrinter();
			break;

		case R.id.PQR_btn_Print:
			runPrintQrCode();
			break;

		default:
			break;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onPrintCallback
	 *
	 *	@param	result
	 *
	 */
	@Override
	public void onPrintCallback(
			boolean	result )
	{
		if (false == result) {
			showErrorMessage( getString( R.string.PQR_msg_PrintError ) );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	regisg ClickListener
	 *
	 */
	private void registClickListener()
	{
		int[] clickTarget = {
			R.id.PQR_btn_SelectPrinter,
			R.id.PQR_btn_Print
		};

		for (int target : clickTarget) {
			Button button = (Button) findViewById( target );

			button.setOnClickListener( this );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	select printer information
	 *
	 *	@param	printerInfo
	 *
	 *	@return	boolean
	 *
	 */
	private boolean setPrinterInfo(
			PrinterInformation	printerInfo )
	{
		String	value		= "";

		EditText	edtPrinterName	= (EditText)findViewById( R.id.PQR_edt_PrinterName );
		EditText	edtInterface	= (EditText)findViewById( R.id.PQR_edt_Interface );
		EditText	edtAddress		= (EditText)findViewById( R.id.PQR_edt_MacAddress );

		if (null == printerInfo) {
			return false;
		}

		if ((false != printerInfo.getPrinterName().equals("")) ||
		 	(false != printerInfo.getAddress().equals(""))) {

		 	return false;
		}

		// printer name
		value = printerInfo.getPrinterName();

		edtPrinterName.setText( value );

		// interface
		switch (mPrinterInfo.getDeviceType()) {
		case DevType.TCP:		value = "Network"; 		break;
		case DevType.BLUETOOTH:	value = "Bluetooth";	break;
		default:										break; }

		edtInterface.setText( value );

		// MAC address
		value = printerInfo.getAddress();

		edtAddress.setText( value );

		return true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	select printer
	 *
	 */
	private void runSelectPrinter()
	{
        Intent intent = new Intent( this, ChoosePrinterActivity.class );

		ActivityInfo	activityInfo	= new ActivityInfo();

		activityInfo.setReturnActivity( this.getClass() );

		intent.putExtra( ActivityInfo.CLASS_NAME, activityInfo );

        startActivityForResult( intent, 0 );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	print qr code
	 *
	 */
	private void runPrintQrCode()
	{
		PrintQrCodeAsyncTask task = new PrintQrCodeAsyncTask(	this,
																mPrinterInfo.getPrinterName(),
																InterfaceType.convDtoP( mPrinterInfo.getDeviceType() ),
																mPrinterInfo.getAddress(),
																QR_CODE_WIDTH );

		try {
			task.setCallback( PrintQrCodeActivity.this );
			task.execute();

		} catch (IllegalStateException e) {
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	show error message
	 *
	 *	@param	message	error message
	 *
	 */
	 private void showErrorMessage(
	 		final String	message )
	 {
		runOnUiThread(
				new Runnable()
		{
			@Override
			public void run()
			{
				MessageBox	msgBox = new MessageBox( PrintQrCodeActivity.this )
				{
					// ----------------------------------------------------------------------------
					@Override
					protected void onButtonClick(
							DialogInterface	dialog,
							int				which )
					{
						// nothing
					}
				};

				msgBox.intMessageBox(	getString( R.string.dialog_title_error ),
										message,
										getString( R.string.dialog_btn_ok ),
										null,
										null );
				msgBox.show();
			}
		});
	}
}
