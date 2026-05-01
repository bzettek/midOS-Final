movi r1, #100
movi r2, #200
pushr r1
pushr r2
popr r3
popr r4
printr r3
printr r4
movi r5, #18
call r5
exit
printr r1
movi r1, #999
printr r1
ret