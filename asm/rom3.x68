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
    move.w    #$0CCC, COLOR01(A4) ; Color 1 = light gray.

	move.w	#$2c81,DIWSTRT(a4)   	; DIWSTRT - topleft corner (2c81)
	move.w	#$f4d1,DIWSTOP(a4)      ; DIWSTOP - bottomright corner (f4d1)
    MOVE.W  #$0038,DDFSTRT(a4)      ; Write to DDFSTRT
    MOVE.W  #$00D0,DDFSTOP(a4)      ; Write to DDFSTOP
    MOVE.W  #0,BPL1MOD(a4)
    MOVE.W  #0,BPL2MOD(a4)

    ; clear ovl to access chip mem and turn power light on
    move.b    #3, $BFE201
    move.b    #2, $BFE001


        ; Set vector interrupts
        ;
        ; Set all interrupts to an empty handler
    move.w    #8,   A0               ; Start at address 8 (vector #2).
    move.w    #$2D, D1               ; Do 46 vectors.
    lea       EMPTY_INTERRUPT(PC),A1 ; Address of initial exception handler.

VECTOR_LOOP:
    move.l    A1,(A0)+          ; Set one vector
    dbra      D1, VECTOR_LOOP   ; Loop back.

        ; Set interrupt prio 3 handler

    lea       $6C,A0                     ; Set vector (level 3 for VBLANK).
    lea       VBLANK_INTERRUPT(PC), a1   ; Address of level 3 interrupt handler.
    move.l    A1,(A0)                    ; Set one vector


    ; copy copper data to chip $1080
    LEA     COPPERSTART(PC), A0
    LEA     $1080, A1
CLOOP:
    MOVE.L (a0),(a1)+           ; Move a word
    CMPI.L #$FFFFFFFE,(a0)+     ; Check for last longword of Copper list
    BNE CLOOP                   ; Loop until entire copper list i9 moved


    LEA     $1080, A1
    MOVE.L  A1, COP1LCH(A4)     ; copper list address
    MOVE.L  A1, COP2LCH(A4)     ; copper list address
    ; strobe to start
    MOVE.W  #$ffff, COPJMP1(A4)

    move.w  #$c380, DMACON(A4)  ; Enable copper & bitplane DMA

    ; clear pending interrupts
    move.w    #$7FFF,D0
    move.w    D0, INTREQ(A4)      ; Clear vblank interrupt.
    move.w    D0, INTREQ(A4)      ; Clear vblank interrupt.

    ;move.w  #$230, VBSTRT(A4)
    ;move.w  #$020, VBSTOP(A4)

    ; enable vblank interrupt
    ; $c020
    move.w  #%1100000000110000, INTENA(A4)       ; intena bit set and 5 (vbl)

    LEA     $1000, A0
    ADD.L   #1, (A0)

    move.w  #$8020, INTREQ(A4)       ; intena bit set and 5 (vbl)

    MOVEQ.L #0, D0
    LEA     $1004, A0
HALT:
    ADDQ.L  #1, D0
    ADD.L   #1, (A0)
    JMP HALT


EMPTY_INTERRUPT:
    MOVEM     A0/A4/D0, -(SP)
    LEA       $DFF000, A4
    LEA       $1008, A0
    ADD.L     #1, (A0)
    move.w    #$7FFF,D0
    move.w    D0, INTREQ(A4)      ; Clear all pending interrupts.
    MOVEM     (SP)+, A0/A4/D0
    RTE


VBLANK_INTERRUPT:
    MOVEM     A0/A4/D0, -(SP)
    LEA       $DFF000, A4
    LEA       $1000, A0
    ADD.L     #1, (A0)

    move.w    #$0020,D0
    move.w    D0, INTREQ(A4)      ; Clear vblank interrupt.
    move.w    D0, INTREQ(A4)      ; Clear vblank interrupt.
    MOVEM     (SP)+, A0/A4/D0
    RTE


COPPERSTART:
    ; bitplane 0
    DC.W BPL1PTH,$0000
    DC.W BPL1PTL,$1000
    DC.W BPLCON0,$1200

    DC.W $180, $0888   ; Move grey into register $0180 (COLOR00)
    DC.W $6601,$FF00   ; Wait for line 150, ignore horiz. position
    DC.W $180, $008F   ; Move white into register $0180 (COLOR00)
    DC.W $9601,$FF00   ; Wait for line 150, ignore horiz. position
    DC.W $180, $0F80   ; Move white into register $0180 (COLOR00)
    DC.W INTREQ, $8010   ; Move white into register $0180 (COLOR00)
    DC.W $FFFF, $FFFE

