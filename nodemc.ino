/*
 *  This sketch demonstrates how to set up a simple HTTP-like server.
 *  The server will set a GPIO pin depending on the request
 *    http://server_ip/led/0 will set the GPIO2 low,
 *    http://server_ip/led/1 will set the GPIO2 high
 *  server_ip is the IP address of the ESP8266 module, will be 
 *  printed to Serial when the module is connected.
 */

#include <ESP8266WiFi.h>
#include <EEPROM.h>

const char* ssid = "yourwifiname";
const char* password = "yourwifipassword";

// Create an instance of the server
// specify the port to listen on as an argument
WiFiServer server(80);


void setup() {
  Serial.begin(115200);
  delay(10);
  int eeAddress = 0;
  // prepare GPIO2
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, 0);
  
  // Connect to WiFi network
  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
  
  // Start the server
  server.begin();
  Serial.println("Server started");

  // Print the IP address
  Serial.println(WiFi.localIP());
}

void loop() {
  // Check if a client has connected
  WiFiClient client = server.available();
  if (!client) {
    return;
  }
  
  // Wait until the client sends some data
  Serial.println("new client");
  while(!client.available()){
    delay(1);
  }
  
  // Read the first line of the request
  String req = client.readStringUntil('\r');
  String fin = req.substring(9,26);
  Serial.println(fin);
  Serial.println(fin.length()); 
   
  client.flush();
  
  // Match the request
  int val;
  if (fin.length()==17)  // led=on
    {val = 1;//CODE for mac address incoming (or) when phone call comes
     
    }
  else 
    val = 0;//CODE when phone call ends
//  else {
//    Serial.println("invalid request");
//    client.stop();
//    return;
//  }

  // Set GPIO2 according to the request
  digitalWrite(LED_BUILTIN, val);
  
  client.flush();

   // Return the response
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html");
  client.println(""); //  do not forget this one
  client.print("Led pin is now: ");
 
  if(val == 1) {
    client.print("On");
  } else {
    client.print("Off");
  }
  Serial.println("Client disonnected");

  // The client will actually be disconnected 
  // when the function returns and 'client' object is detroyed
}
