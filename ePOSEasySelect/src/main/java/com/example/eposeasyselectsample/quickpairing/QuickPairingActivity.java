package com.example.eposeasyselectsample.quickpairing;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.epson.easyselect.EasySelect;
import com.epson.easyselect.EasySelectInfo;
import com.epson.eposprint.Print;
import com.example.eposeasyselectsample.R;
import com.example.eposeasyselectsample.common.BroadcastCallback;
import com.example.eposeasyselectsample.common.BroadcastManager;
import com.example.eposeasyselectsample.common.MessageBox;
import com.example.eposeasyselectsample.common.bluetooth.BluetoothController;
import com.example.eposeasyselectsample.common.bluetooth.IntentListBluetooth;
import com.example.eposeasyselectsample.common.wifi.IntentListWiFi;
import com.example.eposeasyselectsample.common.wifi.WiFiController;
import com.example.eposeasyselectsample.printer.ComEpsonIo;
import com.example.eposeasyselectsample.printer.ComPrint;
import com.example.eposeasyselectsample.printer.PrintCallback;
import com.example.eposeasyselectsample.quickpairing.camera.CameraController;
import com.example.eposeasyselectsample.quickpairing.camera.CameraPreviewCallback;
import com.example.eposeasyselectsample.quickpairing.nfc.NFCController;

