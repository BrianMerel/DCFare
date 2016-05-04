# DCFare

This is a WMATA Bus/Rail Tracking application. Rail and Bus information is gathered from https://developer.wmata.com/.

There API provides json info
    Bus:
      Route
      Schedule
      Current Position
      Sign Header
      
    Rail:
      Schedule
      Location
      
    Incidents:
      Rail 
      Bus
      Elevator
      
  Currently data is fetched from the developer.wmata.com but because of rate limiting, a central server that receives relevant data
  and forwards it to clients is significant. 
  
  Google maps is used for mapping. 
  
  5/3/2016
  Bus tracking is working and stops are displayed correctly. Most routes are displayed correctly but some display polylines incorrectly.
  
  UP Next
    -Fix route polylines
    -Work on central server downloading and relaying info
    -Refine timertask/threading logic
      
  
