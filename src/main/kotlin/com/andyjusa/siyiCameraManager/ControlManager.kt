package com.andyjusa.siyiCameraManager

import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.Socket
import java.net.SocketException
import java.util.Timer
import java.util.TimerTask
@OptIn(ExperimentalUnsignedTypes::class)
class ControlManager(val ip:String = "192.168.144.25",val port:Int = 37260) {
    private var sendData = mutableMapOf<UShort, ByteArray>()
    private var connected = false
    private var seq:UShort = 0u
    private var listener: RecvListener? = null
    private val cmd = Commands()
    private var input:InputStream? = null
    private var output:OutputStream? = null
    lateinit var sock:Socket

//    private val ip = "192.168.123.115"
//    private val port = 9999
    init {
        Thread {
            try {
                sock = Socket(ip, port).apply {
                    input = getInputStream()
                    output = getOutputStream()
                    connected = true
//                    Log.d("Hello","Connected")
                    while (true) {
                        recvData()?.let { it ->
//                            Log.d("Hello", it.toHex())
                            onRecvData(parse(it.toHex()))
                        }
                        break
                    }
                }
            }catch (_:ConnectException)
            {
                connected = false
            }
        }.start()
        val timer = Timer()
        val TT: TimerTask = object : TimerTask() {
            override fun run() {
                if(!connected){
                    connectionReset()
                }
            }
        }

        timer.schedule(TT, 0, 100) //Timer 실행

    }

//    @SuppressLint("SuspiciousIndentation")
    fun recvData():ByteArray?{
        try {
            val data = input?.readBytes()
            return if (data?.size!! > 3) {
                if (data.toHex().substring(14..15) == "00") {
                    reSend()
                } else {
//                CRC16().run {
//                    update(data.sliceArray(0..data.size - 3))
//                    if (uShortToUBytes(value).toByteArray().toHex() != data.sliceArray(data.size-2 until data.size).toHex())
//                    {
//                        sendData(cmd.resend,)
//                        return null
//                    }
//                }
                    data
                }
            } else {
                null
            }
        } catch (_:SocketException)
        {
            connected = false
            return null
        }
    }
    fun connectionReset(){
        try {
            sock = Socket(ip, port).apply {
                connected = true
//                Log.d("Hello","Connected")
                input = getInputStream()
                output = getOutputStream()
                seq = 0u
            }
        }catch (_:ConnectException){ connected = false }
    }
    // 0  1  2  3
    fun setOnListener(recvListener: RecvListener){ listener=recvListener }
    private fun reSend():ByteArray?
    {
        sendData[seq]?.let { output?.write(it) }
        return null
    }

//    fun <T> sendData(cmdBase: CmdBase<T>, data:T)
//    {
//        Thread {
//            var input:InputStream
//            var output:OutputStream
//            val sock = Socket("192.168.144.25", 37260).apply {
//                input = getInputStream()
//                output = getOutputStream()
//                connected = true
//                Thread {
//                    cmdBase.getByteArray(data, seq).toByteArray().let {
//                        output.write(it)
//                        sendData[localPort.toUShort()] = it
//                    }
//                }.start()
//            }
//            while (true) {
//                recvData(input,output,sock.localPort.toUShort())?.let { it ->
//                    Log.d("Hello",it.toHex())
//                    onRecvData(parse(it.toHex()))
//                    sock.close()
//                }
//                break
//            }
//        }.start()
//    }
    fun <T> sendData(cmdBase: CmdBase<T>, data:T) : Unit = sendData(cmdBase, data, seq++)
    fun <T> sendData(cmdBase: CmdBase<T>, data:T,seq:UShort)
    {
        var _seq = seq
        Thread {
            try {
                cmdBase.getByteArray(data, _seq).toByteArray().let {
                    output?.write(it)
                    sendData[_seq] = it
                }
            }catch (_:SocketException){
                connected = false
            }
        }.start()
    }
    private fun onRecvData(a: DataFormat){
        when(a.CMD_ID)
        {
            "01"-> listener?.onGetFirmwareVersion(
					FirmwareVersion(a.DATA.substring(0)),
					FirmwareVersion(a.DATA.substring(4)),
					FirmwareVersion(a.DATA.substring(8))
				)
            "02"->listener?.onGetHardwareID()//TODO
            "04"->listener?.onAutoFocus(a.ACK.datas[0] == 1)
            "05"->listener?.onManualZoom((a.ACK.datas[0] as UShort).toDouble()/10)
            "06"->listener?.onManualFocus(a.ACK.datas[0] == 1)
            "07"->listener?.onGimbalRotate(a.ACK.datas[0] == 1)
            "08"->listener?.onGimbalCenter(a.ACK.datas[0]==1)
            "0A"->listener?.onGetGimbalConfig(a.ACK.datas[1] == 1, getRecordType(a.ACK.datas[3] as UByte),
					getGimbalMode(a.ACK.datas[4] as UByte), getGimbalMounting(a.ACK.datas[5] as UByte),
                (if (a.ACK.datas[6] == 1u) VideoConnectType.CVBS else VideoConnectType.HDMI)
                )
            "0B"->listener?.onFeedback(getFeedback(a.ACK.datas[0] as UByte))
            "0C"->listener?.onPhotoOrVideo()
            "0D"->listener?.onGetGimbalAttitude(
					GimbalAttitude(a.ACK.datas[0] as Short, a.ACK.datas[1] as Short, a.ACK.datas[2] as Short),
					GimbalAttitude(a.ACK.datas[3] as Short, a.ACK.datas[4] as Short, a.ACK.datas[5] as Short)
				)
            "0E"->listener?.onSetGimbalAngle(
					GimbalAttitude(
						a.ACK.datas[0] as Short,
						a.ACK.datas[1] as Short,
						a.ACK.datas[2] as Short
					)
				)
            "0F"->listener?.onSetAbsoluteZoom(a.ACK.datas[0]==1u)
            "10"->listener?.onGetCameraImageType(getVideoMode(a.ACK.datas[0] as UByte))
            "11"->listener?.onSetCameraImageType(getVideoMode(a.ACK.datas[0] as UByte))
            "12"->listener?.onReadTemp((a.ACK.datas[0] as UShort).toDouble()/100,(a.ACK.datas[1] as UShort).toInt(),(a.ACK.datas[2] as UShort).toInt())
            "13"->listener?.onReadTempInBox(
					Pos((a.ACK.datas[0] as UShort).toInt(), (a.ACK.datas[1] as UShort).toInt()),
					Pos((a.ACK.datas[2] as UShort).toInt(), (a.ACK.datas[3] as UShort).toInt()),
                (a.ACK.datas[4] as UShort).toDouble()/100,(a.ACK.datas[5] as UShort).toDouble()/100,
					Pos((a.ACK.datas[6] as UShort).toInt(), (a.ACK.datas[7] as UShort).toInt()),
					Pos((a.ACK.datas[8] as UShort).toInt(), (a.ACK.datas[9] as UShort).toInt())
            )
            "14"->listener?.onReadTempInScreen((a.ACK.datas[0] as UShort).toDouble()/10,(a.ACK.datas[1] as UShort).toDouble()/10,
					Pos((a.ACK.datas[2] as UShort).toInt(), (a.ACK.datas[3] as UShort).toInt()),
					Pos((a.ACK.datas[4] as UShort).toInt(), (a.ACK.datas[5] as UShort).toInt())
				)
            "15"->listener?.onReadLaser((a.ACK.datas[0] as UShort).toDouble()/10)
        }
//        Log.d("Hello",a.toString())
    }

