package com.example.demo;

import java.sql.SQLException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.json.*;

@RestController // related to RequestMapping

@RequestMapping("/api")

@SpringBootApplication
public class SpringBootH2Application {
	@Autowired
    JdbcTemplate jdbcTemplate;
	Deque<SushiOrder> queue = new ConcurrentLinkedDeque<SushiOrder>(); // Order queue
	printThread chef1;
	printThread chef2;
	printThread chef3;
	int initialized = 0;
	
	final int CREATED = 1;
	final int IN_PROGRESS = 2;
	final int PAUSED = 3;
	final int FINISHED = 4;
	final int CANCELLED = 5;
	
	@PostMapping("/orders")
	String newOrder(@RequestBody PostRequestBody body) {
		if( initialized == 0 ) { 
			initialized = 1;
			startThreads();
		}
		System.out.println("Post method ORDERS: body - " + body.toString());
		
		// Query STATUS Table for 'created'
		String sql = "SELECT * FROM status WHERE id=1";
        List<Status> status = jdbcTemplate.query( sql, new StatusRowMapper());
	    System.out.println(status);
	    
	    // Query SUSHI Table for 'requested roll'
	    System.out.print( "THE NAME: " + body.getSushi_name() );

		sql = "SELECT * FROM sushi WHERE name='" + body.getSushi_name() + "'";
        List<Sushi> sushi = jdbcTemplate.query( sql, new SushiRowMapper() );
	    System.out.println(sushi);
		
	    // Insert into SUSHI_ORDER table
	    sql = "INSERT INTO sushi_order ( status_id, sushi_id, time_left ) VALUES (" + 
	    		status.get(0).getId() + ", " + sushi.get(0).getId() + ", " + sushi.get(0).getTimeToMake() + ")";
        int result = jdbcTemplate.update(sql);
        
        // Push order into the queue
		sql = "SELECT TOP 1 * FROM sushi_order ORDER BY createdat DESC";
        List<SushiOrder> topOrder = jdbcTemplate.query( sql, new SushiOrderRowMapper() );
	    queue.add( topOrder.get(0) );
        
        String response;
        if (result > 0) {
            System.out.println("Order taken successfully. " + body.toString() );
            response = "Response: 201\n\n\t\"order\": {\n\t\t\"id\": " + topOrder.get(0).getId() + " ,\n\t\t\"statusId\": " + status.get(0).getId() + 
            		",\n\t\t\"sushiId\": " + sushi.get(0).getId() + ",\n\t\t\"createdAt\": " + topOrder.get(0).getCreatedAt() + "\n\t},\n\t\"code\": 0,\n\t\"msg: \"Order created\"\n"; 
        } else {
            System.out.println("Order failed. " + body.toString() );
            response = "Response: 201\n\n\t\"code\": 1\n\t\"msg\": \"Order failed.\""; 
        }
	    
        return response;
	}
	
	@DeleteMapping("/orders/{order_id}")
	String cancelOrder( @PathVariable Integer order_id ) {
		System.out.println("Delete method called");
		System.out.println("Id received: " + order_id);
	    
	    // Get order
	    String sql = "SELECT * FROM sushi_order WHERE id=" + order_id;
        List<SushiOrder> order = jdbcTemplate.query( sql, new SushiOrderRowMapper() );
        
        String response;
        int result;
        if( order.size() != 0 && order.get(0).getStatus_id() == CREATED ) {
        	// Set order to cancelled
    	    sql = "UPDATE SUSHI_ORDER SET STATUS_ID=" + CANCELLED + " WHERE ID=" + order_id;
            result = jdbcTemplate.update(sql);
            
            // Remove from queue
            Iterator<SushiOrder> itr = queue.iterator();
            while (itr.hasNext())
            {
                if( itr.next().getId() == order_id ) {
                	itr.remove();
                }
            }
            
            System.out.println("Deleted successfully.");
            response = "Response: 200\n{\n\t\"code\": 0,\n\t\"msg\": \"Order " + order_id + " cancelled\"\n}\n";
        } else if( order.size() != 0 && order.get(0).getStatus_id() == IN_PROGRESS ) {
        	// Interrupt the chef (updates time as well)
	        if( chef1.getOrder().getId() == order_id ) {
	        	chef1.interrupt();
	        } else if ( chef2.getOrder().getId() == order_id ) {
	        	chef2.interrupt();
	        } else {
	        	chef3.interrupt();
	        }
	        
	        // Set order to cancelled
    	    sql = "UPDATE SUSHI_ORDER SET STATUS_ID=" + CANCELLED + " WHERE ID=" + order_id;
            result = jdbcTemplate.update(sql);
            
            System.out.println("Deleted successfully.");
            response = "Response: 200\n{\n\t\"code\": 0,\n\t\"msg\": \"Order " + order_id + " cancelled\"\n}\n";
        } else if( order.size() != 0 && order.get(0).getStatus_id() == PAUSED ) {
	        // Set order to cancelled
    	    sql = "UPDATE SUSHI_ORDER SET STATUS_ID=" + CANCELLED + " WHERE ID=" + order_id;
            result = jdbcTemplate.update(sql);
            
            System.out.println("Deleted successfully.");
            response = "Response: 200\n{\n\t\"code\": 0,\n\t\"msg\": \"Order " + order_id + " cancelled\"\n}\n";
        } else {
        	System.out.println("Deleted failed.");
            response = "Response: 200\n{\n\t\"code\": 1,\n\t\"msg\": \"Order " + order_id + " Id not found, already cancelled or finished.\"\n}\n";
        }

        return response;
	}
	
	@PutMapping("/orders/{order_id}/pause")
	String pauseOrder( @PathVariable Integer order_id ) {
        // Check if order is "in-progress"
		int STATUS_IN_PROGRESS = 2;
	    String sql = "SELECT * FROM sushi_order WHERE id=" + order_id + " AND status_id=" + STATUS_IN_PROGRESS;
        List<SushiOrder> orders = jdbcTemplate.query( sql, new SushiOrderRowMapper() );
	    System.out.println(orders);
	    
		String response;
	    if( !orders.isEmpty() ) {
	    	// Grab paused id (could just use hardcoding)
		    sql = "SELECT * FROM status WHERE id=3";
	        List<Status> status = jdbcTemplate.query( sql, new StatusRowMapper() );
		    System.out.println(status);
			
		    // Update order status to paused
		    sql = "UPDATE SUSHI_ORDER SET STATUS_ID=" + status.get(0).getId() + " WHERE ID=" + order_id;
	        int result = jdbcTemplate.update(sql);
	        
	        // Interrupt the chef
	        if( chef1.getOrder().getId() == order_id ) {
	        	chef1.interrupt();
	        } else if ( chef2.getOrder().getId() == order_id ) {
	        	chef2.interrupt();
	        } else {
	        	chef3.interrupt();
	        }
	        
	        if( result > 0 ) {
	        	System.out.println("Paused successfully.");
	            response = "Response: 200\n{\n\t\"code\": 0,\n\t\"msg\": \"Order " + order_id + " paused\"\n}\n";
	        } else {
	        	System.out.println("Could not update database to paused.");
	            response = "Response: 200\n{\n\t\"code\": 0,\n\t\"msg\": \"Order " + order_id + " not paused\"\n}\n";
	        }
            
	    } else {
            System.out.println("Paused failed.");
            response = "Response: 200\n{\n\t\"code\": 1,\n\t\"msg\": \"Order " + order_id + " Id not found or not in-progress\"\n}\n";
	    }
	    return response;    
	}
	
	@PutMapping("/orders/{order_id}/resume")
	String resumeOrder( @PathVariable Integer order_id ) {
		// Get the order from db
	    String sql = "SELECT * FROM sushi_order WHERE id=" + order_id;
        List<SushiOrder> orders = jdbcTemplate.query( sql, new SushiOrderRowMapper() );
	    System.out.println(orders);
	    
	    String response;
	    if( orders.size() != 0 && orders.get(0).getStatus_id() == PAUSED ) {
	    	// Push to top of queue
		    queue.addFirst( orders.get(0) );
		    
	    	System.out.println("Resumed successfully.");
	    	response = "Response: 200\n{\n\t\"code\": 0,\n\t\"msg\": \"Order " + order_id + " resumed\"\n}\n";
	    } else {
	    	System.out.println("Resume failed.");
	    	response = "Response: 200\n{\n\t\"code\": 0,\n\t\"msg\": \"Order " + order_id + " resume failed order not found or not under paused status.\"\n}\n";
	    }
	    return response;
	}
	
	@GetMapping("/orders/status")
	String printStatus() {
	    String sql = "SELECT * FROM sushi_order";
        List<SushiOrder> orders = jdbcTemplate.query( sql, new SushiOrderRowMapper() );
	    System.out.println(orders);
	    
	    String labels[] = {"in-progress", "created", "paused", "cancelled", "completed"};
	    int stats[] = {2, 1, 3, 5, 4};
	    
	    int count = 0;
	    JsonArrayBuilder jsonObjInner = Json.createArrayBuilder();
	    JsonObjectBuilder jsonObjInnerMost = Json.createObjectBuilder();
	    JsonObjectBuilder jsonObjOuter = Json.createObjectBuilder();
	    for (int status : stats) {
	        for (SushiOrder order : orders) {
	        	if( status == order.getStatus_id() ) {
	        		
	        		jsonObjInnerMost.add( "orderId", String.valueOf( order.getId()) );
	        		jsonObjInnerMost.add( "timeSpent", String.valueOf( order.getTime_spent() ));
	        		jsonObjInner.add( jsonObjInnerMost );
	        		jsonObjInnerMost = Json.createObjectBuilder();
	        	}
	        }
        	jsonObjOuter.add( labels[count], jsonObjInner.build() );
        	jsonObjInner = Json.createArrayBuilder();
	        count++;
	    }
	    return jsonObjOuter.build().toString();
	}
	
	@RequestMapping("/")
	String basicURL() {
		return "Welcome to Spring Boot Hello World Tutorial";
	}
	
	String startThreads() {        
    	ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
    	
    	chef1 = (printThread) ctx.getBean("printThread");
    	chef1.setName("Thread 1");
    	chef1.setQueue( queue );
    	chef1.setJDBC( jdbcTemplate );
    	chef1.setOrder(new SushiOrder(-1337));
    	
    	chef2 = (printThread) ctx.getBean("printThread");
    	chef2.setName("Thread 2");
    	chef2.setQueue( queue );
    	chef2.setJDBC( jdbcTemplate );
    	chef2.setOrder(new SushiOrder(-1337));
    	
    	chef3 = (printThread) ctx.getBean("printThread");
    	chef3.setName("Thread 3");
    	chef3.setQueue( queue );
    	chef3.setJDBC( jdbcTemplate );
    	chef3.setOrder(new SushiOrder(-1337));
    	
    	chef1.start();
    	chef2.start();
    	chef3.start();
		
		return "Hello Crunchify Friends! This is your first SpringBoot Example. Isn't that so Simple?";
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(SpringBootH2Application.class, args);
	}

}
