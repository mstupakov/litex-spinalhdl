package test.pmods

import spinal.core._
import spinal.lib._

abstract class PMODBundle() extends Bundle with IMasterSlave {
  val pmod_pin_1 = Bool()
  val pmod_pin_2 = Bool()
  val pmod_pin_3 = Bool()
  val pmod_pin_4 = Bool()
  val pmod_pin_5 = Bool()
}

case class DIPSwitch() extends PMODBundle {
  override def asMaster() = this.asInput()
}

case class DIPSwitchCtrl() extends Component {
  val io = new Bundle {
    val pins = master(DIPSwitch())
    val output = out(UInt(5 bits))
  }

  val bits = Cat(
    io.pins.pmod_pin_1,
    io.pins.pmod_pin_2,
    io.pins.pmod_pin_3,
    io.pins.pmod_pin_4,
    io.pins.pmod_pin_5
  )

  io.output := bits.asUInt
}


