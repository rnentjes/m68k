*-----------------------------------------------------------
* Title      :
* Written by :
* Date       :
* Description:
*-----------------------------------------------------------
    ORG    $F80000
    DC.W   $1111

INIT:                  ; first instruction of program

* Put program code here

    JMP $F800D2

    ; Skip to Start
    DS.B $CA

START:
    ; 6kb for stacks
    LEA     $80000, A0
    LEA     $7E800, A1
    MOVE.L  A0, SP
    MOVE.L  A1, USP
    MOVE.L  #$020000, D0

WAIT:
    NOP
    SUBQ.L  #1, D0
    BGT.S   WAIT

        ; Disable interrupts and DMA.

    lea       $DFF000,A4         ; Base address of custom chip area.
    move.w    #$7FFF,D0
    move.w    D0,$9A(A4)        ; Disable all interrupts.
    move.w    D0,$9C(A4)        ; Clear all pending interrupts.
    move.w    D0,$96(A4)        ; Disable all DMA.

    move.w    #$0200,$0100(A4)  ; BPLCON0 = Blank screen.
    move.w    #$AAAA,$0110(A4)  ; Bitplane 0 data = all zeros.
    move.w    #$0444,$0180(A4)  ; Background colour = dark gray.

    ; clear ovl to access chip mem and turn power on
    move.b    #3, $BFE201

    ; test SP

    ; BSR   SUBROUTINE
    ; JMP   COLORS

        ; Set all interrupts to an empty handler
    move.w    #8,A0                 ; Start at address 8 (vector #2).
    move.w    #$2D,D1               ; Do 46 vectors.
    lea       EMPTY_INTERRUPT,A1    ; Address of initial exception handler.

VECTOR_LOOP:
    move.l    A1,(A0)+          ; Set one vector
    dbra      D1, VECTOR_LOOP   ; Loop back.

        ; Set interrupt prio 3 handler

    move.w    #$6C,A0           ; Set vector (level 3 for VBLANK).
    lea       INTERRUPT(PC),A1  ; Address of level 3 interrupt handler.
    move.l    A1,(A0)           ; Set one vector

    ; trap 0 test
    move.l     #TRAP0, $80

        ; Enable vblank interrupt
    move.w  #$20,$9c(A4)         ; intreq vbi off
    move.w  #$20,$9c(A4)         ; twice

    move.w  #$8180,$96(A4)       ; dma copper & bitplane on
    move.w  #$c030,$9a(A4)       ; intena bit set and 5 (vbl)

        ; write to copper to wake it up (and the dma, and with that the interrupt(?))

    LEA     COPPERSTART(PC), A0
    LEA     $1080, A1
    MOVE.W  COPPEREND - COPPERSTART, D2
    ROR.W   #2, D2
COPYCOP:
    MOVE.L  (A0)+, (A1)+
    DBEQ    D2, COPYCOP

    LEA     $1080, A1
    MOVE.L  A1, $80(A4)     ; copper list address
    MOVE.L  A1, $E0(A4)     ; bitplane 0 address
    MOVE.W  $88(A4), D2
    MOVE.W  D2, $88(A4)

    MOVE.W  #$1000, $100(A4)    ; BPLCON0 1 bitplane start at 1000
    MOVE.W  #$0000, $102(A4)    ; BPLCON1
    MOVE.W  #$0000, $104(A4)    ; BPLCON2

    MOVE.W  #$2020, $8E(A4)     ; DIWSTRT
    MOVE.W  #$8080, $90(A4)     ; DIWSTOP
    MOVE.W  #$0070, $92(A4)     ; DDFSTRT
    MOVE.W  #$01A0, $94(A4)     ; DDFSTOP

COLORS:
    MOVE.W  #$0F0F, $0180(A4)
    TRAP #0
    MOVE.W  #$0FFF, $0180(A4)

    MOVE.W  #$8F8, D0

    MOVE.W  D0, $0180(A4)
    ADDQ.W  #1, D0
    AND.W   #$FFF, D0

    MOVE.W  #$8F8, D0

    lea     $1004,A0
    ADDQ.L  #1, (A0)

    BSR   SUBROUTINE

    MOVE.W  D0, $0180(A4)
LOOP:
    JMP     LOOP

INTERRUPT_CALL:
    JMP INTERRUPT

EMPTY_INTERRUPT:
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

SUBROUTINE:
    LEA     $DFF000,A4         ; Base address of custom chip area.
    MOVE.W  #$848, $0180(A4)
    RTS


TRAP0:
    MOVEM   A0/A4/D0, -(SP)

    lea     $DFF000,A4         ; Base address of custom chip area.
    MOVE.W  #$FF0, D0
    MOVE.W  D0, $0180(A4)

    lea     $1000,A0
    MOVE.L  (A0), D0
    ADDQ.L  #1, D0
    MOVE.L  D0, (A0)

    MOVEM   (SP)+, D0/A4/A0
    RTE

COPPERSTART:
    DC.W $180, $0888   ; Move black into register $0180 (COLOR00)
    DC.W $9601,$FF00   ; Wait for line 150, ignore horiz. position
    DC.W $180, $0444   ; Move black into register $0180 (COLOR00)
    DC.W $FFFF, $FFFF

COPPEREND: