package uk.co.sticksoft.adce;

public class SampleASM
{
    public final static String notchs_example_asm =
    		"        ; Try some basic stuff\r\n" + 
    		"                      SET A, 0x30              ; 7c01 0030\r\n" + 
    		"                      SET [0x1000], 0x20       ; 7de1 1000 0020\r\n" + 
    		"                      SUB A, [0x1000]          ; 7803 1000\r\n" + 
    		"                      IFN A, 0x10              ; c00d \r\n" + 
    		"                         SET PC, crash         ; 7dc1 001a [*]\r\n" + 
    		"                      \r\n" + 
    		"        ; Do a loopy thing\r\n" + 
    		"                      SET I, 10                ; a861\r\n" + 
    		"                      SET A, 0x2000            ; 7c01 2000\r\n" + 
    		"        :loop         SET [0x2000+I], [A]      ; 2161 2000\r\n" + 
    		"                      SUB I, 1                 ; 8463\r\n" + 
    		"                      IFN I, 0                 ; 806d\r\n" + 
    		"                         SET PC, loop          ; 7dc1 000d [*]\r\n" + 
    		"        \r\n" + 
    		"        ; Call a subroutine\r\n" + 
    		"                      SET X, 0x4               ; 9031\r\n" + 
    		"                      JSR testsub              ; 7c10 0018 [*]\r\n" + 
    		"                      SET PC, crash            ; 7dc1 001a [*]\r\n" + 
    		"        \r\n" + 
    		"        :testsub      SHL X, 4                 ; 9037\r\n" + 
    		"                      SET PC, POP              ; 61c1\r\n" + 
    		"                        \r\n" + 
    		"        ; Hang forever. X should now be 0x40 if everything went right.\r\n" + 
    		"        :crash        SET PC, crash            ; 7dc1 001a [*]\r\n" + 
    		"        \r\n" + 
    		"        ; [*]: Note that these can be one word shorter and one cycle faster by using the short form (0x00-0x1f) of literals,\r\n" + 
    		"        ;      but my assembler doesn't support short form labels yet.     ";
    
    public final static  char[] notchs_example_assembled =
	{
		0x7c01, 0x0030, 0x7de1, 0x1000, 0x0020, 0x7803, 0x1000, 0xc00d,
		0x7dc1, 0x001a, 0xa861, 0x7c01, 0x2000, 0x2161, 0x2000, 0x8463,
		0x806d, 0x7dc1, 0x000d, 0x9031, 0x7c10, 0x0018, 0x7dc1, 0x001a,
		0x9037, 0x61c1, 0x7dc1, 0x001a, 0x0000, 0x0000, 0x0000, 0x0000,
	};
}
