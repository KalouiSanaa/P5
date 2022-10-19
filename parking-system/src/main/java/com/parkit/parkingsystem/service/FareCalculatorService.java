package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	private static final int HOURMILLISECONDS = 60 * 60 * 1000;
    private static final double HALF_HOUR = 0.5;
    
    public void calculateFare(Ticket ticket,boolean Recurrent){
    	
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

       long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
       long duration = outHour - inHour;
       
       double  TIME= duration /HOURMILLISECONDS;
       //Recurrent = false;
       if(TIME<HALF_HOUR) {
              
    	   TIME=0;
	
                 }

       if(Recurrent == true) 
          {
	TIME = (long) ((TIME) - (0.05*TIME));
	 
                } 
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                double price =TIME * Fare.CAR_RATE_PER_HOUR;
               ticket.setPrice( Math.round((price)*100.0)/100.0);
              ticket.setPrice( Math.round((price)*100.0)/100.0);
          
            	     break;  
   
            }
            case BIKE: {
                double price =TIME * Fare.BIKE_RATE_PER_HOUR;
                ticket.setPrice( Math.round((price)*100.0)/100.0);
        
           	     break;  
            }
      
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}