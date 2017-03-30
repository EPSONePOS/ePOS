package com.example.eposeasyselectsample.quickpairing.camera;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;


// ------------------------------------------------------------------------------------------------
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class CameraController
		implements	SurfaceHolder.Callback,
					Camera.PreviewCallback,
					Runnable
{
	// --------------------------------------------------------------------------------------------
	public static final int		ROTETE_0			= 0;
	public static final int		ROTETE_90			= 90;
	public static final int		ROTETE_180			= 180;
	public static final int		ROTETE_270			= 2700;

	// --------------------------------------------------------------------------------------------
	private static final int	MAX_PERVIEW_WIDTH		= 800;
	private static final int	MAX_PERVIEW_HEIGHT		= 600;

	private static final int	DEFAULT_SCAN_INTERVAL	= 350;

	private static final int	FRAME_WIDTH_RATE		= 4;

	// --------------------------------------------------------------------------------------------
	private Context		mContext		= null;
	private ViewGroup	mViewGroup		= null;
	private SurfaceView	mSurfaceView	= null;

	private Camera		mCamera			= null;

	private boolean		mCameraSupport	= false;
	private boolean		mAutoFocus		= false;
	private boolean		mCAF			= false;
	private boolean		mFlash			= false;

	private Point		mPreviewSize	= null;

	private boolean		mPreviewStart	= false;

	private Point		mFrameSize		= null;
	private Point		mFrameOffset	= null;

	private RectF		mFrameRect		= null;

	private int			mFrameColor		= Color.RED;

	private int			mRotation		= ROTETE_90;

	private Thread		mScanThread			= null;
	private boolean		mWaiteScanPreview	= true;
	private int			mScanInterval		= DEFAULT_SCAN_INTERVAL;

	private CameraPreviewCallback	mPreviewCallback	= null;

	private int			mMaxPreviewWidth	= MAX_PERVIEW_WIDTH;
	private int			mMaxPreviewHeight	= MAX_PERVIEW_HEIGHT;

	// --------------------------------------------------------------------------------------------
	/**
	 *	constructer
	 *
	 *	@param	context
	 *
	 */
	public CameraController(
			Context	context )
	{
		mContext = context;

		mCameraSupport	= checkFeature( mContext, PackageManager.FEATURE_CAMERA );	// back camera
		if (false == mCameraSupport) {
			mCameraSupport	= checkFeature( mContext, PackageManager.FEATURE_CAMERA_FRONT );	// front camera
		}

		mAutoFocus		= checkFeature( mContext, PackageManager.FEATURE_CAMERA_AUTOFOCUS );
		mFlash			= checkFeature( mContext, PackageManager.FEATURE_CAMERA_FLASH );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	surfaceCreated
	 *
	 *	@param	hasFocus
	 *
	 */
	@Override
	public void surfaceCreated(
			SurfaceHolder	holder )
	{
		if (false == mCameraSupport) {
			return ;
		}

		try {
			if (mCamera != null) {
				mCamera.setDisplayOrientation( mRotation );
				mCamera.setPreviewDisplay( holder );
			}

		} catch (IOException exception) {
			return ;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	surfaceChanged
	 *
	 *	@param	hasFocus
	 *	@param	format
	 *	@param	width
	 *	@param	height
	 *
	 */
	@Override
	public void surfaceChanged(
			SurfaceHolder	holder,
			int				format,
			int				width,
			int				height )
	{
		if (false == mCameraSupport) {
			return ;
		}

		if (null == mCamera) {
			return ;
		}

		if (null == mSurfaceView) {
			return ;
		}

		try {
			Camera.Parameters parameters = mCamera.getParameters();

			mCamera.stopPreview();

			// camera preview area
			int frameWidth = (mPreviewSize.x / FRAME_WIDTH_RATE) * 2;
			int offsetX = (mPreviewSize.x - frameWidth) / 2;
			int offsetY = (mPreviewSize.y - frameWidth) / 2;

			if ((ROTETE_90 == mRotation) || (ROTETE_90 == mRotation)) {
				parameters.setPreviewSize( mPreviewSize.y, mPreviewSize.x );

				offsetX = (mPreviewSize.y - frameWidth) / 2;
				offsetY = (mPreviewSize.x - frameWidth) / 2;

			} else {
				parameters.setPreviewSize( mPreviewSize.x, mPreviewSize.y );
			}

			mFrameSize = new Point( frameWidth, frameWidth );
			mFrameOffset = new Point( offsetX, offsetY );

			// preview frame
			frameWidth = (mSurfaceView.getWidth() / FRAME_WIDTH_RATE) * 2;
			offsetX = (mSurfaceView.getWidth() - frameWidth) / 2;
			offsetY = (mSurfaceView.getHeight() - frameWidth) / 2;

			mFrameRect = new RectF(	offsetX,
									offsetY,
									(offsetX + frameWidth),
									(offsetY + frameWidth) );

			// Auto forcusの
			if (false != mAutoFocus) {
				List<String> focusModeList = parameters.getSupportedFocusModes();

 				// CAF(confinuous auto focusing) setting
 				if (false != focusModeList.contains( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO )) {
					parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO );
					mCAF = true;

 				} else if (false != focusModeList.contains( Camera.Parameters.FOCUS_MODE_AUTO )) {
					parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );

				} else {
					// nothing
				}

				// zoome
				parameters.setZoom( (int)(parameters.getMaxZoom() * 0.3f) );

			} else {
				// not zoome
				parameters.setZoom( 1 );
			}

			// Flashの
			if (false != mFlash) {
				if (false != parameters.getSupportedFlashModes().contains( Camera.Parameters.FLASH_MODE_AUTO )) {
					parameters.setFlashMode( Camera.Parameters.FLASH_MODE_AUTO );
				}
			}

			mCamera.setParameters( parameters );
			mCamera.startPreview();

			setScanFrame();

		} catch (RuntimeException exception) {
			return ;
		}

		mScanThread = new Thread( this );
		mScanThread.start();

		mPreviewStart = true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	surfaceDestroyed
	 *
	 *	@param	holder
	 *
	 */
	@Override
	public void surfaceDestroyed(
		SurfaceHolder	holder )
	{
		mPreviewStart = false;

//		stopPreview();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	onPreviewFrame
	 *
	 *	@param	data
	 *	@param	camera
	 *
	 */
	@Override
	public void onPreviewFrame(
			byte[] data,
			Camera camera )
	{
		if (false == mCameraSupport) {
			return ;
		}

		// During processing, preview stop
		mWaiteScanPreview = true;

		try {
			if (null != mPreviewCallback) {
				boolean	ret = false;
				ret = mPreviewCallback.cameraPreviewCallback(	data,
																mPreviewSize,
																mFrameSize.x,
																mFrameSize.y,
																mFrameOffset.x,
																mFrameOffset.y );

				if (false == ret) {
					mWaiteScanPreview = false;
				}

			} else {
				mWaiteScanPreview = false;
			}

		} catch (Exception e) {
			mWaiteScanPreview = false;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	preview thread
	 *
	 */
	@Override
	public void run()
	{
		if (false == mCameraSupport) {
			return ;
		}

		while (true) {
			try {
				// During processing, preview stop
				if (false == mWaiteScanPreview) {
					if (null != mCamera) {
						mCamera.setOneShotPreviewCallback( this );
					}
				}

				Thread.sleep( mScanInterval );

			} catch (InterruptedException e) {
				return ;
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	check camra support
	 *
	 *	@return	boolean
	 *
	 */
	public boolean isSuport()
	{
		return mCameraSupport;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	check auto focus
	 *
	 *	@return	boolean
	 *
	 */
	public boolean isSuportAutoFocus()
	{
		return mAutoFocus;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	regist CameraPreviewCallback
	 *
	 */
	public void setCameraPreviewCallback(
			CameraPreviewCallback	cameraPreviewCallback )
	{
		mPreviewCallback = cameraPreviewCallback;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	previe scan interval
	 *
	 *	@param	interval
	 *
	 */
	public void setScanInterval(
			int	interval )
	{
		if (0 < interval) {
			mScanInterval = interval;
		} else {
			mScanInterval = DEFAULT_SCAN_INTERVAL;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	start camera
	 *
	 *	@return	boolean
	 *
	 */
	public boolean open()
	{
		if (null != mCamera) {
			close();
		}

		mCamera = getCamera();

		if (null == mCamera) {
			return false;
		}

		return true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	start camera preview
	 *
	 */
	public void startCameraPreview()
	{
		if (null != mCamera) {
			mCamera.startPreview();
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	stop camera preview
	 *
	 */
	public void stopCameraPreview()
	{
		if (null != mCamera) {
			mCamera.stopPreview();
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	close camera
	 *
	 */
	public void close()
	{
		mWaiteScanPreview = true;

		if (null != mCamera) {
			mCamera.cancelAutoFocus();
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	start preview
	 *
	 *	@param	viewGroup
	 *
	 */
	public boolean startPreview(
			ViewGroup viewGroup )
	{
		mFrameColor = Color.RED;

		if (false == mCameraSupport) {
			return false;
		}

		if (null == viewGroup) {
			return false;
		}

		if (null == mCamera) {
			mCamera = getCamera();
			if (null == mCamera) {
				return false;
			}
		}

		mViewGroup = viewGroup;
		mPreviewSize = getPreviewSize();

		// create SurfaceView
		mSurfaceView = createSufaceView( mViewGroup );
		if (null == mSurfaceView) {
			return false;
		}

		SurfaceHolder holder = mSurfaceView.getHolder();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// Preview of Android 3.0 and earlier
		    holder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
		}

		holder.addCallback( this );

		mWaiteScanPreview = false;

		return true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	stop preview
	 *
	 */
	@SuppressLint("NewApi")
	public void stopPreview()
	{
		mFrameColor = Color.RED;

		mWaiteScanPreview = true;

		mPreviewStart = false;

		if (false == mCameraSupport) {
			return ;
		}

		close();

		setScanFrameDrawable( null );

		try {
			if (null != mScanThread) {
				mScanThread.interrupt();
				mScanThread = null;
			}

			if (null != mViewGroup) {
				if (null != mSurfaceView) {
					mViewGroup.removeView( mSurfaceView );
					mSurfaceView = null;
					mViewGroup = null;
				}
			}

		} catch (Exception e) {
			mPreviewStart = false;
			return ;
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	waite scan
	 *
	 *	@param	waite
	 *
	 */
	public void waiteScanPreview(
			boolean waite )
	{
		mWaiteScanPreview = waite;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	set frame color
	 *
	 *	@param	color
	 *
	 */
	public void setFrameColor(
			int	color )
	{
		mFrameColor = color;

		if (null != mSurfaceView) {
			Activity activity = (Activity)mSurfaceView.getContext();

			if (null != activity) {
				activity.runOnUiThread( new Runnable()
				{
					// ----------------------------------------------------------------------------
					@Override
					public void run()
					{
						if (null != mSurfaceView) {
							mSurfaceView.invalidate();
						}
					}
				});
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	set focus
	 *
	 */
	public void setFocus()
	{
		if (false == mPreviewStart) {
			return ;
		}

		if (null != mCamera) {
			if ((false != mAutoFocus) && (false == mCAF)) {
				// not support CAF

				// only set forcus
				mCamera.autoFocus( null );
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get camera
	 *
	 *	@return	Camera
	 *
	 */
	private Camera getCamera()
	{
		Camera camera = null;

		try {
			int	numberOfCameras = Camera.getNumberOfCameras();

			for (int i = 0; i < numberOfCameras; i++) {
				CameraInfo	cameraInfo = new CameraInfo();

				Camera.getCameraInfo( i, cameraInfo );

				if ((null != cameraInfo) && (CameraInfo.CAMERA_FACING_BACK == cameraInfo.facing)) {
					camera = Camera.open( i );

					break;
				}
			}

			if ((null == camera) && (0 < numberOfCameras)) {
				camera = Camera.open( 0 );
			}

		} catch (Exception e) {
			e.printStackTrace();
			camera = null;
		}

		return camera;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get preview size
	 *
	 *	@return	Camera
	 *
	 */
	private Point getPreviewSize()
	{
		Camera.Parameters	cameraParams	= null;
		int					previewWidth	= 0;
		int					previewHeight	= 0;

		if (null == mCamera) {
			return null;
		}

		cameraParams = mCamera.getParameters();

		List<Camera.Size> supportedPreviewSizes = cameraParams.getSupportedPreviewSizes();
		if (0 >= supportedPreviewSizes.size()) {
			return null;
		}

		for (int i = 0; i < supportedPreviewSizes.size(); i++) {
			Camera.Size previewSize = supportedPreviewSizes.get( i );

			// check camera preview size
			if ((previewSize.width <= mMaxPreviewWidth)	&&
				(previewSize.height <= mMaxPreviewHeight)) {

				if ((previewWidth * previewHeight) < (previewSize.width * previewSize.height)) {
					previewWidth	= previewSize.width;
					previewHeight	= previewSize.height;
				}
			}
		}

		if ((0 == previewWidth) || (0 == previewHeight)) {
			previewWidth = supportedPreviewSizes.get( 0 ).width;
			previewHeight = supportedPreviewSizes.get( 0 ).height;
		}

		if ((ROTETE_90 == mRotation) || (ROTETE_90 == mRotation)) {
			int swap	= previewWidth;

			previewWidth = previewHeight;
			previewHeight = swap;
		}

		return new Point( previewWidth, previewHeight );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	create SurfaceView
	 *
	 *	@param	viewGroup
	 *
	 *	@return	SurfaceView
	 *
	 */
	private SurfaceView createSufaceView(
		ViewGroup viewGroup )
	{
		int	surfaceWidth	= 0;
		int	surfaceHeight	= 0;

		if (null == viewGroup) {
			return null;
		}

		if ((0 >= viewGroup.getWidth()) || (0 >= viewGroup.getHeight())) {
			return null;
		}

		// calc SurfaceView size
		if ((viewGroup.getWidth() * mPreviewSize.y) > (viewGroup.getHeight() * mPreviewSize.x)) {
			surfaceHeight = viewGroup.getHeight();
			surfaceWidth = (surfaceHeight * mPreviewSize.x) / mPreviewSize.y;

		} else {
			surfaceWidth = viewGroup.getWidth();
			surfaceHeight = (surfaceWidth * mPreviewSize.y) / mPreviewSize.x;
		}

		Context context = viewGroup.getContext();
		SurfaceView surfaceView = new SurfaceView( context );

		viewGroup.addView( surfaceView );

		ViewGroup.LayoutParams layoutParams = (LayoutParams)surfaceView.getLayoutParams();

		layoutParams.width = surfaceWidth;
		layoutParams.height = surfaceHeight;

		surfaceView.setLayoutParams( layoutParams );

		return surfaceView;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	draw frame setting
	 *
	 *	@param	drawable
	 *
	 */
	@SuppressLint("NewApi")
	private void setScanFrameDrawable(
			ShapeDrawable drawable )
	{
		if (null != mSurfaceView) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				mSurfaceView.setBackgroundDrawable( drawable );

			} else {
				mSurfaceView.setBackground( drawable );
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	draw frame setting
	 *
	 */
	private void setScanFrame()
	{
		Shape shape = new Shape()
		{
			// ------------------------------------------------------------------------------------
			@Override
			public void draw(
					Canvas	canvas,
					Paint	paint )
			{
				float strokeWidth = 5;	// line width

				if (null == canvas || null == paint) {
					return ;
				}

				if (false == mPreviewStart) {
					return ;
				}

				if (null == mSurfaceView) {
					return ;
				}

				paint.setStyle( Style.STROKE );
				paint.setStrokeWidth( strokeWidth );
				paint.setColor( mFrameColor );

				// doraw rect
				canvas.drawRect( mFrameRect, paint );
			}
		};

		ShapeDrawable shapeDrawable = new ShapeDrawable();
		shapeDrawable.setShape( shape );

		setScanFrameDrawable( shapeDrawable );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	function check
	 *
	 *	@param	context
	 *	@param	feature
	 *
	 */
	private boolean checkFeature(
			Context			context,
			final String	feature )
	{
		if (null == context) {
			return false;
		}

		PackageManager packageManager = context.getPackageManager();

		if (null == packageManager) {
			return false;
		}

		return packageManager.hasSystemFeature( feature );
	}
}
