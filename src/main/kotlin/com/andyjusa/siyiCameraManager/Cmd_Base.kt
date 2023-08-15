package com.andyjusa.siyiCameraManager

import com.quickbirdstudios.CRC16

@OptIn(ExperimentalUnsignedTypes::class)
open class CmdBase<T>(val CmdID:UByte) {
    fun getByteArray(data:T,seq:UShort):UByteArray
    {
        var a = ubyteArrayOf(0x55u,0x66u,0x01u)
        a+=((data as TypeBase).DataLen)
        a+=0x00u
        a+=(ubyteArrayOf(((seq * 256u) / 256u).toUByte(),(seq / 256u).toUByte(),CmdID))
        a+=(data as TypeBase).data
        CRC16().run {
            update(a)
            a += uShortToUBytes(value)
        }
        return a
    }
}

class Commands {
    val resend = CmdBase<Type0>(0x00u)
    val firmwareVersion = CmdBase<NullArgs>(0x01u)
    val hardwareID = CmdBase<NullArgs>(0x02u)
    val autoFocus = CmdBase<Type0>(0x04u)
    val manualZoomAndAutoFocus = CmdBase<Type5>(0x05u)
    val absoluteZoomAndAutoFocus = CmdBase<Type1>(0x0Fu)
    val acquireTheMaxZoomValue = CmdBase<NullArgs>(0x16u)
    val manualFocus = CmdBase<Type5>(0x06u)
    val gimbalRotation = CmdBase<Type6>(0x07u)
    val center = CmdBase<Type0>(0x08u)
    val gimbalConfigurationInformation = CmdBase<NullArgs>(0x0Au)
    val functionFeedbackInformation = CmdBase<NullArgs>(0x0Bu)
    val photoAndVideo = CmdBase<Type0>(0x0Cu)
    val gimbalAttitude = CmdBase<NullArgs>(0x0Du)
    val setGimbalControlAngle = CmdBase<Type2>(0x0Eu)
    val cameraImageType = CmdBase<NullArgs>(0x10u)
    val setCameraImageType = CmdBase<Type0>(0x11u)
    val readTemperatureOfAPoint = CmdBase<Type3>(0x12u)
    val readTemperatureOfABoxOnScreen = CmdBase<Type4>(0x13u)
    val readTemperatureOfTheFullScreen = CmdBase<Type0>(0x14u)
    val readRangeFromLaserRangefinder = CmdBase<NullArgs>(0x15u)
    val maxZoomValue = CmdBase<NullArgs>(0x16u)
}

class DataFormat
{
    lateinit var STX:String
    lateinit var CTRL:String
    var DATA_LEN:UShort = 0u
    var SEQ:UShort = 0u
    lateinit var CMD_ID:String
    lateinit var DATA:String
    lateinit var CRC:String
    lateinit var ACK: Ack
}

fun parse(data:String): DataFormat
{
    return DataFormat().apply {
        STX = data.substring(0..3)
        CTRL = data.substring(4..5)
        DATA_LEN = (data.substring(8..9) + data.substring(6..7)).toUShort(16)
        SEQ = (data.substring(12..13) + data.substring(10..11)).toUShort(16)
        CMD_ID = data.substring(14..15)
        DATA = data.substring(16..(15 + (2 * DATA_LEN.toInt())))
        CRC = data.substring((16 + (2 * DATA_LEN.toInt())))
        ACK = AckList[CMD_ID]?.split(DATA)!!
    }
}

class k<aa>(val a:Int,val b:aa)

val UIntFun:(String) -> k<Any> = { k(8, it.substring(0 .. 7).toUInt(16)) }
val UByteFun:(String) -> k<Any> = { k(2, it.substring(0 .. 1).toUByte(16)) }
val UShortFun:(String) -> k<Any> = { k(4, it.substring(0 .. 3).toUShort(16)) }
val ShortFun:(String) -> k<Any> = { k(4, it.substring(0 .. 3).toShort(16)) }

val AckList = mutableMapOf(
    "00" to Ack(),
    "01" to Ack(UIntFun, UIntFun, UIntFun),
    "02" to Ack(UByteFun),
    "04" to Ack(UByteFun),
    "05" to Ack(UShortFun),
    "06" to Ack(UByteFun),
    "07" to Ack(UByteFun),
    "08" to Ack(UByteFun),
    "10" to Ack(UByteFun),
    "0A" to Ack(UByteFun, UByteFun, UByteFun, UByteFun, UByteFun, UByteFun, UByteFun),
    "0B" to Ack(UByteFun),
    "0C" to Ack(),
    "0D" to Ack(ShortFun, ShortFun, ShortFun, ShortFun, ShortFun, ShortFun),
    "0E" to Ack(ShortFun, ShortFun, ShortFun),
    "0F" to Ack(UByteFun),
    "11" to Ack(UByteFun),
    "12" to Ack(UShortFun, UShortFun, UShortFun),
    "13" to Ack(
		 UShortFun,
		 UShortFun,
		 UShortFun,
		 UShortFun,
		 UShortFun,
		 UShortFun,
		 UShortFun,
		 UShortFun,
		 UShortFun,
		 UShortFun
	 ),
    "14" to Ack(UShortFun, UShortFun, UShortFun, UShortFun, UShortFun, UShortFun),
    "15" to Ack(UShortFun),
    "16" to Ack(UByteFun, UByteFun)
)

class Ack (private vararg val arg: ((String) -> k<Any>))
{
    var datas = ArrayList<Any>()
    fun split(data: String): Ack
    {
        var a = 0
        arg.forEach {
            it(data.substring(a)).let {
                a+=it.a
                datas.add(it.b)
            }
        }
        return this
    }
}

