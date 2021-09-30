# RestAPIs


========== WORKING MODEL ==================
	1) Threads pick up orders from a queue (Chefs)
	2) Are able to be interrupted on a pause or cancelled query
	3) And sleep periodically waking up to check the queue
	4) Given SQL file was modifed: table sushi_order was appended to include
	field time_left. Field used to keep track of cancelled and paused orders
	(resumption of paused orders).
	
========== ASSUMPTIONS ==================
	1) Invalid body in the POST method will result in a 500 server error
	2) Only IN-PROGRESS orders can be paused
	3) Only PAUSED orders can be resumed
	4) FINISHED orders cannot be cancelled
	5) DB uses time_left for a job: time_spent = total_time_to_make_roll - time_left
	and total_time_to_make_roll can be deduced from the sushi_id via sushi table
	6) Threads are started asynchrounously with a delay
	7) Chefs only deal with the queue to pick up orders
