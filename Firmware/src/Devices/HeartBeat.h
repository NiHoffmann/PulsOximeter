#ifndef HEART_BEAT_H
#define HEART_BEAT_H
#include "MAX30101.h"

#define SAMPLE_BUFFER 300     // ~10 seconds at 50 Hz
#define SAMPLING_PERIOD 20    // ms per sample (~50 Hz)
#define SAMPLING_FREQUENCY 50 // Sampling frequency in Hz

/* Minimum samples between beats (~0.4 sec), about 150 bpm,
 * too many false peaks detected at 200 bpm max sadly.
 * good enough for hobby sport, insuficient for extreme sport!
 */
#define MIN_DISTANCE (int)(0.4 * SAMPLING_FREQUENCY)

void sample();
void init_heart_beat_sensor();
uint32_t get_heart_beat();
uint32_t get_o2();

#endif