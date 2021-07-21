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

my_connector = [
  ("MYGPIO", "R18 V1 U1 H16"),
]

my_feather_btns = [
  ("btn_ctrl", 0,
      Subsignal("up"   , Pins("MYGPIO:0"), IOStandard("LVCMOS33"), Misc("PULLMODE=DOWN")),
      Subsignal("down" , Pins("MYGPIO:1"), IOStandard("LVCMOS33"), Misc("PULLMODE=DOWN")),
      Subsignal("left" , Pins("MYGPIO:2"), IOStandard("LVCMOS33"), Misc("PULLMODE=DOWN")),
      Subsignal("right", Pins("MYGPIO:3"), IOStandard("LVCMOS33"), Misc("PULLMODE=DOWN")),
  )
]

ulx3s._io_common += [
  ("my_leds", 0,
      Subsignal("leds", Pins("B2 C2 C1 D2 D1 E2 E1 H3")), IOStandard("LVCMOS33")
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
  def __init__(self, leds, btns):
    self.submodules.crg = _CRG(plat)

    self.specials += Instance(
            "SuperTop",

            i_clk_25mhz = ClockSignal(),
            i_rst       = ResetSignal(),

            i_clk_5MHz  = ClockSignal("sys_5MHz"),
            i_clk_10MHz = ClockSignal("sys_10MHz"),
            i_clk_20MHz = ClockSignal("sys_10MHz"),

            i_btns      = Cat(* btns),
            o_leds      = Cat(* leds)
    )

parser = argparse.ArgumentParser(description="MyBoard Args")

parser.add_argument("--build", action="store_true", help="Build bitstream")
parser.add_argument("--upload", action="store_true", help="Upload bitstream")
parser.add_argument("-p", "--parallel", default=1, type=int,
                    help="number of parallel builds (default: %(default)s)")

args = parser.parse_args()
print("Paralel: ", args.parallel)


plat = ulx3s.Platform(device="LFE5U-85F")

vdirs = [ os.path.dirname(__file__), "design/spinalhdl/output" ]
[plat.add_verilog_include_path(p) for p in vdirs]
[plat.add_source_dir(p)           for p in vdirs]

leds = [
         plat.request("user_led", 0),
         plat.request("user_led", 1),
         plat.request("user_led", 2),
         plat.request("user_led", 3),
         plat.request("user_led", 4),
         plat.request("user_led", 5),
         plat.request("user_led", 6),
         plat.request("user_led", 7),
       ]

btns = [
         plat.request("user_btn"),
         plat.request("user_btn"),
         plat.request("user_btn"),
         plat.request("user_btn"),
         plat.request("user_btn"),
       ]

top = Top(leds, btns)

plat.build(top, compress=True, run=args.build)

if args.upload:
  plat.create_programmer().load_bitstream('build/top.bit')
