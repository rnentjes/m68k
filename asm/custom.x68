BLTDDAT   EQU  $000  ;ER  A       Blitter destination early read (dummy address)
DMACONR   EQU  $002  ;R   AP      DMA control (and blitter status) read
VPOSR     EQU  $004  ;R   A( E )  Read vert most signif. bit (and frame flop)
VHPOSR    EQU  $006  ;R   A       Read vert and horiz. position of beam
DSKDATR   EQU  $008  ;ER  P       Disk data early read (dummy address)
JOY0DAT   EQU  $00A  ;R   D       Joystick-mouse 0 data (vert,horiz)
JOY1DAT   EQU  $00C  ;R   D       Joystick-mouse 1 data (vert,horiz)
CLXDAT    EQU  $00E  ;R   D       Collision data register (read and clear)
ADKCONR   EQU  $010  ;R   P       Audio, disk control register read
POT0DAT   EQU  $012  ;R   P( E )  Pot counter pair 0 data (vert,horiz)
POT1DAT   EQU  $014  ;R   P( E )  Pot counter pair 1 data (vert,horiz)
POTGOR    EQU  $016  ;R   P       Pot port data read (formerly POTINP)
SERDATR   EQU  $018  ;R   P       Serial port data and status read
DSKBYTR   EQU  $01A  ;R   P       Disk data byte and status read
INTENAR   EQU  $01C  ;R   P       Interrupt enable bits read
INTREQR   EQU  $01E  ;R   P       Interrupt request bits read
DSKPTH    EQU  $020  ;W   A( E )  Disk pointer (high 3 bits, 5 bits if ECS)
DSKPTL    EQU  $022  ;W   A       Disk pointer (low 15 bits)
DSKLEN    EQU  $024  ;W   P       Disk length
DSKDAT    EQU  $026  ;W   P       Disk DMA data write
REFPTR    EQU  $028  ;W   A       Refresh pointer
VPOSW     EQU  $02A  ;W   A       Write vert most signif. bit (and frame flop)
VHPOSW    EQU  $02C  ;W   A       Write vert and horiz position of beam
COPCON    EQU  $02E  ;W   A( E )  Coprocessor control register (CDANG)
SERDAT    EQU  $030  ;W   P       Serial port data and stop bits write
SERPER    EQU  $032  ;W   P       Serial port period and control
POTGO     EQU  $034  ;W   P       Pot port data write and start
JOYTEST   EQU  $036  ;W   D       Write to all four joystick-mouse counters at once
STREQU    EQU  $038  ;S   D       Strobe for horiz sync with VB and EQU
STRVBL    EQU  $03A  ;S   D       Strobe for horiz sync with VB (vert. blank)
STRHOR    EQU  $03C  ;S   DP      Strobe for horiz sync
STRLONG   EQU  $03E  ;S   D( E )  Strobe for identification of long horiz. line.
BLTCON0   EQU  $040  ;W   A       Blitter control register 0
BLTCON1   EQU  $042  ;W   A( E )  Blitter control register 1
BLTAFWM   EQU  $044  ;W   A       Blitter first word mask for source A
BLTALWM   EQU  $046  ;W   A       Blitter last word mask for source A
BLTCPTH   EQU  $048  ;W   A       Blitter pointer to source C (high 3 bits)
BLTCPTL   EQU  $04A  ;W   A       Blitter pointer to source C (low 15 bits)
BLTBPTH   EQU  $04C  ;W   A       Blitter pointer to source B (high 3 bits)
BLTBPTL   EQU  $04E  ;W   A       Blitter pointer to source B (low 15 bits)
BLTAPTH   EQU  $050  ;W   A( E )  Blitter pointer to source A (high 3 bits)
BLTAPTL   EQU  $052  ;W   A       Blitter pointer to source A (low 15 bits)
BLTDPTH   EQU  $054  ;W   A       Blitter pointer to destination D (high 3 bits)
BLTDPTL   EQU  $056  ;W   A       Blitter pointer to destination D (low 15 bits)
BLTSIZE   EQU  $058  ;W   A       Blitter start and size (window width,height)
BLTCON0L  EQU  $05A  ;W   A( E )  Blitter control 0, lower 8 bits (minterms)
BLTSIZV   EQU  $05C  ;W   A( E )  Blitter V size (for 15 bit vertical size)
BLTSIZH   EQU  $05E  ;W   A( E )  Blitter H size and start (for 11 bit H size)
BLTCMOD   EQU  $060  ;W   A       Blitter modulo for source C
BLTBMOD   EQU  $062  ;W   A       Blitter modulo for source B
BLTAMOD   EQU  $064  ;W   A       Blitter modulo for source A
BLTDMOD   EQU  $066  ;W   A       Blitter modulo for destination D
;              $068  ;
;              $06A  ;
;              $06C  ;
;              $06E  ;
BLTCDAT   EQU  $070  ;W   A       Blitter source C data register
BLTBDAT   EQU  $072  ;W   A       Blitter source B data register
BLTADAT   EQU  $074  ;W   A       Blitter source A data register
;              $076  ;
SPRHDAT   EQU  $078  ;W   A( E )  Ext. logic UHRES sprite pointer and data id
;              $07A  ;
DENISEID  EQU  $07C  ;R   D( E )  Chip revision level for Denise (video out chip)
DSKSYNC   EQU  $07E  ;W   P       Disk sync pattern register for disk read
COP1LCH   EQU  $080  ;W   A( E )  Coprocessor first location register (high 3 bits, high 5 bits if ECS)
COP1LCL   EQU  $082  ;W   A       Coprocessor first location register(low 15 bits)
COP2LCH   EQU  $084  ;W   A( E )  Coprocessor second location register (high 3 bits, high 5 bits if ECS)
COP2LCL   EQU  $086  ;W   A       Coprocessor second location register (low 15 bits)
COPJMP1   EQU  $088  ;S   A       Coprocessor restart at first location
COPJMP2   EQU  $08A  ;S   A       Coprocessor restart at second location
COPINS    EQU  $08C  ;W   A       Coprocessor instruction fetch identify
DIWSTRT   EQU  $08E  ;W   A       Display window start (upper left vert-horiz position)
DIWSTOP   EQU  $090  ;W   A       Display window stop (lower right vert.-horiz. position)
DDFSTRT   EQU  $092  ;W   A       Display bitplane data fetch start (horiz. position)
DDFSTOP   EQU  $094  ;W   A       Display bitplane data fetch stop (horiz. position)
DMACON    EQU  $096  ;W   ADP     DMA control write (clear or set)
CLXCON    EQU  $098  ;W   D       Collision control
INTENA    EQU  $09A  ;W   P       Interrupt enable bits (clear or set bits)
INTREQ    EQU  $09C  ;W   P       Interrupt request bits (clear or set bits)
ADKCON    EQU  $09E  ;W   P       Audio, disk, UART control
AUD0LCH   EQU  $0A0  ;W   A( E )  Audio channel 0 location (high 3 bits, 5 if ECS)
AUD0LCL   EQU  $0A2  ;W   A       Audio channel 0 location (low 15 bits)
AUD0LEN   EQU  $0A4  ;W   P       Audio channel 0 length
AUD0PER   EQU  $0A6  ;W   P( E )  Audio channel 0 period
AUD0VOL   EQU  $0A8  ;W   P       Audio channel 0 volume
AUD0DAT   EQU  $0AA  ;W   P       Audio channel 0 data
;              $0AC  ;
;              $0AE  ;
AUD1LCH   EQU  $0B0  ;W   A       Audio channel 1 location (high 3 bits)
AUD1LCL   EQU  $0B2  ;W   A       Audio channel 1 location (low 15 bits)
AUD1LEN   EQU  $0B4  ;W   P       Audio channel 1 length
AUD1PER   EQU  $0B6  ;W   P       Audio channel 1 period
AUD1VOL   EQU  $0B8  ;W   P       Audio channel 1 volume
AUD1DAT   EQU  $0BA  ;W   P       Audio channel 1 data
;              $0BC  ;
;              $0BE  ;
AUD2LCH   EQU  $0C0  ;W   A       Audio channel 2 location (high 3 bits)
AUD2LCL   EQU  $0C2  ;W   A       Audio channel 2 location (low 15 bits)
AUD2LEN   EQU  $0C4  ;W   P       Audio channel 2 length
AUD2PER   EQU  $0C6  ;W   P       Audio channel 2 period
AUD2VOL   EQU  $0C8  ;W   P       Audio channel 2 volume
AUD2DAT   EQU  $0CA  ;W   P       Audio channel 2 data
;              $0CC  ;
;              $0CE  ;
AUD3LCH   EQU  $0D0  ;W   A       Audio channel 3 location (high 3 bits)
AUD3LCL   EQU  $0D2  ;W   A       Audio channel 3 location (low 15 bits)
AUD3LEN   EQU  $0D4  ;W   P       Audio channel 3 length
AUD3PER   EQU  $0D6  ;W   P       Audio channel 3 period
AUD3VOL   EQU  $0D8  ;W   P       Audio channel 3 volume
AUD3DAT   EQU  $0DA  ;W   P       Audio channel 3 data
;              $0DC  ;
;              $0DE  ;
BPL1PTH   EQU  $0E0  ;W   A       Bitplane 1 pointer (high 3 bits)
BPL1PTL   EQU  $0E2  ;W   A       Bitplane 1 pointer (low 15 bits)
BPL2PTH   EQU  $0E4  ;W   A       Bitplane 2 pointer (high 3 bits)
BPL2PTL   EQU  $0E6  ;W   A       Bitplane 2 pointer (low 15 bits)
BPL3PTH   EQU  $0E8  ;W   A       Bitplane 3 pointer (high 3 bits)
BPL3PTL   EQU  $0EA  ;W   A       Bitplane 3 pointer (low 15 bits)
BPL4PTH   EQU  $0EC  ;W   A       Bitplane 4 pointer (high 3 bits)
BPL4PTL   EQU  $0EE  ;W   A       Bitplane 4 pointer (low 15 bits)
BPL5PTH   EQU  $0F0  ;W   A       Bitplane 5 pointer (high 3 bits)
BPL5PTL   EQU  $0F2  ;W   A       Bitplane 5 pointer (low 15 bits)
BPL6PTH   EQU  $0F4  ;W   A       Bitplane 6 pointer (high 3 bits)
BPL6PTL   EQU  $0F6  ;W   A       Bitplane 6 pointer (low 15 bits)
;              $0F8  ;
;              $0FA  ;
;              $0FC  ;
;             0$FE  ;
BPLCON0   EQU  $100  ;W   AD( E ) Bitplane control register (misc. control bits)
BPLCON1   EQU  $102  ;W   D       Bitplane control reg. (scroll value PF1, PF2)
BPLCON2   EQU  $104  ;W   D( E )  Bitplane control reg. (priority control)
BPLCON3   EQU  $106  ;W   D( E )  Bitplane control (enhanced features)
BPL1MOD   EQU  $108  ;W   A       Bitplane modulo (odd planes)
BPL2MOD   EQU  $10A  ;W   A       Bitplane modulo (even planes)
;              $10C  ;
;              $10E  ;
BPL1DAT   EQU  $110  ;W   D       Bitplane 1 data (parallel-to-serial convert)
BPL2DAT   EQU  $112  ;W   D       Bitplane 2 data (parallel-to-serial convert)
BPL3DAT   EQU  $114  ;W   D       Bitplane 3 data (parallel-to-serial convert)
BPL4DAT   EQU  $116  ;W   D       Bitplane 4 data (parallel-to-serial convert)
BPL5DAT   EQU  $118  ;W   D       Bitplane 5 data (parallel-to-serial convert)
BPL6DAT   EQU  $11A  ;W   D       Bitplane 6 data (parallel-to-serial convert)
;              $11C  ;
;              $11E  ;
SPR0PTH   EQU  $120  ;W   A       Sprite 0 pointer (high 3 bits)
SPR0PTL   EQU  $122  ;W   A       Sprite 0 pointer (low 15 bits)
SPR1PTH   EQU  $124  ;W   A       Sprite 1 pointer (high 3 bits)
SPR1PTL   EQU  $126  ;W   A       Sprite 1 pointer (low 15 bits)
SPR2PTH   EQU  $128  ;W   A       Sprite 2 pointer (high 3 bits)
SPR2PTL   EQU  $12A  ;W   A       Sprite 2 pointer (low 15 bits)
SPR3PTH   EQU  $12C  ;W   A       Sprite 3 pointer (high 3 bits)
SPR3PTL   EQU  $12E  ;W   A       Sprite 3 pointer (low 15 bits)
SPR4PTH   EQU  $130  ;W   A       Sprite 4 pointer (high 3 bits)
SPR4PTL   EQU  $132  ;W   A       Sprite 4 pointer (low 15 bits)
SPR5PTH   EQU  $134  ;W   A       Sprite 5 pointer (high 3 bits)
SPR5PTL   EQU  $136  ;W   A       Sprite 5 pointer (low 15 bits)
SPR6PTH   EQU  $138  ;W   A       Sprite 6 pointer (high 3 bits)
SPR6PTL   EQU  $13A  ;W   A       Sprite 6 pointer (low 15 bits)
SPR7PTH   EQU  $13C  ;W   A       Sprite 7 pointer (high 3 bits)
SPR7PTL   EQU  $13E  ;W   A       Sprite 7 pointer (low 15 bits)
SPR0POS   EQU  $140  ;W   AD      Sprite 0 vert-horiz start position data
SPR0CTL   EQU  $142  ;W   AD( E ) Sprite 0 vert stop position and control data
SPR0DATA  EQU  $144  ;W   D       Sprite 0 image data register A
SPR0DATB  EQU  $146  ;W   D       Sprite 0 image data register B
SPR1POS   EQU  $148  ;W   AD      Sprite 1 vert-horiz start position data
SPR1CTL   EQU  $14A  ;W   AD      Sprite 1 vert stop position and control data
SPR1DATA  EQU  $14C  ;W   D       Sprite 1 image data register A
SPR1DATB  EQU  $14E  ;W   D       Sprite 1 image data register B
SPR2POS   EQU  $150  ;W   AD      Sprite 2 vert-horiz start position data
SPR2CTL   EQU  $152  ;W   AD      Sprite 2 vert stop position and control data
SPR2DATA  EQU  $154  ;W   D       Sprite 2 image data register A
SPR2DATB  EQU  $156  ;W   D       Sprite 2 image data register B
SPR3POS   EQU  $158  ;W   AD      Sprite 3 vert-horiz start position data
SPR3CTL   EQU  $15A  ;W   AD      Sprite 3 vert stop position and control data
SPR3DATA  EQU  $15C  ;W   D       Sprite 3 image data register A
SPR3DATB  EQU  $15E  ;W   D       Sprite 3 image data register B
SPR4POS   EQU  $160  ;W   AD      Sprite 4 vert-horiz start position data
SPR4CTL   EQU  $162  ;W   AD      Sprite 4 vert stop position and control data
SPR4DATA  EQU  $164  ;W   D       Sprite 4 image data register A
SPR4DATB  EQU  $166  ;W   D       Sprite 4 image data register B
SPR5POS   EQU  $168  ;W   AD      Sprite 5 vert-horiz start position data
SPR5CTL   EQU  $16A  ;W   AD      Sprite 5 vert stop position and control data
SPR5DATA  EQU  $16C  ;W   D       Sprite 5 image data register A
SPR5DATB  EQU  $16E  ;W   D       Sprite 5 image data register B
SPR6POS   EQU  $170  ;W   AD      Sprite 6 vert-horiz start position data
SPR6CTL   EQU  $172  ;W   AD      Sprite 6 vert stop position and control data
SPR6DATA  EQU  $174  ;W   D       Sprite 6 image data register A
SPR6DATB  EQU  $176  ;W   D       Sprite 6 image data register B
SPR7POS   EQU  $178  ;W   AD      Sprite 7 vert-horiz start position data
SPR7CTL   EQU  $17A  ;W   AD      Sprite 7 vert stop position and control data
SPR7DATA  EQU  $17C  ;W   D       Sprite 7 image data register A
SPR7DATB  EQU  $17E  ;W   D       Sprite 7 image data register B

COLOR00   EQU  $180  ;W   D       Color table 00
COLOR01   EQU  $182  ;W   D       Color table 01
COLOR02   EQU  $184  ;W   D       Color table 02
COLOR03   EQU  $186  ;W   D       Color table 03
COLOR04   EQU  $188  ;W   D       Color table 04
COLOR05   EQU  $18A  ;W   D       Color table 05
COLOR06   EQU  $18C  ;W   D       Color table 06
COLOR07   EQU  $18E  ;W   D       Color table 07
COLOR08   EQU  $190  ;W   D       Color table 08
COLOR09   EQU  $192  ;W   D       Color table 09
COLOR10   EQU  $194  ;W   D       Color table 10
COLOR11   EQU  $196  ;W   D       Color table 11
COLOR12   EQU  $198  ;W   D       Color table 12
COLOR13   EQU  $19A  ;W   D       Color table 13
COLOR14   EQU  $19C  ;W   D       Color table 14
COLOR15   EQU  $19E  ;W   D       Color table 15
COLOR16   EQU  $1A0  ;W   D       Color table 16
COLOR17   EQU  $1A2  ;W   D       Color table 17
COLOR18   EQU  $1A4  ;W   D       Color table 18
COLOR19   EQU  $1A6  ;W   D       Color table 19
COLOR20   EQU  $1A8  ;W   D       Color table 20
COLOR21   EQU  $1AA  ;W   D       Color table 21
COLOR22   EQU  $1AC  ;W   D       Color table 22
COLOR23   EQU  $1AE  ;W   D       Color table 23
COLOR24   EQU  $1B0  ;W   D       Color table 24
COLOR25   EQU  $1B2  ;W   D       Color table 25
COLOR26   EQU  $1B4  ;W   D       Color table 26
COLOR27   EQU  $1B6  ;W   D       Color table 27
COLOR28   EQU  $1B8  ;W   D       Color table 28
COLOR29   EQU  $1BA  ;W   D       Color table 29
COLOR30   EQU  $1BC  ;W   D       Color table 30
COLOR31   EQU  $1BE  ;W   D       Color table 31

