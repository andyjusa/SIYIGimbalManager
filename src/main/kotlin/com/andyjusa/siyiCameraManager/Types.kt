@file:OptIn(ExperimentalUnsignedTypes::class)

package com.andyjusa.siyiCameraManager

open class TypeBase(val DataLen:UByte,val data:UByteArray)

class NullArgs : TypeBase(0u, ubyteArrayOf())
class Type0(a0:UByte) : TypeBase(1u, ubyteArrayOf(a0))
class Type1(a0:UByte,a1:UByte) : TypeBase(2u,ubyteArrayOf(a0, a1))
class Type2(a0:Short,a1:Short) : TypeBase(4u, shortToUBytes(a0) + shortToUBytes(a1))
class Type3(a0:UShort, a1:UShort, a2:UByte) : TypeBase(5u, uShortToUBytes(a0) + uShortToUBytes(a1) +a2)
class Type4(a0:UShort, a1:UShort, a2:UShort,  a3:UShort, a4:UByte) : TypeBase(9u,
    uShortToUBytes(a0) + uShortToUBytes(a1) + uShortToUBytes(a2) + uShortToUBytes(a3) +a4)
class Type5(a0:Byte) : TypeBase(1u,ubyteArrayOf(a0.toUByte()))
class Type6(a0:Byte,a1:Byte) : TypeBase(2u,ubyteArrayOf(a0.toUByte(),a1.toUByte()))
class FirmwareVersion(version:String){
    val major:Int
    val minor:Int
    val patch:Int
    init {
        major = version.substring(2..3).toInt(16)
        minor = version.substring(4..5).toInt(16)
        patch = version.substring(6..7).toInt(16)
    }
    override fun toString():String{
        return "$major.$minor.$patch"
    }
}
enum class RecordType(val i:UByte){
    ON(0u),
    OFF(1u),
    TFCardEmpty(2u),
    DataLoss(3u),
    ERROR(10u)
}
fun getRecordType(ii:UByte): RecordType = (RecordType.values().find { it.i==ii }?: RecordType.ERROR)
enum class GimbalMode(val i: UByte){
    LOCK(0u),
    FOLLOW(1u),
    FPV(2u),
    ERROR(10u)
}
fun getGimbalMode(ii:UByte): GimbalMode = (GimbalMode.values().find { it.i==ii }?: GimbalMode.ERROR)

//Gimbal Mounting Method
//0: Reserved
//1: Normal
//2: Upside Down

enum class GimbalMounting(val i:UByte)
{
    RESERVED(0u),
    NORMAL(1u),
    UPSIDEDOWN(2u),
    ERROR(10u)
}
fun getGimbalMounting(ii:UByte): GimbalMounting = (GimbalMounting.values().find { it.i==ii }?: GimbalMounting.ERROR)

enum class VideoConnectType{
    HDMI,
    CVBS
}

enum class FeedBack(val i:UByte){
    SUCCESS(0u),
    FAILPHOTO(1u),
    HDRON(2u),
    HDROFF(3u),
    FAILVIDEO(4u),
    ERROR(10u)
}
fun getFeedback(ii:UByte): FeedBack = (FeedBack.values().find { it.i==ii }?: FeedBack.ERROR)

class GimbalAttitude(val yaw:Short, val pitch:Short, val roll:Short)
//Camera Image Mode:
//0: Split Screen (Main: Zoom &
//Thermal. Sub: Wide Angle)
//1: Split Screen (Main: Wide Angle &
//Thermal. Sub: Zoom)
//2: Split Screen (Main: Zoom & Wide
//Angle. Sub: Thermal)
//3: Single Image (Main: Zoom. Sub:
//Thermal)
//4: Single Image (Main: Zoom. Sub:
//Wide Angle)
//5: Single Image (Main: Wide Angle.
//Sub: Thermal)
//6: Single Image (Main: Wide Angle.
//Sub: Zoom)
//7: Single Image (Main: Thermal. Sub:
//Zoom)
//8: Single Image (Main: Thermal. Sub:
//Wide Angle)
enum class VideoMode(val i:UByte, val mainVideo: VideoType, val subVideo: VideoType){
    ZERO(0u, VideoType.ZOOMnTHERMAL, VideoType.WIDE),
    ONE(1u, VideoType.WIDEnTHERMAL, VideoType.ZOOM),
    TWO(2u, VideoType.ZOOMnWIDE, VideoType.THERMAL),
    THREE(3u, VideoType.ZOOM, VideoType.THERMAL),
    FOUR(4u, VideoType.ZOOM, VideoType.WIDE),
    FIVE(5u, VideoType.WIDE, VideoType.THERMAL),
    SIX(6u, VideoType.WIDE, VideoType.ZOOM),
    SEVEN(7u, VideoType.THERMAL, VideoType.ZOOM),
    EIGHT(8u, VideoType.THERMAL, VideoType.WIDE),
    ERROR(10u, VideoType.WIDE, VideoType.WIDE)
}
fun getVideoMode(ii:UByte): VideoMode = (VideoMode.values().find { it.i==ii }?: VideoMode.ERROR)
fun getVideoMode(ii: VideoType, iii: VideoType): VideoMode = (VideoMode.values().find { it.mainVideo==ii&&it.subVideo==iii }?: VideoMode.ERROR)
enum class VideoType{
    WIDE,
    THERMAL,
    ZOOM,
    WIDEnTHERMAL,
    ZOOMnTHERMAL,
    ZOOMnWIDE
}
enum class TempFlag(val i:UByte)
{
    TURNOFF(0u),
    ONCE(1u),
    REPEAT(2u),
    ERROR(10u)
}
fun getTempFlag(ii:UByte): TempFlag = (TempFlag.values().find { it.i==ii }?: TempFlag.ERROR)
enum class ZoomType(val i:Byte){
    ZOOMIN(1),
    STOPZOOM(0),
    ZOOMOUT(-1)
}
enum class FocusType(val i:Byte){
    LONG(1),
    STOP(0),
    CLOSE(-1)
}

enum class CameraControl(val i:UByte)
{
    TAKEPICTURE(0u),
    TOGGLEHDR(1u),
    TOGGLERECOREDING(2u),
    LOCKMODE(3u),
    FOLLOWMODE(4u),
    FPVMODE(5u),
    HDMI(6u),
    CVBS(7u),
    OFF(8u)
}

class Pos(val x:Int,val y:Int)

fun shortToUBytes(short: Short) : UByteArray = ubyteArrayOf(((short * 256) / 256).toUByte(),(short / 256).toUByte())
fun uShortToUBytes(uShort: UShort) : UByteArray = ubyteArrayOf(((uShort * 256u) / 256u).toUByte(),(uShort / 256u).toUByte())
