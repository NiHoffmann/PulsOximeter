#include "HeartBeat.h"
#include <DataProcessing/Filter.h>
#include <DataProcessing/PeakFinder.h>
#include <Wire.h>
#include <math.h>

MAX30101 heart_beat_sensor = MAX30101(0x57, Wire);

// Sample ring buffer
float red_filtered[SAMPLE_BUFFER];
float ir_filtered[SAMPLE_BUFFER];

int sample_index = 0;
int samples_filled = 0;

IIRFilter hp_filter_red, lp_filter_red;
IIRFilter hp_filter_ir, lp_filter_ir;

void init_heart_beat_sensor() {
  Wire.setSCL(PB6);
  Wire.setSDA(PB7);
  Wire.setClock(100000);

  Wire.begin();

  while (!heart_beat_sensor.begin())
    delay(100);

  // Heart beat is 1 - 3.3 Hz
  init_highpass(&hp_filter_red, 0.8, SAMPLING_FREQUENCY);
  init_lowpass(&lp_filter_red, 3.5, SAMPLING_FREQUENCY);
  init_highpass(&hp_filter_ir, 0.8, SAMPLING_FREQUENCY);
  init_lowpass(&lp_filter_ir, 3.5, SAMPLING_FREQUENCY);
}

void sample() {
  static bool once = true;

  // 200 samples to get the filter going
  if (once) {
    for (int i = 0; i < SAMPLE_BUFFER; i++) {
      MAX30101Sample sample = heart_beat_sensor.readSample();

      // Store in circular buffer
      red_filtered[sample_index] =
          filter_bandpass(&hp_filter_red, &lp_filter_red, sample.red);
      ir_filtered[sample_index] =
          filter_bandpass(&hp_filter_ir, &lp_filter_ir, sample.ir);
      sample_index = (sample_index + 1) % SAMPLE_BUFFER;

      if (samples_filled < SAMPLE_BUFFER) {
        samples_filled++;
      }
      delay(SAMPLING_PERIOD);
    }
    once = false;
  }

  MAX30101Sample sample = heart_beat_sensor.readSample();

  red_filtered[sample_index] =
      filter_bandpass(&hp_filter_red, &lp_filter_red, sample.red);
  ir_filtered[sample_index] =
      filter_bandpass(&hp_filter_ir, &lp_filter_ir, sample.ir);
  sample_index = (sample_index + 1) % SAMPLE_BUFFER;

  if (samples_filled < SAMPLE_BUFFER) {
    samples_filled++;
  }
}

/**
 *
 * Heart beat.
 *
 */

uint32_t get_heart_beat() {
  static float ordered[SAMPLE_BUFFER];
  static int peak_indices[SAMPLE_BUFFER / 2];

  if (samples_filled < SAMPLE_BUFFER)
    return 0;

  memset(ordered, 0, sizeof(ordered));
  memset(peak_indices, 0, sizeof(peak_indices));

  int start = (sample_index + 1) % SAMPLE_BUFFER;
  for (int i = 0; i < SAMPLE_BUFFER; i++) {
    int idx = (start + i) % SAMPLE_BUFFER;
    ordered[i] = (float)red_filtered[idx];
  }

  size_t num_peaks = 0;
  PeakFinder::findPeaks(ordered, SAMPLE_BUFFER, peak_indices, num_peaks, true,
                        1.0f);

  if (num_peaks < 3)
    return 0;

  size_t filtered_count = 0;
  int last_kept = peak_indices[1];
  peak_indices[filtered_count++] = last_kept;

  for (size_t i = 2; i < num_peaks; i++) {
    int dist = peak_indices[i] - last_kept;
    if (dist >= MIN_DISTANCE) {
      last_kept = peak_indices[i];
      peak_indices[filtered_count++] = last_kept;
    }
  }

#if DEBUG_PEAKS
  for (size_t i = filtered_count; i < num_peaks; i++) {
    peak_indices[i] = 0;
  }
#endif

  if (filtered_count < 2)
    return 0;

  float avg_interval_samples = 0.0f;
  for (size_t i = 1; i < filtered_count; i++) {
    avg_interval_samples += (float)(peak_indices[i] - peak_indices[i - 1]);
  }
  avg_interval_samples /= (filtered_count - 1);

  float avg_interval_sec = avg_interval_samples * (SAMPLING_PERIOD / 1000.0f);

  uint32_t bpm = (uint32_t)(60.0f / avg_interval_sec);

  if (bpm < 60 || bpm > 150)
    return 0;

  return bpm;
}

/**
 *
 * SpO2.
 *
 * Formula:
 *
 * ratio = (AC_RED / DC_RED) / (AC_IR / DC_IR)
 * SpO2 = Basevalue - ratio * callibration_contant
 *
 * see:
 * https://forum.arduino.cc/t/afe4403-spo2-calculation/458889
 *
 * We use calculations and callibrations as described here:
 * https://www.analog.com/en/resources/technical-articles/guidelines-for-spo2-measurement--maxim-integrated.html
 *
 */

float dc_part(float *samples) {
  float sum = 0.0f;
  for (int i = 0; i < SAMPLE_BUFFER; i++) {
    sum += samples[i];
  }
  return sum / SAMPLE_BUFFER;
}

// RMS
float ac_part(float *samples) {
  float dc = dc_part(samples);
  double ac_sum = 0.0f;
  float x = 0;
  for (int i = 0; i < SAMPLE_BUFFER; i++) {
    x = samples[i] - dc;
    ac_sum += x * x;
  }
  return sqrt(ac_sum / SAMPLE_BUFFER);
}

float calculate_ratio_of_ratios() {
  float ac_red = ac_part(red_filtered);
  float dc_red = dc_part(red_filtered);

  float ac_ir = ac_part(ir_filtered);
  float dc_ir = dc_part(ir_filtered);

  if (dc_red == 0 || dc_ir == 0)
    return 0.0f;

  float ratio = (ac_red / dc_red) / (ac_ir / dc_ir);
  return ratio;
}

/**
 *
 *
 * Producer provided callibrations.
 *
 */
const float a = 1.5958422;
const float b = -34.6596622;
const float c = 112.6898759;
float ratio_to_spo2(float ratio) {
  float spo2 = (a * ratio * ratio) + (b * ratio) + c;

  if (spo2 > 100.0f)
    spo2 = 100.0f;
  if (spo2 < 0.0f)
    spo2 = 0.0f;

  return spo2;
}

uint32_t get_o2() {
  float ratio = calculate_ratio_of_ratios();
  return ratio_to_spo2(ratio);
}
