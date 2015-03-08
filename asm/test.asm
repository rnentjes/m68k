start:
    addx    d2, d3
    addx    -(a5), -(a3)
    abcd    d4, d6
    abcd     -(a5), -(a1)
    bsr.b   test
    BRA.B   start

test:
    MOVEQ    #3, D4
    RTS
