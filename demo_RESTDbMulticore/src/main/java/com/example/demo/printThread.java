package com.example.demo;

import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.List;

@Component
@Scope("prototype")
public class printThread extends Thread{

	private float time;
	private int timeToFinish;
	private float timeSpent;
	private float timeLeft;
	private SushiOrder currentOrder;
	static volatile Deque<SushiOrder> jobQueue;
	
	final int ORDER_IN_PROGRESS = 2;
	final int ORDER_FINISHED = 4;
	
	final int WORK_INTERVAL_MS = 500;
	final int MILLISECOND = 1000;
	
    JdbcTemplate jdbcTemplate;
    
    final int DEBUG = 1;
	
	/*
	 * 1) Take items from queue
	 * 2) Able to be interrupted via a pause cmd/delete cmds
	 * 3) Otherwise wake up every so often and check queue
	 */
	@Override
	public void run() {
		while( true ) {

			boolean queueMutex = false;
			synchronized( jobQueue ) {
				if( !jobQueue.isEmpty() ) {
					currentOrder = jobQueue.remove();
					queueMutex = true;
				}
			}
			
			if( queueMutex ) {
				// Update Status
			    String sql = "UPDATE SUSHI_ORDER SET STATUS_ID=" + ORDER_IN_PROGRESS + " WHERE ID=" + currentOrder.getId();
		        int result = jdbcTemplate.update(sql);
				
				// Process order
				time = 0;
				timeToFinish = currentOrder.getTime_left();
				timeSpent = getTotalTimeById( currentOrder.getSushi_id() ) - timeToFinish;
				timeLeft = getTotalTimeById( currentOrder.getSushi_id() ) - timeSpent;
				
				System.out.println(getName() + " is running order: " + currentOrder.getId() + " with time: " + timeToFinish);
				try {
					while( true ) {
						Thread.sleep(WORK_INTERVAL_MS);
						time += 0.5;
						timeSpent += 0.5;
						timeLeft -= 0.5;
						//timeLeft = getTotalTimeById( currentOrder.getSushi_id() ) - timeSpent;
						
						// Update timeSpent
					    sql = "UPDATE SUSHI_ORDER SET TIME_SPENT=" + timeSpent + " WHERE ID=" + currentOrder.getId();
				        jdbcTemplate.update(sql);
						
				        // Update timeLeft
					    sql = "UPDATE SUSHI_ORDER SET TIME_LEFT=" + timeLeft + " WHERE ID=" + currentOrder.getId();
				        jdbcTemplate.update(sql);
				        
						if( DEBUG == 1 ) { 
							System.out.println(this.getName() + " working with time: " + time );
						}
						if( time >= timeToFinish ){
							System.out.println("Thread: " + this.getName() + " finished order: " + currentOrder.getId() );
						    sql = "UPDATE SUSHI_ORDER SET STATUS_ID=" + ORDER_FINISHED + " WHERE ID=" + currentOrder.getId();
					        jdbcTemplate.update(sql);
							break;
						}
					}
				} catch (InterruptedException e) {
					System.out.println("Thread: " + this.getName() + " interrupted.");
				    sql = "UPDATE SUSHI_ORDER SET TIME_LEFT=" + getTimeToFinish() + " WHERE ID=" + currentOrder.getId();
			        jdbcTemplate.update(sql);
				}
			} else {
				// Wait until order gets here
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.out.println("Should never be in here..." + e);
				}
			}
		}
	}
	
	private int getTotalTimeById( int sushi_id ) {
	    String sql = "SELECT * FROM sushi WHERE id=" + sushi_id;
        List<Sushi> sushi = jdbcTemplate.query( sql, new SushiRowMapper() );
        int timeToMakeRoll = sushi.get(0).getTimeToMake();
        return timeToMakeRoll;
	}

	public int getTimeToFinish(){
		int timeLeft = Math.round( timeToFinish - time );
		return timeLeft;
	}
	
	public void setTimeToFinish( int timeToFinish ){
		this.timeToFinish = timeToFinish;
	}
	
	public void setQueue( Deque<SushiOrder> queue ) {
		jobQueue = queue;
	}
	
	public void setJDBC( JdbcTemplate jdbc ) {
		jdbcTemplate = jdbc;
	}
	
	public void setOrder( SushiOrder order ) {
		currentOrder = order;
	}
	
	public SushiOrder getOrder() {
		return currentOrder;
	}
}