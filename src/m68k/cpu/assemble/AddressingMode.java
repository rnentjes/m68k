package m68k.cpu.assemble;

/**
 * User: rnentjes
 * Date: 5-10-14
 * Time: 13:04
 */
public enum AddressingMode {
    NA(0),
    IMMEDIATE_DATA(0),
    IMMEDIATE_ADDRESS(1),
    INDIRECT(2),
    INDIRECT_POST(3),
    INDIRECT_PRE(4),
    INDIRECT_DISP(5),
    INDIRECT_INDEX(6),
    ABSOLUTE_NEAR(7),
    ABSOLUTE_FAR(7),
    PC_DISP(7),
    PC_INDEX(7),
    IMMEDIATE(7),
    SR(0),
    CCR(0)
    ;

    private int bits;

    AddressingMode(int bits) {
        this.bits = bits;
    }

    public int bits() {
        return bits;
    }
}