HTOTAL    EQU  $1C0  ;W   A( E )  Highest number count, horiz line (VARBEAMEN=1)
HSSTOP    EQU  $1C2  ;W   A( E )  Horizontal line position for HSYNC stop
HBSTRT    EQU  $1C4  ;W   A( E )  Horizontal line position for HBLANK start
HBSTOP    EQU  $1C6  ;W   A( E )  Horizontal line position for HBLANK stop
VTOTAL    EQU  $1C8  ;W   A( E )  Highest numbered vertical line (VARBEAMEN=1)
VSSTOP    EQU  $1CA  ;W   A( E )  Vertical line position for VSYNC stop
VBSTRT    EQU  $1CC  ;W   A( E )  Vertical line for VBLANK start
VBSTOP    EQU  $1CE  ;W   A( E )  Vertical line for VBLANK stop
;         EQU  $1D0  ;            Reserved
;         EQU  $1D2  ;            Reserved
;         EQU  $1D4  ;            Reserved
;         EQU  $1D6  ;            Reserved
;         EQU  $1D8  ;            Reserved
;         EQU  $1DA  ;            Reserved
BEAMCON0  EQU  $1DC  ;W   A( E )  Beam counter control register (SHRES,PAL)
HSSTRT    EQU  $1DE  ;W   A( E )  Horizontal sync start (VARHSY)
VSSTRT    EQU  $1E0  ;W   A( E )  Vertical sync start   (VARVSY)
HCENTER   EQU  $1E2  ;W   A( E )  Horizontal position for Vsync on interlace
DIWHIGH   EQU  $1E4  ;W   AD( E ) Display window -  upper bits for start, stop
