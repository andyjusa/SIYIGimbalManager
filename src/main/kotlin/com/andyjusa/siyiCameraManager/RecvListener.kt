package com.andyjusa.siyiCameraManager

interface RecvListener {
    fun onGetFirmwareVersion(board: FirmwareVersion, gimbal: FirmwareVersion, zoom: FirmwareVersion)
    fun onGetHardwareID()//TODO
    fun onAutoFocus(success:Boolean)
    fun onManualZoom(zoom: Double)
    fun onManualFocus(success: Boolean)
    fun onGimbalRotate(success: Boolean)
    fun onGimbalCenter(success: Boolean)
    fun onGetGimbalConfig(hdr:Boolean, record: RecordType, gimbalMode: GimbalMode, gimbalMounting: GimbalMounting, video: VideoConnectType)
    fun onFeedback(feedBack: FeedBack)
    fun onPhotoOrVideo()
    fun onGetGimbalAttitude(attitude: GimbalAttitude, velocity: GimbalAttitude)
    fun onSetGimbalAngle(attitude: GimbalAttitude)
    fun onSetAbsoluteZoom(success:Boolean)
    fun onGetCameraImageType(videoMode: VideoMode)
    fun onSetCameraImageType(videoMode: VideoMode)
    fun onReadTemp(temp:Double,x:Int,y:Int)
    fun onReadTempInBox(start: Pos, end: Pos, tempMax:Double, tempMin:Double, maxPos: Pos, minPos: Pos)
    fun onReadTempInScreen(tempMax:Double, tempMin:Double, maxPos: Pos, minPos: Pos)
    fun onReadLaser(range:Double)
}