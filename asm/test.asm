start:
  adda.w d0, a0
  move.w #$1234,d0
label2:
  addq  #7, d1
  move.l d0,d2
  ;move.w (10, A0, A1.w), d3

pipo equ $1234