    fun getFirmwareVersion():Unit = sendData(cmd.firmwareVersion, NullArgs())
    fun getHardwareID():Unit = sendData(cmd.hardwareID, NullArgs())
    fun setAutoFocus(focus:Boolean):Unit = sendData(cmd.autoFocus, Type0(if (focus) 1u else 0u))
    fun setManualZoom(zoomType: ZoomType):Unit = sendData(cmd.manualZoomAndAutoFocus,
		 Type5(zoomType.i)
    )
    fun setAbsoluteZoom(zoom: Double): Unit = sendData(cmd.absoluteZoomAndAutoFocus,
		 Type1(zoom.toInt().toUByte(), ((zoom * 10) % 10).toInt().toUByte())
	 )
    fun getMaxZoomValue():Unit = sendData(cmd.acquireTheMaxZoomValue, NullArgs())
    fun setManualFocus(focus: FocusType):Unit = sendData(cmd.manualFocus, Type5(focus.i))
    fun setGimbalRotate(yaw:Byte,pitch:Byte):Unit = sendData(cmd.gimbalRotation, Type6(yaw, pitch))
    fun setToCenter(data:Boolean):Unit = sendData(cmd.center, Type0(if (data) 1u else 0u))
    fun getGimbalConfig():Unit = sendData(cmd.gimbalConfigurationInformation, NullArgs())
    fun getFeedback():Unit = sendData(cmd.functionFeedbackInformation, NullArgs())
    fun controlCamera(cameraControl: CameraControl):Unit = sendData(cmd.photoAndVideo,
		 Type0(cameraControl.i)
    )
    fun getGimbalAttitude():Unit = sendData(cmd.gimbalAttitude, NullArgs())
    fun setControlAngle(yaw: Short,pitch:Short):Unit = sendData(cmd.setGimbalControlAngle,
		 Type2(yaw, pitch)
    )
    fun getCameraImageType():Unit = sendData(cmd.cameraImageType, NullArgs())
    fun setCameraImageType(main: VideoType, sub: VideoType):Unit = sendData(cmd.setCameraImageType, Type0(
		 getVideoMode(main, sub).i
	 )
	 )
    fun getTempOfAPoint(pos: Pos, tempFlag: TempFlag):Unit = sendData(cmd.readTemperatureOfAPoint,
		 Type3(pos.x.toUShort(), pos.y.toUShort(), tempFlag.i)
    )
    fun getTempOfBox(start: Pos, end: Pos, tempFlag: TempFlag):Unit = sendData(cmd.readTemperatureOfABoxOnScreen,
		 Type4(start.x.toUShort(), start.y.toUShort(), end.x.toUShort(), end.y.toUShort(), tempFlag.i)
    )
    fun getTempOfScreen(tempFlag: TempFlag):Unit = sendData(cmd.readTemperatureOfTheFullScreen, Type0(tempFlag.i))
    fun getLaserRange():Unit = sendData(cmd.readRangeFromLaserRangefinder, NullArgs())
}
fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }