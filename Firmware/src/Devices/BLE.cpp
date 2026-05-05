#include "BLE.h"
#include <Arduino.h>
#include <RN487x_BLE.h>

HardwareSerial ble_serial(LPUART1);
const char *DEVICE_NAME = "Pulsoxymeter";
const char *SERVICE_UUID = "AD11CF40063F11E5BE3E0002A5D5C51B";
const char *CHARACT_UUID = "BF3FBD80063F11E59E690002A5D5C501";

void init_ble() {
  // init
  delay(500);
  pinMode(PA5, OUTPUT);
  digitalWrite(PA5, HIGH);

  ble_serial.setTx(PB10);
  ble_serial.setRx(PB11);
  ble_serial.setRts(-1);
  ble_serial.setCts(-1);

  ble_serial.begin(rn487xBle.getDefaultBaudRate());
  rn487xBle.initBleStream(&ble_serial);

  while (!rn487xBle.swInit())
    delay(100);

  // register services
  rn487xBle.enterCommandMode();
  rn487xBle.clearAllServices();
  rn487xBle.setSerializedName(DEVICE_NAME);
  rn487xBle.setDevName(DEVICE_NAME);
  rn487xBle.setDefaultServices(DEVICE_INFO_SERVICE);
  rn487xBle.setServiceUUID(SERVICE_UUID);
  rn487xBle.setCharactUUID(CHARACT_UUID, READ_PROPERTY, MAX_PAYLOAD_SIZE);
  rn487xBle.reboot();

  // start advertising
  rn487xBle.enterCommandMode();
  rn487xBle.startCustomAdvertising(210);
}

void transfer_data(char *payload) {
  rn487xBle.writeLocalCharacteristic(0x72, payload);
}
