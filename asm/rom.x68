*-----------------------------------------------------------
* Title      :
* Written by :
* Date       :
* Description:
*-----------------------------------------------------------
    ORG    $0
    DC.W   $1111
    
INIT:                  ; first instruction of program

* Put program code here

    JMP $FC00D2

    ; Skip to Start
    DS.B $CA
    
START:    
    MOVE.L  #$020000, D0
WAIT:    
    SUBQ.L  #1, D0
    BGT.S   WAIT

    MOVE.W $0, D0
    
    LEA $DFF000, A4
LOOP:
    MOVE.W  D0, $0180(A4)
    ADDQ.W  #1, D0
    JMP     LOOP
    
END:
    JMP END    
    END START
    
*~Font name~Courier New~
*~Font size~10~
*~Tab type~1~
*~Tab size~4~
