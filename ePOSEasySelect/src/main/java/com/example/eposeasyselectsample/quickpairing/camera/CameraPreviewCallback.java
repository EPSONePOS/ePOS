package com.example.eposeasyselectsample.quickpairing.camera;

import android.graphics.Point;

// ------------------------------------------------------------------------------------------------
public interface CameraPreviewCallback
{
	// --------------------------------------------------------------------------------------------
	/**
	 *	camera preview callback
	 *
	 *	@param	data
	 *	@param	previewSize
	 *	@param	frameWidth
	 *	@param	frameHeigth
	 *	@param	offsetX
	 *	@param	offsetY
	 *
	 *	@return	boolean	true:stop preview false:restart preview
	 *
	 */
	public boolean cameraPreviewCallback(
			byte[]	data,
			Point	previewSize,
			int		frameWidth,
			int		frameHeigth,
			int		offsetX,
			int		offsetY );
}
