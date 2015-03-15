    bra.b     loop
    clr.l   d0
    moveq.w #$8, d0
loop:
    addq.l  #2, d1
    dbra    d0, loop

    rts
