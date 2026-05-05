#ifndef PEAKFINDER_H
#define PEAKFINDER_H

#include "Devices/HeartBeat.h"
#include <cmath>
#include <cstddef>
#include <cstdint>

/**
 * Found at : https://github.com/claydergc/find-peaks/tree/master
 * and refactored for embedded, static allocation etc.
 * this was done using ai (converting from vect, dynamic to static allocations)
 *
 */

namespace PeakFinder {
constexpr float EPS = 2.2204e-16f;
constexpr size_t MAX_SAMPLES = SAMPLE_BUFFER;
constexpr size_t MAX_PEAKS = MAX_SAMPLES / 2;

/*
    Inputs
    x0: input signal
    extrema: 1 if maxima are desired, -1 if minima are desired
    includeEndpoints - If true the endpoints will be included as possible
   extrema otherwise they will not be included Output peakInds: Indices of peaks
   in x0
*/
void findPeaks(const float *x0, size_t N, int *peakInds, size_t &numPeaks,
               bool includeEndpoints = true, float extrema = 1.0f);

} // namespace PeakFinder

#endif
