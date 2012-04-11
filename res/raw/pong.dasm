		; Entry point
					SET [0x1000], 10 	; Ball X
					SET [0x1001], 7 	; Ball Y
					SET [0x1002], 2  	; X direction
					SET [0x1003], 2  	; Y direction

					
		:run
					SET PUSH, 0x0000	; Black
					JSR draw
					
					ADD [0x1000], [0x1002]	; Move ball x
					ADD [0x1001], [0x1003]	; Move ball y

					AND [0x1000], 0xff
					AND [0x1001], 0xff
					
					SET PUSH, 0xFFFF	; Cyan
					JSR draw
							:lrcheck
                    IFG [0x1002], 128
						SET PC, left
		:right
					IFG 0x60, [0x1000]		; Assuming bounds of 0x60
						SET PC, udcheck
						
					SET i, 256
					SUB i, [0x1002]
					SET [0x1002], i
					SET PC, udcheck
		:left
					IFG [0x1000], 10
						SET PC, udcheck
						
					SET i, 256
					SUB i, [0x1002]
					SET [0x1002], i
				
		:udcheck
					IFG [0x1003], 128
						SET PC, up
		:down
					IFG 0x60, [0x1001]		; Assuming bounds of 0x60
						SET PC, run
						
					SET i, 256
					SUB i, [0x1003]
					SET [0x1003], i
					SET PC, run
		:up
					IFG [0x1001], 10
						SET PC, run
						
					SET i, 256
					SUB i, [0x1003]
					SET [0x1003], i
					
					SET PC, run
					
					
					SET PC, crash
					SET PC, crash
					SET PC, crash 
					
		:draw
					SET j, POP
					SET z, POP			; Colour
					SET x, [0x1000]
					SET y, [0x1001]
					MUL y, 256			; Assuming width of 256
					ADD x, y
					ADD x, 0x3000		; Assuming VRAM starts at 0x3000
					SET [x], z
					ADD x, 1
					SET [x], z
					ADD x, 255
					SET [x], z
					ADD x, 1
					SET [x], z
					SET PC, j
					
		:crash
					SET PC, crash