package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.parkit.parkingsystem.constants.ParkingType.BIKE;
import static com.parkit.parkingsystem.constants.ParkingType.CAR;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;



   @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    } 
    

    @Test
    public void processExitingVehicleTest(){
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");     
        when(ticketDAO.getTicketWithOutTimeNull("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);

        
        parkingService.processExitingVehicle();
        
        assertThat(ticket.getPrice()).isGreaterThanOrEqualTo(0);
        ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        verify(parkingSpotDAO ,  Mockito.times(1)).updateParking(parkingSpotCaptor.capture());
        ParkingSpot updatedParkingSpot = parkingSpotCaptor.getValue();
        assertTrue(updatedParkingSpot.isAvailable());
        assertEquals(1,updatedParkingSpot.getId());
    }
    
    @SuppressWarnings("unused")
	@Test
    public void processIncomingCarTest() throws Exception {
    	
        when(inputReaderUtil.readSelection()).thenReturn(1);

        when(parkingSpotDAO.getNextAvailableSlot(CAR)).thenReturn(1);
        parkingService.processIncomingVehicle();

        ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        verify(parkingSpotDAO).updateParking(parkingSpotCaptor.capture());
        ParkingSpot updatedParkingSpot = parkingSpotCaptor.getValue();
        assertFalse(updatedParkingSpot.isAvailable());
        assertEquals(CAR,updatedParkingSpot.getParkingType());
        assertEquals(1,updatedParkingSpot.getId());

        ArgumentCaptor<Ticket> saveTicketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO).saveTicket(saveTicketCaptor.capture());
        Ticket saveTicket = saveTicketCaptor.getValue();
        assertNotNull(saveTicket.getInTime());
        assertEquals("ABCDEF",saveTicket.getVehicleRegNumber());
        assertEquals(1,saveTicket.getParkingSpot().getId());
        assertEquals(0,saveTicket.getPrice());
        assertNull(saveTicket.getOutTime());

    }

    @Test
    public void processIncomingBikeTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(2);

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(4);

        parkingService.processIncomingVehicle();

        ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        verify(parkingSpotDAO).updateParking(parkingSpotCaptor.capture());
        ParkingSpot updatedParkingSpot = parkingSpotCaptor.getValue();
        assertThat(updatedParkingSpot.isAvailable()).isFalse();
        assertThat(updatedParkingSpot.getParkingType()).isEqualTo(ParkingType.BIKE);
        assertThat(updatedParkingSpot.getId()).isEqualTo(4);

        ArgumentCaptor<Ticket> saveTicketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO).saveTicket(saveTicketCaptor.capture());
        Ticket saveTicket = saveTicketCaptor.getValue();
        assertNotNull(saveTicket.getInTime());
        assertEquals("ABCDEF",saveTicket.getVehicleRegNumber());
        assertEquals(4,saveTicket.getParkingSpot().getId());
        assertEquals(0,saveTicket.getPrice());
        assertNull(saveTicket.getOutTime());

    }

    @Test
    public void processExitingCarTest() {
    	 Ticket ticket = new Ticket();
         ticket.setVehicleRegNumber("ABCDEF");
         ParkingSpot parkingSpot = new ParkingSpot(1, CAR, false);
         ticket.setParkingSpot(parkingSpot);
         ticket.setInTime(new Date(new Date().getTime() - 3600000));

         when(ticketDAO.getTicketWithOutTimeNull("ABCDEF")).thenReturn(ticket);
         when(ticketDAO.recurrentUsers("ABCDEF")).thenReturn(true);
         when(ticketDAO.updateTicket(ticket)).thenReturn(true);
         when(parkingSpotDAO.updateParking(ticket.getParkingSpot())).thenReturn(true);

         parkingService.processExitingVehicle();

        
         
         ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
         verify(parkingSpotDAO).updateParking(parkingSpotCaptor.capture());
         ParkingSpot updatedParkingSpot = parkingSpotCaptor.getValue();
         assertTrue(updatedParkingSpot.isAvailable());

         ArgumentCaptor<Ticket> saveTicketCaptor = ArgumentCaptor.forClass(Ticket.class);
         verify(ticketDAO).updateTicket(saveTicketCaptor.capture());
         Ticket saveTicket = saveTicketCaptor.getValue();
         assertNotNull(saveTicket.getInTime());
         assertEquals("ABCDEF",saveTicket.getVehicleRegNumber());
         assertThat(saveTicket.getPrice()).isGreaterThanOrEqualTo(0);
         assertNotNull(saveTicket.getOutTime());

    }

    @Test
    public void processExitingBikeTest() {
    	Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABCDEF");
        ParkingSpot parkingSpot = new ParkingSpot(4, BIKE, false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date(new Date().getTime() - 3600000));

        when(ticketDAO.getTicketWithOutTimeNull("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.recurrentUsers("ABCDEF")).thenReturn(true);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);
        when(parkingSpotDAO.updateParking(ticket.getParkingSpot())).thenReturn(true);

        parkingService.processExitingVehicle();

       
        
        ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        verify(parkingSpotDAO).updateParking(parkingSpotCaptor.capture());
        ParkingSpot updatedParkingSpot = parkingSpotCaptor.getValue();
        assertTrue(updatedParkingSpot.isAvailable());

        ArgumentCaptor<Ticket> saveTicketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO).updateTicket(saveTicketCaptor.capture());
        Ticket saveTicket = saveTicketCaptor.getValue();
        assertNotNull(saveTicket.getInTime());
        assertEquals("ABCDEF",saveTicket.getVehicleRegNumber());
        assertThat(saveTicket.getPrice()).isGreaterThanOrEqualTo(0);
        assertNotNull(saveTicket.getOutTime());

        
        }
    
   
    @Test
    public void processIncomingCarWithAnExistingVehicleNumberTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); 

        when(parkingSpotDAO.getNextAvailableSlot(CAR)).thenReturn(1);
        when(ticketDAO.alreadyAtParking("ABCDEF")).thenReturn(true); 
        parkingService.processIncomingVehicle();
        verify(parkingSpotDAO, times(0)).updateParking(any());
        verify(ticketDAO, times(0)).saveTicket(any());


    }
    
  
    
}
