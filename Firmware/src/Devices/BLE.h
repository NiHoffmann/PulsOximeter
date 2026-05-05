#ifndef BLE_H
#define BLE_H
#include "RN487x_BLE.h"

#define MAX_PAYLOAD_SIZE (20)

void init_ble();
void transfer_data(char *payload);

#endif