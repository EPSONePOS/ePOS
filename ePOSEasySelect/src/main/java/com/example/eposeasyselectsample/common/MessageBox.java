package com.example.eposeasyselectsample.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

// ------------------------------------------------------------------------------------------------
public abstract class MessageBox
{
	// --------------------------------------------------------------------------------------------
	private AlertDialog.Builder	mMsgBox	= null;

	// --------------------------------------------------------------------------------------------
	protected abstract void onButtonClick( DialogInterface dialog, int which );

	// --------------------------------------------------------------------------------------------
	/**
	 *	constructer
	 *
	 * @param context
	 *
	 */
	public MessageBox(
		Context context )
	{
		mMsgBox = new AlertDialog.Builder( context );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	initalize
	 *
	 * @param title
	 * @param message
	 * @param button_ok
	 * @param button_cancel
	 * @param button_neutral
	 *
	 */
	public void intMessageBox(
			String	title,
			String	message,
			String	button_ok,
			String	button_cancel,
			String	button_neutral )
	{
		if (null == mMsgBox) {
			return ;
		}

		// title
		if (null != title) {
			mMsgBox.setTitle( title );
		}

		// message
		if (null != message) {
			mMsgBox.setMessage( message );
		}

		// OK button
		if (null != button_ok) {
			mMsgBox.setPositiveButton( button_ok,
				new DialogInterface.OnClickListener()
				{
					public void onClick(
							DialogInterface	dialog,
							int				whichButton )
					{
						onButtonClick( dialog, whichButton );
					}
				});
		}

		// cancel button
		if (null != button_cancel) {
			mMsgBox.setNegativeButton( button_cancel,
				new DialogInterface.OnClickListener()
				{
					public void onClick(
							DialogInterface	dialog,
							int				whichButton )
					{
						onButtonClick( dialog, whichButton );
					}
				});
		}

		// button 3
		if (null != button_neutral) {
			mMsgBox.setNeutralButton( button_neutral,
				new DialogInterface.OnClickListener()
				{
					public void onClick(
							DialogInterface	dialog,
							int				whichButton )
					{
						onButtonClick( dialog, whichButton );
					}
				});
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	show message box
	 *
	 */
	public void show()
	{
		if (null == mMsgBox) {
			return ;
		}

		try {
			mMsgBox.setCancelable( false );
			mMsgBox.create();

			mMsgBox.show();
		} catch (Exception e) {
			// nothing
		}
	}
}
