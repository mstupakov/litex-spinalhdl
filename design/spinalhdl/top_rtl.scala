package mylib

import scala.util.Random

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._
import spinal.lib.com.uart._
import spinal.lib.misc.BinTools
import spinal.lib.io.TriStateArray
import spinal.lib.io._
import spinal.lib.bus.misc._
import spinal.lib.bus.wishbone._
import spinal.lib.com.uart._

import test._

class MyTest() extends Component {
  val io = new Bundle {
    val gpio = master(TriStateArray(8 bits)) //addTag(noLatchCheck)


  }
}

class WishboneGpio(config : WishboneConfig, offset : Int, gpioWidth : Int)
  extends Component {

  val io = new Bundle{
    val wb = slave(Wishbone(config))
    val gpio = master(TriStateArray(gpioWidth bits))
  }

  val ctrl = WishboneSlaveFactory(io.wb)

  ctrl.read(io.gpio.read, offset + 0)
  ctrl.driveAndRead(io.gpio.write, offset + 4)
  ctrl.driveAndRead(io.gpio.writeEnable, offset + 8)
  io.gpio.writeEnable.getDrivingReg init(0)
}

class SuperSpinalTop(FRQ_IN: HertzNumber = 10 MHz) extends Component {
  noIoPrefix()

  val io = new Bundle {
    val clk_25mhz = in Bool()
    val rst       = in Bool()

    val clk_5MHz  = in Bool()
    val clk_10MHz = in Bool()
    val clk_20MHz = in Bool()

    val btns = in  Bits(8 bits)
    val leds = out Bits(8 bits)

    val uart_rx = in  Bool()
    val uart_tx = out Bool()

    //val my_uart = master(Uart())

    val gpio = master(TriStateArray(8 bits)) //addTag(noLatchCheck)
    val ttt = out(Bool())
    val yyy = out(Bits(8 bits))

    val ooo = out Bool() setAsReg() init(False)
  }

  val sys_cd = ClockDomain(io.clk_25mhz, io.rst, frequency = FixedFrequency(FRQ_IN))
  ClockDomain.push(sys_cd)

  io.ttt := RegNext(Reg(Bool()) init (False))
  io.yyy := RegNext(Reg(Bits(8 bits)) init (0))


//  val qq = Reg(TriStateArray(8 bits))
//  io.gpio := qq
//
//
//  qq.write init(0)
//  qq.writeEnable init(0)


//  val ww = RegNext(qq.read)



  //val cc = InOutWrapper(new MyTest())
  //io.gpio := RegNext(Reg(TriStateArray(8 bits)) init (U"0".asBits.resized))
  //io.yyy := RegNext(B"8'x0")
  //io.ttt := RegNext(False)
  //io.ttt := RegNext(B"8'x00")
  //io.ttt := RegNext(B"8'x00")
  //io.gpio := RegNext()
  //val gpioo = RegNext(gpio)


  new Area {
    new SlowArea(2 Hz) {
      val r = RegInit(True)
      r := !r

      //io.leds(3 downto 0) := (default -> r)
    }
  }

  //io.leds(0) := io.btns(3)
  //io.uart_tx := io.uart_rx

  val fsm = new StateMachine {
    val stateA, stateB, stateC, stateD = new State
    val stateE = new StateDelay((ClockDomain.current.frequency.getValue * 5).toInt)

    setEntry(stateA)

    val a = RegInit(False)
    val b = RegInit(False)
    val c = RegInit(False)
    val d = RegInit(False)
    val e = RegInit(False)

    //io.leds(7) := a
    //io.leds(6) := b
    //io.leds(5) := c
    //io.leds(4) := d
    //io.leds(3) := e

    a := isActive(stateA)
    b := isActive(stateB)
    c := isActive(stateC)
    d := isActive(stateD)
    e := isActive(stateE)

    stateA.whenIsActive(
      when (io.btns(1).rise) {
        goto(stateB)
      } otherwise {
  //      goto(stateA)
      }
    )

    stateB.whenIsActive(
      when (io.btns(2).rise) {
        goto(stateC)
      } otherwise {
  //      goto(stateA)
      }
    )

    stateC.whenIsActive(
      when (io.btns(3).rise) {
        goto(stateD)
      } otherwise {
//        goto(stateA)
      }
    )

    stateD.whenIsActive(
      when (io.btns(4).rise) {
        goto(stateE)
      } otherwise {
//        goto(stateA)
      }
    )

    stateE.whenCompleted {
      goto(stateA)
    }
  }

