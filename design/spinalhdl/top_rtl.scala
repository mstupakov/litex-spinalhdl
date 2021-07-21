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
}

object MyTopLevelVerilog {
  def main(args: Array[String]) : Unit = {
    println("...........Begin")
    //System.exit(1)

    val TTT = new SpinalConfig(
//      defaultConfigForClockDomains = ClockDomainConfig(
//        resetKind = SYNC,
//        resetActiveLevel = LOW
//        ),
      defaultClockDomainFrequency = FixedFrequency(25 MHz),
      targetDirectory = "output/",
      mode=Verilog,
      rtlHeader="++++++++++",
      oneFilePerComponent=false,
      globalPrefix="",
      device=Device(vendor = "lattice"),
      //inlineRom=true,
      //mergeAsyncProcess=true,
      anonymSignalUniqueness=true,
      anonymSignalPrefix="sig"
    //).generate(new SuperTop).printPruned()
    ).generate(InOutWrapper(new SuperTop(25 MHz))).printPruned()

  //report.mergeRTLSource("mergeRTL")
    //SpinalVerilog(ClockDomain.external("", withReset = false)(new Top))
    println("...........End")
//    sys.exit(0)
  }
}
