#!/bin/sh
java -jar ../lib/reasm.jar rom3.x68 MC68000

cp a.out custom.rom
length=$(stat -c%s custom.rom)
bytes=$(expr 524272 - $length)
dd if=/dev/zero of=custom.rom bs=1 count=${bytes} seek=${length}
dd if=romend.dat of=custom.rom bs=1 count=16 seek=524272