  //io.gpio.setAllBits

//  val qq = Reg(TriStateArray(8 bits))
//  io.gpio := qq
//
//
//  qq.write init(0)
//  qq.writeEnable init(0)

/*
  val TT = new Area {

//class WishboneGpio(config : WishboneConfig, offset : Int, gpioWidth : Int) extends Component{
    //val wb = slave(Wishbone(config))
    //val gpio = master(TriStateArray(gpioWidth bits))

    val wbGpio = new WishboneGpio(WishboneConfig(8, 8), 0, 8)

    val gpio = Reg(TriStateArray(8 bits))
//    io.gpio := gpio
//
//    gpio.write init(0)
//    gpio.writeEnable init(0)


    //val gpio = master(TriStateArray(8 bits))
//    val gpio = TriStateArray(8 bits)
    //val wb_gpio = WishboneGpio(WishboneConfig(8, 8), 8)

    val wb = Reg(Wishbone(WishboneConfig(8,8)))
    wbGpio.io.wb := wb

    val ctrl = WishboneSlaveFactory(wb)

    val abc = Reg(Bits(8 bits)) init(0)
    val rr1 = Reg(Bits(8 bits)) init(0)

    val flag = RegInit(False)

    ctrl.read(gpio.read, 0x04)
    ctrl.driveAndRead(gpio.write, 0x08)
    ctrl.driveAndRead(gpio.writeEnable, 0x10)

    gpio.writeEnable.getDrivingReg init(0)


    ctrl.read(abc, 0x00)
    ctrl.driveAndRead(rr1, 0x14)
    ctrl.driveAndRead(Bool(), 0x20)

    ctrl.onWrite(0x14) {
      flag := ~flag
    }

    //ctrl.build()

  }
  */

  val BB = new Area {
    val uart = Uart()

    val uartCtrlConfig = UartCtrlInitConfig(
      baudrate = 115200,
      dataLength = 7,  // 8 bits
      parity = UartParityType.NONE,
      stop = UartStopType.ONE
    )


    val uartCtrl = UartCtrl(uartCtrlConfig)

    val uartStream = Reg(Stream(Bits(8 bits)))


    when (io.btns(3).rise(False)) {
      uartStream.valid := True
    } elsewhen (uartStream.ready) {
      uartStream.valid := False
    }


    uartStream.payload := B"8'x39"

    uartCtrl.io.write <> uartStream
    uartCtrl.io.uart <> uart

    val l = RegInit(U"8'x04")
    io.leds := l.asBits

    val queue = uartCtrl.io.read.queue(16)
    queue.ready := False

    when (queue.valid && io.btns(3).rise) {
      queue.ready := True
      l := queue.payload.asUInt
    }

    io.uart_tx <> uart.txd
    io.uart_rx <> uart.rxd
  }

  val PP = new Area {
    val wbCfg = new WishboneConfig(8, 8)
    val wbBus = Reg(Wishbone(wbCfg))

    wbBus.CYC      init(False)
    wbBus.STB      init(False)
    wbBus.WE       init(False)
    wbBus.ACK      init(False)
    wbBus.ADR      init(0)
    wbBus.DAT_MOSI init(0)
    wbBus.DAT_MISO init(0)

    //wbBus.asBits setAsReg() init(0)



    val wbGpio = new WishboneGpio(wbCfg, 0x00, 8)

    wbGpio.io.gpio <> io.gpio
    wbGpio.io.wb   <> wbBus


    val wbGpio1 = new WishboneGpio(wbCfg, 0x00, 8)
    val szMapping = SizeMapping(0x00, 0x04 Bytes)
    val wbIConn = new WishboneInterconFactory()

    //wbIConn.addSlave(wbBus, szMapping)
    //wbIConn.addSlave(wbGpio1.io.wb, szMapping)
    //wbIConn.addMaster(wbBus, List(szMapping))
  }

}

  /*
class WishboneGpio(config : WishboneConfig, offset : Int, gpioWidth : Int)
  extends Component{
  val io = new Bundle{
    val wb = slave(Wishbone(config))
    val gpio = master(TriStateArray(gpioWidth bits))
  }

  val ctrl = WishboneSlaveFactory(io.wb)

  ctrl.read(io.gpio.read, offset + 0)
  ctrl.driveAndRead(io.gpio.write, offset + 4)
  ctrl.driveAndRead(io.gpio.writeEnable, offset + 8)
  io.gpio.writeEnable.getDrivingReg init(0)
}
   * */

object MyTopLevelVerilog {
  def main(args: Array[String]) : Unit = {
    println("...........SpinalHDL Starting")


    //val wbconf = new WishboneConfig(16,32);
    //SpinalVerilog(new RgbMatrixControllerSpi(wbconf, false))


    new SpinalConfig(
      mode            = Verilog,
      device          = Device(vendor = "lattice"),
      targetDirectory = "output/",
      defaultClockDomainFrequency = FixedFrequency(25 MHz)
    //).generate(new SuperSpinalTop(25 MHz)).printPruned()
    ).generate(InOutWrapper(new SuperSpinalTop(25 MHz))).printPruned()
    //showRtl(new SuperSpinalTop)

  //  SpinalVerilog(InOutWrapper(new SuperSpinalTop(25 MHz)))
    println("...........End")
  }
}
