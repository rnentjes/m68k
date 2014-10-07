start:
  move.l (a2), d0
  move.w -(a2), d1
  move.w (a2)+, d2
  move.w 10(a2), d3
  move.w 10(a2, d1), d4
  move.w 123.w, d5
  move.w 12345.l, d5
  move.w 10(PC), d6
  move.w (10, PC, d4), d7
  move.w 10(PC, d4), d7

  adda.w d0, a0
  move.w #$1234,d0
label2:
  addq  #7, d1
  move.l d0,d2
  ;move.w (10, A0, A1.w), d3
  addi #$1234, d0
  addi.l #$4321, (a0)

pipo equ $1234
