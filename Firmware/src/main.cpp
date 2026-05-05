#include "Devices/BLE.h"
#include "Devices/HeartBeat.h"
#include <Arduino.h>

#define BLE_INTERVAL 1000

unsigned long last_ble_time = 0;
unsigned long last_sample_time = 0;

#define MOVING_AVERAGE_SCALE 0.3f

char payload[MAX_PAYLOAD_SIZE];

void ble_routine() {
  static float co2_mov_avg = 0;
  static bool first_run = true;

  if (!rn487xBle.getConnectionStatus()) {
    return;
  }

  uint32_t bpm_raw = get_heart_beat();
  uint32_t co2_raw = get_o2();

  uint8_t bpm_capped = (bpm_raw > 255) ? 255 : (uint8_t)bpm_raw;
  uint8_t co2_capped = (co2_raw > 255) ? 255 : (uint8_t)co2_raw;

  if (first_run) {
    co2_mov_avg = co2_capped;
    first_run = false;
  } else {
    co2_mov_avg = MOVING_AVERAGE_SCALE * co2_capped +
                  (1 - MOVING_AVERAGE_SCALE) * co2_mov_avg;
  }

  // round up
  uint8_t co2 = (uint8_t)(co2_mov_avg + 0.5f);

  snprintf(payload, MAX_PAYLOAD_SIZE, "%02X%02X\0", bpm_capped, co2);
  transfer_data(payload);
}

void setup() {
  init_heart_beat_sensor();
  init_ble();
}

void loop() {
  unsigned long current_millis = millis();

  if (current_millis - last_sample_time >= SAMPLING_PERIOD) {
    sample();
    last_sample_time += SAMPLING_PERIOD;
  }

  if (current_millis - last_ble_time >= BLE_INTERVAL) {
    ble_routine();
    last_ble_time += BLE_INTERVAL;
  }
}