#include "DHT.h"
#include <U8x8lib.h>
#define DHTPIN A2
#define DHTTYPE DHT11   // DHT 11
#define REDLED 4
#define BUZZER 5

DHT dht(DHTPIN, DHTTYPE);
boolean counter = true;
auto display = U8X8_SSD1306_128X64_NONAME_HW_I2C(U8X8_PIN_NONE);
void setup() {
  Serial.begin(9600);
  dht.begin();
 display.begin();
 display.setFlipMode(0);
 display.clearDisplay();
 display.setFont(u8x8_font_profont29_2x3_r); // set font
 pinMode(REDLED, OUTPUT);
 pinMode(BUZZER, OUTPUT);
}
void loop() {
  
  delay(500);
  display.clear();
  
  byte h = dht.readHumidity();
  byte t = dht.readTemperature();
  int ha = dht.readHumidity();
  int ta = dht.readTemperature();
  const auto receivedData = Serial.read();

  //Inputs
  if (receivedData == 1){ 
    digitalWrite(REDLED,HIGH);
    display.setCursor(0,10);
    display.print("ON");}
  if (receivedData == 2){ 
    digitalWrite(REDLED,LOW);
    display.setCursor(0,10);
    display.print("OFF");}
  if (receivedData == 3){ 
    digitalWrite(BUZZER,HIGH);
    delay(2000);
     digitalWrite(BUZZER,LOW);
  }
  
  //Outputs
  if (isnan(h) || isnan(t)) {return;}
  display.print(ta);
  display.setCursor(0,5);
  display.print(ha);


  if(counter){
    Serial.write(ta); 
    counter = false;
  } else {
    Serial.write(ha); 
    counter = true;
  }
  
  
}
