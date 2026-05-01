movi r1, #100     
alloc r1, r2      
printr r2         
movi r3, #42
movrm r2, r3      
printm r2         

movi r1, #600     
alloc r1, r4
printr r4         

movi r1, #99999   
alloc r1, r5
printr r5         

freememory r2     
movi r1, #100
alloc r1, r6      
printr r6         

exit
