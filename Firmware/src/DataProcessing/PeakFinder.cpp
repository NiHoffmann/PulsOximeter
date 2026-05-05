#include "PeakFinder.h"
#include <cstring> // for memset

namespace PeakFinder {

static void diff(const float *in, float *out, size_t N) {
  for (size_t i = 1; i < N; i++)
    out[i - 1] = in[i] - in[i - 1];
}

static void vectorElementsProduct(const float *a, const float *b, float *out,
                                  size_t N) {
  for (size_t i = 0; i < N; i++)
    out[i] = a[i] * b[i];
}

static void scalarProduct(float scalar, float *x, size_t N) {
  for (size_t i = 0; i < N; i++)
    x[i] *= scalar;
}

static size_t minIndex(const float *x, size_t N) {
  size_t idx = 0;
  for (size_t i = 1; i < N; i++)
    if (x[i] < x[idx])
      idx = i;
  return idx;
}

static size_t maxIndex(const float *x, size_t N) {
  size_t idx = 0;
  for (size_t i = 1; i < N; i++)
    if (x[i] > x[idx])
      idx = i;
  return idx;
}

void findPeaks(const float *x0, size_t N, int *peakInds, size_t &numPeaks,
               bool includeEndpoints, float extrema) {
  numPeaks = 0;
  if (N < 3)
    return;

  static float x[MAX_SAMPLES];
  static float dx[MAX_SAMPLES - 1];
  static float dxProd[MAX_SAMPLES - 2];
  static int ind[MAX_SAMPLES];
  static int peakLoc[MAX_PEAKS];
  static float peakMag[MAX_PEAKS];

  memset(x, 0, sizeof(x));
  memset(dx, 0, sizeof(dx));
  memset(dxProd, 0, sizeof(dxProd));
  memset(ind, 0, sizeof(ind));
  memset(peakLoc, 0, sizeof(peakLoc));
  memset(peakMag, 0, sizeof(peakMag));

  for (size_t i = 0; i < N; i++)
    x[i] = x0[i];

  scalarProduct(extrema, x, N);

  diff(x, dx, N);

  for (size_t i = 0; i < N - 1; i++)
    if (dx[i] == 0.0f)
      dx[i] = -EPS;

  vectorElementsProduct(dx, dx + 1, dxProd, N - 2);

  size_t indCount = 0;
  for (size_t i = 0; i < N - 2; i++)
    if (dxProd[i] < 0)
      ind[indCount++] = i + 1;

  float sel = (x[maxIndex(x, N)] - x[minIndex(x, N)]) / 4.0f;

  float leftMin = x[minIndex(x, N)];
  float minMag = leftMin;

  size_t ii = (x[0] >= x[1]) ? 0 : 1;
  bool foundPeak = false;
  float tempMag = minMag;
  int tempLoc = 0;
  size_t cInd = 0;

  while (ii < N) {
    ii++;
    if (ii == N)
      break;

    if (foundPeak) {
      tempMag = minMag;
      foundPeak = false;
    }

    if (x[ii - 1] > tempMag && x[ii - 1] > leftMin + sel) {
      tempLoc = ii - 1;
      tempMag = x[ii - 1];
    }

    ii++;
    if (ii == N)
      break;

    if (!foundPeak && tempMag > sel + x[ii - 1]) {
      foundPeak = true;
      leftMin = x[ii - 1];
      peakLoc[cInd] = tempLoc;
      peakMag[cInd] = tempMag;
      cInd++;
      if (cInd >= MAX_PEAKS)
        break;
    } else if (x[ii - 1] < leftMin) {
      leftMin = x[ii - 1];
    }
  }

  for (size_t i = 0; i < cInd; i++)
    peakInds[i] = peakLoc[i];

  numPeaks = cInd;
}

} // namespace PeakFinder
