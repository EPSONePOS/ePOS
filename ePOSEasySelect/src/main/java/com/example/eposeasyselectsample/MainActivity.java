package com.example.eposeasyselectsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.eposeasyselectsample.printqrcode.PrintQrCodeActivity;
import com.example.eposeasyselectsample.quickpairing.QuickPairingActivity;

public class MainActivity extends Activity
	implements	OnClickListener

{
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

		setContentView( R.layout.activity_main );

		registClickListener();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onClick
	 *
	 *	@param	v
	 *
	 *	@return	void
	 */
	@Override
	public void onClick(
			View	v )
	{
		switch (v.getId()) {
		case R.id.Main_btn_QuickPairingSample:
			runQuickPairingSample();
			break;

		case R.id.Main_btn_PrintQRCodeSample:
			runPrintQrCodeSample();
			break;

		default:
			break;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	regist ClickListener
	 *
	 */
	private void registClickListener()
	{
		int[] clickTarget = {
			R.id.Main_btn_QuickPairingSample,
			R.id.Main_btn_PrintQRCodeSample
		};

		for (int target : clickTarget) {
			Button button = (Button) findViewById( target );

			button.setOnClickListener( this );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	Quick pairing sampel
	 *
	 */
	private void runQuickPairingSample()
	{
		Intent intent = new Intent( this, QuickPairingActivity.class );

		startActivity( intent );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	Print QR code sample
	 *
	 */
	private void runPrintQrCodeSample()
	{
		Intent intent = new Intent( this, PrintQrCodeActivity.class );

		startActivity( intent );
	}
}
