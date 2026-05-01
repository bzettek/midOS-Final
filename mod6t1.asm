
movi r1, #256     
alloc r1, r2      
movi r3, #11
movrm r2, r3      
alloc r1, r4      
movi r3, #22
movrm r4, r3

alloc r1, r5    
movi r3, #33
movrm r5, r3

alloc r1, r6     
movi r3, #44
movrm r6, r3

printm r2         
printm r4         
printm r5        
printm r6        

movi r3, #111
movrm r2, r3
movi r3, #222
movrm r4, r3
printm r2         
printm r4         
printm r5         
printm r6         

exit
