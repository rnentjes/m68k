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
    move.w    D0, INTREQ(A4)      ; Clear all pending interrupts.
    move.w    D0, INTREQ(A4)      ; Clear all pending interrupts.
    move.w    D0, DMACON(A4)      ; Disable all DMA.

    ; 6kb for stacks
    ; assume 512kb chip at least
    LEA     $80000, A0
    LEA     $7E800, A1
    MOVE.L  A0, SP
    MOVE.L  A1, USP

    move.w    #$0444, COLOR00(A4) ; Background colour = dark gray.

    ; clear ovl to access chip mem and turn power light on
    move.b    #3, $BFE201
    move.b    #2, $BFE001


        ; Set vector interrupts
        ;
        ; Set all interrupts to an empty handler
    move.w    #8,   A0               ; Start at address 8 (vector #2).
    move.w    #$2D, D1               ; Do 46 vectors.
    lea       EMPTY_INTERRUPT,A1 ; Address of initial exception handler.


VECTOR_LOOP:
    move.l    A1,(A0)+          ; Set one vector
    dbra      D1, VECTOR_LOOP   ; Loop back.

        ; Set interrupt prio 3 handler

    lea       $6C,A0                     ; Set vector (level 3 for VBLANK).
    lea       VBLANK_INTERRUPT, a1       ; Address of level 3 interrupt handler.
    move.l    A1,(A0)                    ; Set one vector

    ; enable dna
    ; move.w  #$c380, DMACON(A4)  ; Enable copper & bitplane DMA

    ; enable vblank interrupt
    ; $c020
    move.w   #$C020, INTENA(A4) ; intena bit set and 5 (vbl)

    ; enable interrupts in sr register
    ; go to user mode
    move.w  #300, sr

    MOVEQ.L #0, D0
    LEA     $1004, A0
HALT:
    ADDQ.L  #1, D0
    ADD.L   #1, (A0)
    JMP HALT

EMPTY_INTERRUPT:
    ;MOVEM.L     A0/A4/D0, -(SP)
    ;LEA         $DFF000, A4
    ADD.L       #1, ($1008)

    ;move.w      #$7FFF,D0
    ;move.w      #$7FFF, INTREQ(A4)      ; Clear all pending interrupts.
    ;move.w      #$7FFF, INTREQ(A4)      ; Clear all pending interrupts.
    ;MOVEM.L     (SP)+, D0/A4/A0
    RTE


VBLANK_INTERRUPT:
    ;MOVEM.L   A0/A4/D0, -(SP)
    ;LEA       $DFF000, A4
    ADD.L     #1, ($1000)

    move.w    #$0020, INTREQ(A4)      ; Clear vblank interrupt.
    move.w    #$0020, INTREQ(A4)      ; Clear vblank interrupt.
    ;MOVEM.L   (SP)+, D0/A4/A0
    RTE
