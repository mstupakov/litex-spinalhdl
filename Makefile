SHELL = /bin/bash

all: gen_spinal gen_migen

gen_migen:
	python3 generic.py --build

gen_spinal:
	cd design/spinalhdl/ && sbt -client run

build:
	$(build/build_top.sh)

upload: gen_spinal gen_migen
	python3 generic.py --upload

clean:
	ls design/spinalhdl/{target,project}/

cleanall:
	-rm -rf build/ .bsp/ target/ project/ boards/__pycache__/ design/top/__pycache__/ \
		design/spinalhdl/.bsp/ design/spinalhdl/target/ design/spinalhdl/project/ design/spinalhdl/output/*

.PHONY: .all gen_migen gen_spinal build clean cleanall
