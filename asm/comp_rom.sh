#!/bin/sh
java -jar ../lib/reasm.jar rom.x68 MC68000

cp a.out custom.rom
length=$(stat -c%s custom.rom)
bytes=$(expr 524288 - $length)
dd if=/dev/zero of=custom.rom bs=1 count=${bytes} seek=${length}