// ------------------------------------------------------------------------------------------------
public class QuickPairingActivity
	extends	Activity
	implements	OnClickListener,
				PrintCallback,
				BroadcastCallback,
				CameraPreviewCallback
{
	// --------------------------------------------------------------------------------------------
	private static final String		LI_LABEL			= "li_label";
	private static final String		LI_VALUE			= "li_value";

	private static final int		ACTION_NONE			= 0;
	private static final int		ACTION_CONNECT		= 1;
	private static final int		ACTION_PRINT		= 2;

	private static final int		WAITE_WIFI_SCAN		= 2000;

	// --------------------------------------------------------------------------------------------
	// View
	private Button					mPrintButton		= null;
	private ImageView				mNfcImage			= null;
	private LinearLayout			mNfcSettingsLayout	= null;
	private Button					mNfcSettingsButton	= null;
	private TextView				mNfcSettingsText	= null;
	private TextView				mConnectingText		= null;;

	private Thread					mOpenThread			= null;;

	// --------------------------------------------------------------------------------------------
	private EasySelect				mEasySelect				= null;	// EasySelect Library
	private EasySelectInfo			mEasySelectInfo			= null;

	private boolean					mOpenedPrinter			= false;

	private NFCController			mNfcCtr					= null;
	private CameraController		mCameraCtr				= null;

	private BarcodeManager			mBarcodeManager			= null;

	private WiFiController			mWifiController			= null;
	private BluetoothController		mBluetoothController	= null;

	private BroadcastManager		mBroadcastManager		= null;

	private boolean					mConnecting				= false;

	private int						mAction					= ACTION_NONE;

	private boolean					mIsCreataPreview		= false;

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
		setContentView( R.layout.activity_quick_pairing_sample );

		mConnecting = false;

		// View
		mNfcImage			= (ImageView)findViewById( R.id.QP_img_NFC );

		mPrintButton		= (Button)findViewById( R.id.QP_btn_Print );
		mNfcSettingsLayout	= (LinearLayout)findViewById( R.id.quickPairing_nfc_settings_LinearLayout );
		mNfcSettingsText	= (TextView)findViewById( R.id.QP_msg_NfcInformation );
		mNfcSettingsButton	= (Button)findViewById( R.id.QP_btn_NfcSetting );

		mConnectingText		= (TextView)findViewById( R.id.QP_msg_Information );

		mPrintButton.setEnabled( false );

		// Button
		registClickListener();

		setSelectPrinterInfo( "", -1, "" );

		mEasySelect = new EasySelect();

		mWifiController			= new WiFiController( this );
		mBluetoothController	= new BluetoothController();

		mBroadcastManager = new BroadcastManager();

		// initialize NFC & Camera
		initNfc();
		initCamera();
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

		if (false != mNfcCtr.isSuport()) {
			if (false != mNfcCtr.isEnabled()) {
				mNfcSettingsLayout.setVisibility( View.INVISIBLE );

				mNfcCtr.enableDispatch( this, true );
				mNfcImage.setVisibility( View.VISIBLE );

			} else {
				// disable NFC
				mNfcSettingsText.setText( getString( R.string.QP_msg_NfcDisabled ) );
				mNfcSettingsButton.setEnabled( true );
				mNfcSettingsLayout.setVisibility( View.VISIBLE );
				mNfcImage.setVisibility( View.INVISIBLE );
			}
		}

		if (mOpenedPrinter) {
			connectPrinter( mEasySelectInfo.printerName, mEasySelectInfo.deviceType, mEasySelectInfo.macAddress );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onPause
	 *
	 */
	@Override
	protected void onPause()
	{
		if (false != mNfcCtr.isSuport()) {
			mNfcCtr.enableDispatch( this, false );
		}

		if (null != mOpenThread) {
			mOpenThread.interrupt();
		}

		if (mOpenedPrinter) {
			mOpenedPrinter = false;
		}

		super.onPause();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onStop
	 *
	 */
	@Override
	protected void onStop()
	{
		mPrintButton.setEnabled( false );
		mConnectingText.setVisibility( View.INVISIBLE );

		setSelectPrinterInfo( "", -1, "" );

		waiteScan( false );

		super.onStop();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onDestroy
	 *
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		releaseCameraPreview();
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
		case R.id.QP_btn_NfcSetting:
			runNfcSetting();
			break;

		case R.id.QP_btn_Print:
			runPrintSample();
			break;

		default:
			break;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onWindowFocusChanged
	 *
	 *	@param	hasFocus
	 *
	 */
    @Override
	public void onWindowFocusChanged(
			boolean	hasFocus )
	{
		super.onWindowFocusChanged( hasFocus );

		if (null == mCameraCtr) {
			return ;
		}

		if (false == mCameraCtr.isSuport()) {
			return ;
		}

		if (hasFocus) {
			if (false == mIsCreataPreview) {
				createCameraPreview();
				mIsCreataPreview = true;
			} else {
				mCameraCtr.startCameraPreview();
				mCameraCtr.waiteScanPreview( false );
			}

		} else {
			mCameraCtr.waiteScanPreview( true );
			mCameraCtr.stopCameraPreview();
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onKeyDown
	 *
	 *	@param	keyCode
	 *	@param	event
	 *
	 */
	@Override
	public boolean onKeyDown(
			int			keyCode,
			KeyEvent	event )
	{
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			releaseCameraPreview();
		}

		return super.onKeyDown( keyCode, event );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onNewIntent
	 *
	 *	@param	intent
	 *
	 */
	@Override
	public void onNewIntent(
			Intent	intent )
	{
		// event frow : onPause -> onNewIntent -> onResume

		// read NFC, stop preview scan
		Tag tag = mNfcCtr.scanNfc( intent );
		if (null == tag) {
			// not NFC tag
			waiteScan( false );
			return;
		}

		// stop the scan of the camera / NFC.
		waiteScan( true );

		// parse NFC Tag
		ArrayList<EasySelectInfo> easySelectInfoList = mEasySelect.parseNFC( tag, EasySelect.PARSE_NFC_TIMEOUT_DEFAULT );
		if( (null != easySelectInfoList) && (easySelectInfoList.size() > 0) ) {
		    mEasySelectInfo = easySelectInfoList.get(0);
		} else {
		    mEasySelectInfo = null;
		}

		if (null != mEasySelectInfo) {
		    if( (null == mEasySelectInfo.printerName) || mEasySelectInfo.printerName.equals("") ) {
		        // Please specify the printer name of the use printers.
		        mEasySelectInfo.printerName = "TM-T88V";
		    }

		    if( (null == mEasySelectInfo.macAddress) || mEasySelectInfo.macAddress.equals("") ) {
		        // When a communication error occurred, macAddress is empty string.
		        mEasySelectInfo = null;
				waiteScan( false );
				return;
		    }

			connectPrinter(	mEasySelectInfo.printerName,
							mEasySelectInfo.deviceType,
							mEasySelectInfo.macAddress );

		} else {
			// other NFC

			waiteScan( false );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onTouchEvent
	 *
	 *	@param	event
	 *
	 */
	@Override
	public boolean onTouchEvent(
			MotionEvent event )
	{
		if (MotionEvent.ACTION_DOWN == event.getAction()) {
			if (null != mCameraCtr) {
				// If does not support the AF, the focus is set by the tap.
				mCameraCtr.setFocus();
			}
		}

		return super.onTouchEvent( event );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	BloaccastReceiver onReceive
	 *
	 *	@param	context
	 *	@param	intent
	 *
	 */
	@Override
	public void broadcastCallback(
			Context	context,
			Intent	intent )
	{
		if (false != intent.getAction().equals( WifiManager.WIFI_STATE_CHANGED_ACTION )) {
			int status	= intent.getIntExtra( WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN );

			// enable Wi-Fi
			if (WifiManager.WIFI_STATE_ENABLED == status) {
				mBroadcastManager.unregisterFilter( this );
				mBroadcastManager.unregistCallback();

				// example waite wi-fi connect
 				// if changed enabled, can not connect immediately.
 				try {
					Thread.sleep( WAITE_WIFI_SCAN );
				} catch (InterruptedException e) {
					// nothing
				}

				if (ACTION_CONNECT == mAction) {
					connectPrinter(	mEasySelectInfo.printerName,
									mEasySelectInfo.deviceType,
									mEasySelectInfo.macAddress );

				} else if (ACTION_PRINT == mAction) {
					printDemo();
				} else {
					// nothing
				}

				mAction = ACTION_NONE;
			}

		} else if (false != intent.getAction().equals( BluetoothAdapter.ACTION_STATE_CHANGED )) {
			int status	= intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF );

			// enable Bluetooth
			if (BluetoothAdapter.STATE_ON == status) {
				mBroadcastManager.unregisterFilter( this );
				mBroadcastManager.unregistCallback();

				if (ACTION_CONNECT == mAction) {
					connectPrinter(	mEasySelectInfo.printerName,
									mEasySelectInfo.deviceType,
									mEasySelectInfo.macAddress );

				} else if (ACTION_PRINT == mAction) {
					printDemo();

				} else {
					// nothing
				}

				mAction = ACTION_NONE;
			}
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
			showErrorMessage( getString(R.string.QP_msg_PrintError) );
		}

		mConnecting = false;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	regist ClickListener
	 *
	 */
	private void registClickListener()
	{
		int[] clickTarget = {
			R.id.QP_btn_NfcSetting,
			R.id.QP_btn_Print
		};

		for (int target : clickTarget) {
			Button button = (Button) findViewById( target );

			button.setOnClickListener( this );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	NFC setting
	 *
	 */
	private void runNfcSetting()
	{
		if (false != mNfcCtr.isSuport()) {
			mNfcCtr.showNfcSetting( QuickPairingActivity.this );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	print samepl
	 *
	 */
	private void runPrintSample()
	{
		printDemo();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	initalize NFC
	 *
	 */
	private void initNfc()
	{
		mNfcCtr = new NFCController( this );

		if (false == mNfcCtr.isSuport()) {
			// not support NFC
			mNfcSettingsText.setText( getString( R.string.QP_msg_NfcNoSupport ) );

			mNfcSettingsButton.setVisibility( View.INVISIBLE );
			mNfcSettingsLayout.setVisibility( View.VISIBLE );
			mNfcImage.setVisibility( View.INVISIBLE );

			return ;
		}

		PendingIntent	pendingIntent	= null;
		Intent			intent			= new Intent( this, this.getClass() ).setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP );

		pendingIntent = PendingIntent.getActivity( this, 0, intent, 0 );

		IntentFilter	actionNdef	= IntentFilter.create( NfcAdapter.ACTION_NDEF_DISCOVERED, "*/*" );
		IntentFilter[]	filters		=  new IntentFilter[] { actionNdef };

		mNfcCtr.setPendingIntent( pendingIntent );
		mNfcCtr.setNfcfilter( filters );
		mNfcCtr.setTechLists( null );	// does not limit the type of tag
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	initialize Camera
	 *
	 */
	private void initCamera()
	{
		mCameraCtr = new CameraController( this );

		if (false == mCameraCtr.isSuport()) {
			// this device is not support camera
			return ;
		}

		mCameraCtr.setCameraPreviewCallback( this );

		// analyze barcdode
		mBarcodeManager = new BarcodeManager();

		BarcodeManager.BARCODE_TYPE hints[] ={ BarcodeManager.BARCODE_TYPE.QR_CODE };	// only qr code

		mBarcodeManager.setDecodHints( hints );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	start camera previe
	 *
	 */
	public void createCameraPreview()
	{
		// If you do not a layout after the final, you can not get the size.
		// ex.match_parent
		FrameLayout frame = (FrameLayout) findViewById( R.id.QP_Layout_CameraPreview );

		mCameraCtr.startPreview( frame );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	stop camera preview
	 *
	 */
	public void releaseCameraPreview()
	{
		mCameraCtr.stopPreview();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	cameraPreviewCallback
	 *
	 *	@param	data picture of camera
	 *	@param	previewSize preview size
	 *	@param	frameWidth frame width
	 *	@param	frameHeigth frame height
	 *	@param	offsetX ofset of frame X
	 *	@param	offsetY ofset of frame Y
	 *
	 *	@return	boolean result
	 *
	 */
	public boolean cameraPreviewCallback(
			byte[]	data,
			Point	previewSize,
			int		frameWidth,
			int		frameHeigth,
			int		offsetX,
			int		offsetY )
	{
		boolean	result	= false;

		if (null == data) {
			return false;
		}

		// decode
		result = mBarcodeManager.decode( data, previewSize, frameWidth, frameHeigth, offsetX, offsetY );
		if (false == result) {
			return false;
		}

		// parse QR code
		mEasySelectInfo = mEasySelect.parseQR( mBarcodeManager.getStringResult() );

		if (null != mEasySelectInfo) {
			mCameraCtr.setFrameColor( Color.GREEN );

			connectPrinter(	mEasySelectInfo.printerName,
							mEasySelectInfo.deviceType,
							mEasySelectInfo.macAddress );

			// waite preview
			result = true;

		} else {
			// not support QR Code
			result = false;
		}

		return result;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	stop scan NFC / camera
	 *
	 *	@param	waite	true:stop	false:restart
	 *
	 */
	private void waiteScan(
			boolean	waite )
	{
		// stop the scan of the camera / NFC.
		// NFC
		mNfcCtr.waiteScanNfc( waite );

		// camera
		mCameraCtr.waiteScanPreview( waite );
		if (false == waite) {
			mCameraCtr.setFrameColor( Color.RED );
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	connect printer
	 *
	 *	@param	printerName
	 *	@param	interfaceType
	 *	@param	address
	 *
	 */
	 private void connectPrinter(
	 		String	printerName,
	 		int		interfaceType,
	 		String	address )
	 {
		mPrintButton.setEnabled( false );

		if (false == checkInterface( mEasySelectInfo.deviceType )) {
			mAction = ACTION_CONNECT;
			return ;
		}

		if (false != mConnecting) {
			return ;
		}

		mConnecting = true;

		// select printer info
		setSelectPrinterInfo(	mEasySelectInfo.printerName,
								mEasySelectInfo.deviceType,
								mEasySelectInfo.macAddress );

		openSelectPrinter(	mEasySelectInfo.deviceType,
							mEasySelectInfo.macAddress );

		mOpenedPrinter = true;
	 }

	// --------------------------------------------------------------------------------------------
	/**
	 *	print demo
	 *
	 */
	private void printDemo()
	{
		if (false == checkInterface( mEasySelectInfo.deviceType )) {
			mAction = ACTION_PRINT;
			return ;
		}

		mConnecting = true;

		PrintSampleAsyncTask task = new PrintSampleAsyncTask(	QuickPairingActivity.this,
																mEasySelectInfo.printerName,
																mEasySelectInfo.deviceType,
																mEasySelectInfo.macAddress );

		try {
			task.setCallback( QuickPairingActivity.this );
			task.execute();

		} catch (IllegalStateException e) {
			// nothing
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	Set select printer information
	 *
	 *	@param	printerName	Printer name
	 *	@param	interfaceType	Interface type
	 *	@param	macAddress	MAC address
	 *
	 */
	private void setSelectPrinterInfo(
			String	printerName,
			int		interfaceType,
			String	macAddress )
	{
		ListView							list		= (ListView)findViewById( R.id.QP_List_TargetInfo );
		SimpleAdapter						adapter		= null;
		ArrayList<HashMap<String, String>>	printerInfo	= new ArrayList<HashMap<String, String>>();
		String								label		= null;
		String								value		= null;
		HashMap<String, String>				item		= new HashMap<String, String>();

		adapter	= new SimpleAdapter( this, printerInfo, R.layout.listitem_horizontal_layout,
									new String[] { LI_LABEL, LI_VALUE },
									new int[] { R.id.listitem_hl_label, R.id.listitem_hl_value } );

		// printer name
		label = getString( R.string.QP_Item_PrinterInfo_PrinterName );
		item.put( LI_LABEL, label );
		item.put( LI_VALUE, printerName );
		printerInfo.add( item );

		// interface
		item = new HashMap<String, String>();

		label = getString( R.string.QP_Item_PrinterInfo_Interface );

		switch (interfaceType) {
		case Print.DEVTYPE_TCP:			value = getString( R.string.QP_Item_Interface_Network );		break;
		case Print.DEVTYPE_BLUETOOTH:	value = getString( R.string.QP_Item_Interface_Bluetooth );	break;
		default:						value = "";													break; }

		item.put( LI_LABEL, label );
		item.put( LI_VALUE, value );

		printerInfo.add( item );

		// mac address
		item = new HashMap<String, String>();

		label = getString( R.string.QP_Item_PrinterInfo_MacAddress );
		item.put( LI_LABEL, label );
		item.put( LI_VALUE, macAddress );
		printerInfo.add( item );

		list.setAdapter( adapter );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	open printer
	 *
	 *	@param	interfaceType
	 *	@param	address
	 *
	 */
	 private void openSelectPrinter(
			int				interfaceType,
			final String	address )
	{
		mPrintButton.setEnabled( false );
		mConnectingText.setVisibility( View.VISIBLE );

		if ((null == mOpenThread) || !mOpenThread.isAlive()) {
			mOpenThread = new Thread( new Runnable()
			{
				// --------------------------------------------------------------------------------
				@Override
				public void run()
				{
					ComPrint comPrint = new ComPrint();

					final int retval = comPrint.open( mEasySelectInfo.deviceType, address, 0, 1000 );

					if (ComEpsonIo.RESULT_SUCCESS == retval) {
						mOpenedPrinter = true;

					} else {
						showErrorMessage( getString( R.string.QP_msg_ErrorPrinterOpen ) );
					}

					runOnUiThread( new Runnable()
					{
						// --------------------------------------------------------------------
						@Override
						public void run()
						{
							if (ComEpsonIo.RESULT_SUCCESS == retval) {
								mPrintButton.setEnabled( true );
								mConnecting = false;
							} else {
								mConnecting = false;
							}

							mConnectingText.setVisibility( View.INVISIBLE );
						}
					});

					//
					// If you want to print immediately, send print command here.
					//

					comPrint.close();

					// restart NFC / Camera preview
					waiteScan( false );
				}
			});

			mOpenThread.start();

		} else {
    		showErrorMessage( getString( R.string.QP_msg_ErrorPrinterOpen ) );
			mConnecting = false;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	check interface
	 *
	 *	@param	interfaceType
	 *
	 *	@return	boolean
	 *
	 */
	private boolean checkInterface(
			int	interfaceType )
	{
		boolean	result	= true;

		if (Print.DEVTYPE_TCP == interfaceType) {
			if (false == mWifiController.isSuport()) {
				result = false;

			} else {
				// Wi-Fi
				if (false == mWifiController.isEnabled()) {
					msgWiFiEnabled();
					result = false;
				}
			}
		}

		if (Print.DEVTYPE_BLUETOOTH == interfaceType) {
			if (false == mBluetoothController.isSuport()) {
				result = false;

			} else {
				// Bluetooth
				if (false == mBluetoothController.isEnabled()) {
					msgBluetoothEnabled();
					result = false;
				}
			}
		}

		return result;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	message box enable Wi-Fi
	 *
	 *	@return	void
	 *
	 */
	 private void msgWiFiEnabled()
	 {
		MessageBox	msgBox = new MessageBox( this )
		{
			// ------------------------------------------------------------------------------------
			@Override
			protected void onButtonClick(
					DialogInterface	dialog,
					int				which )
			{
				switch (which) {
				case (DialogInterface.BUTTON_POSITIVE):
					// enable Wi-Fi
					IntentFilter	broadcastFilter = null;

					broadcastFilter = new IntentFilter();
					for (IntentListWiFi i : IntentListWiFi.values()) {
						broadcastFilter.addAction( i.getAction() );
					}

					mBroadcastManager.registFilter( QuickPairingActivity.this, broadcastFilter );
					mBroadcastManager.registCallback( QuickPairingActivity.this );

					mWifiController.setEnabled( true );
					break;

				default:
					waiteScan( false );
					break;
				}
			}
		};

		msgBox.intMessageBox(	null,
								getString( R.string.CP_Msg_TurnOnWiFi ),
								getString( R.string.dialog_btn_yes ),
								getString( R.string.dialog_btn_no ),
								null );
		msgBox.show();
	 }

	// --------------------------------------------------------------------------------------------
	/**
	 *	message box enable Bluetooth
	 *
	 *	@return	void
	 *
	 */
	 private void msgBluetoothEnabled()
	 {
		MessageBox	msgBox = new MessageBox( this )
		{
			// ------------------------------------------------------------------------------------
			@Override
			protected void onButtonClick(
					DialogInterface	dialog,
					int				which )
			{
				switch (which) {
				case (DialogInterface.BUTTON_POSITIVE):
					// enable Bluetooth
					IntentFilter	broadcastFilter	= null;

					broadcastFilter = new IntentFilter();
					for (IntentListBluetooth i : IntentListBluetooth.values()) {
						broadcastFilter.addAction( i.getAction() );
					}

					mBroadcastManager.registFilter( QuickPairingActivity.this, broadcastFilter );
					mBroadcastManager.registCallback( QuickPairingActivity.this );

					mBluetoothController.setEnabled( null, true );
					break;

				default:
					waiteScan( false );
					break;
				}
			}
		};

		msgBox.intMessageBox(	null,
								getString( R.string.CP_Msg_TurnOnBluetooth ),
								getString( R.string.dialog_btn_yes ),
								getString( R.string.dialog_btn_no ),
								null );
		msgBox.show();
	 }

	// --------------------------------------------------------------------------------------------
	/**
	 *	show error message
	 *
	 *	@param	message	error message
	 *
	 *	@return	void
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
				MessageBox	msgBox = new MessageBox( QuickPairingActivity.this )
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
