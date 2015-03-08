start:
    abcd    d4, d6
    abcd     -(a5), -(a1)
    MOVE.W   #1, D0
    MOVE.L   #2, D1
    BSR.B    test
    BSR.B    test2
    BEQ.b continue
    MOVEQ    #4, D2
    TRAP     #10
    BRA.B start

continue:
    BRA continue

test:
    MOVEQ    #3, D4
    RTS
