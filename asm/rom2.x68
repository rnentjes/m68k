*-----------------------------------------------------------
* Title      :
* Written by :
* Date       :
* Description:
*-----------------------------------------------------------
    INCLUDE "custom.x68"

    ORG    $F80000
    DC.W   $1111

INIT:                  ; first instruction of program

* Put program code here

    JMP START

START:

    MOVE.L  #$020000, D0

WAIT:
    NOP
    SUBQ.L  #1, D0
    BGT.S   WAIT

        ; Disable interrupts and DMA.

    lea       $DFF000,A4         ; Base address of custom chip area.
    move.w    #$7FFF,D0
    move.w    D0, INTENA(A4)      ; Disable all interrupts.
    move.w    D0, DMACON(A4)      ; Disable all DMA.
    move.w    D0, INTREQ(A4)      ; Clear all pending interrupts.
    move.w    D0, INTREQ(A4)      ; Clear all pending interrupts.

    ; 6kb for stacks
    ; assume 512kb chip at least
    LEA     $80000, A0
    LEA     $7E800, A1
    MOVE.L  A0, SP
    MOVE.L  A1, USP

    move.w    #$1000, BPLCON0(A4) ; BPLCON0 = Blank screen.
    move.w    #$AAAA, BPL1DAT(A4) ; Bitplane 0 data = all zeros.
    move.w    #$0444, COLOR00(A4) ; Background colour = dark gray.
    move.w    #$0CCC, COLOR01(A4) ; Color 1 = light gray.

    MOVE.W  #$0038,DDFSTRT(a4)      ; Write to DDFSTRT
    MOVE.W  #$00D0,DDFSTOP(a4)      ; Write to DDFSTOP
    MOVE.W  #0,BPL1MOD(a4)

    ; clear ovl to access chip mem and turn power light on
    move.b    #3, $BFE201

    ; copy copper data to chip $1080
    LEA     COPPERSTART(PC), A0
    LEA     $1080, A1
    MOVE.W  COPPEREND - COPPERSTART, -$10(A1)
    MOVE.W  (COPPEREND - COPPERSTART), D2
    ROR.W   #2, D2
    ; hack
    MOVE.W  #4, D2
    MOVE.W  D2, -$E(A1)
COPYCOP:
    MOVE.L  (A0)+, (A1)+
    DBEQ    D2, COPYCOP

    LEA     $1080, A1
    MOVE.L  A1, COP1LCH(A4)     ; copper list address
    ; strobe to start
    ; MOVE.W  $88(A4), D2

    MOVE.W  #1, COPJMP1(A4)

    move.w  #$C180, DMACON(A4)  ; Enable copper and bitplanes




        ; Set vector interrupts
        ;
        ; Set all interrupts to an empty handler
    move.w    #8,A0                 ; Start at address 8 (vector #2).
    move.w    #$2D,D1               ; Do 46 vectors.
    lea       EMPTY_INTERRUPT,A1    ; Address of initial exception handler.

VECTOR_LOOP:
    move.l    A1,(A0)+          ; Set one vector
    dbra      D1, VECTOR_LOOP   ; Loop back.

        ; Set interrupt prio 3 handler

    move.w    #$6C,A0                   ; Set vector (level 3 for VBLANK).
    lea       VBLANK_INTERRUPT(PC),A1   ; Address of level 3 interrupt handler.
    move.l    A1,(A0)                   ; Set one vector

    ; enable vblank interrupt
    move.w  #$c020,$9a(A4)       ; intena bit set and 5 (vbl)




HALT:
    JMP HALT

EMPTY_INTERRUPT:
    move.w    #$7FFF,D0
    move.w    D0,INTREQ(A4)      ; Clear all pending interrupts.
    RTE

VBLANK_INTERRUPT:
    move.w    #$0020,D0
    move.w    D0,INTREQ(A4)      ; Clear vblank interrupt.
    RTE



INTERRUPT:
    MOVEM   A0/A4/D0, -(SP)
    lea     $DFF000,A4         ; Base address of custom chip area.

    move.w #$0020,$9c(A4)   ; Intreq = interrupt processed.
    move.w #$0020,$9c(A4)   ; twice for compatibility

    MOVE.W  #$0FF, D0

    MOVE.W  D0, $0182(A4)

    lea     $1004,A0         ; Base address of custom chip area.
    ADDQ.L  #1, (A0)

    MOVEM   (SP)+, A0/A4/D0
    RTE


COPPERSTART:
    DC.W $180, $0888   ; Move black into register $0180 (COLOR00)
    DC.W $9601,$FF00   ; Wait for line 150, ignore horiz. position
    DC.W $180, $0444   ; Move black into register $0180 (COLOR00)
    DC.W $FFFF, $FFFF
COPPEREND:
