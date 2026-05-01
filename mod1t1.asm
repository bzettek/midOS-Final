movi r1, #5
movi r2, #10
addr r1, r2
printr r1
movi r3, #1
movi r4, @A
printcr r4
incr r3
cmpi r3, #4
jlti #-27
exit