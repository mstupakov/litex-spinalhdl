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

import test._

class SuperTop(FRQ_IN: HertzNumber = 10 MHz) extends Component {
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
  }

  val sys_cd = ClockDomain(io.clk_25mhz, io.rst, frequency = FixedFrequency(FRQ_IN))
  ClockDomain.push(sys_cd)

  new Area {
    new SlowArea(2 Hz) {
      val r = RegInit(True)
      r := !r

      io.leds := (default -> r)
    }
  }

  io.leds(0) := io.btns(3)
  io.uart_tx := io.uart_rx
}

object MyTopLevelVerilog {
  def main(args: Array[String]) : Unit = {
    println("...........SpinalHDL Starting")

    new SpinalConfig(
      mode            = Verilog,
      device          = Device(vendor = "lattice"),
      targetDirectory = "output/",
      defaultClockDomainFrequency = FixedFrequency(25 MHz)
    ).generate(InOutWrapper(new SuperTop(25 MHz))).printPruned()

    println("...........End")
  }
}
