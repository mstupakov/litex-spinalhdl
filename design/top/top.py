import os
import argparse

import ulx3s

from migen import *
from migen.fhdl import verilog
from migen.fhdl.decorators import ClockDomainsRenamer
from migen.fhdl.verilog import convert
from migen.fhdl.structure import Mux, Cat, If, Case
from migen.build.generic_platform import Pins, IOStandard
from migen.genlib.misc import WaitTimer
from migen.genlib.divider import Divider

from litex.soc.cores.led import LedChaser
from litex.soc.cores.clock import *
from litex.build.generic_platform import *

feather_serial = [
    ("gpio_serial", 0,
        Subsignal("tx", Pins("GPIO_P:0"), IOStandard("LVCMOS33")),
        Subsignal("rx", Pins("GPIO_N:0"), IOStandard("LVCMOS33")),
    )
]

class _CRG(Module):
    def __init__(self, platform):
        self.clock_domains.cd_sys = ClockDomain()

        clk = plat.request("clk25")
        rst = plat.request("rst")

        plat.add_period_constraint(clk, 1e9/25e6)

        self.comb += self.cd_sys.clk.eq(clk)
        self.comb += self.cd_sys.rst.eq(rst)

        self.clock_domains.cd_sys_5MHz  = ClockDomain()
        self.clock_domains.cd_sys_10MHz = ClockDomain()
        self.clock_domains.cd_sys_50MHz = ClockDomain()

        self.submodules.pll = pll = ECP5PLL()
        pll.register_clkin(clk, 25e6)

        pll.create_clkout(self.cd_sys_5MHz,   5e6)
        pll.create_clkout(self.cd_sys_10MHz, 10e6)
        pll.create_clkout(self.cd_sys_50MHz, 50e6)

class Top(Module):
  def __init__(self, leds, btns, serial):
    self.submodules.crg = _CRG(plat)

    self.specials += Instance(
            "SuperTop",

            i_clk_25mhz = ClockSignal(),
            i_rst       = ResetSignal(),

            i_clk_5MHz  = ClockSignal("sys_5MHz"),
            i_clk_10MHz = ClockSignal("sys_10MHz"),
            i_clk_20MHz = ClockSignal("sys_10MHz"),

            i_btns      = Cat(* btns),
            o_leds      = Cat(* leds),

            i_uart_rx   = serial.rx,
            o_uart_tx   = serial.tx,
    )

##############################################################
##############################################################
plat = ulx3s.Platform(device="LFE5U-85F")

vdirs = [ os.path.dirname(__file__), "design/spinalhdl/output" ]
[plat.add_verilog_include_path(p) for p in vdirs]
[plat.add_source_dir(p)           for p in vdirs]

leds = [
         plat.request_all("user_led")
       ]

btns = [
         plat.request("user_btn"),
         plat.request("user_btn"),
         plat.request("user_btn"),
         plat.request("user_btn"),
         plat.request("user_btn"),
       ]

plat.add_extension(feather_serial)
serial = plat.request("gpio_serial")

top = Top(leds, btns, serial)

parser = argparse.ArgumentParser(description="ULX3S Board")
parser.add_argument("--build", action="store_true", help="Build bitstream")
parser.add_argument("--upload", action="store_true", help="Upload bitstream")

args = parser.parse_args()

if args.build:
  plat.build(top, compress=True, run=args.build)

if args.upload:
  plat.create_programmer().load_bitstream('build/top.bit')
