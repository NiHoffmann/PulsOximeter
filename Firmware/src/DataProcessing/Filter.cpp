#include "Filter.h"
#include <math.h>

void init_highpass(IIRFilter *f, double cutoff, double fs) {
  double w0 = 2 * M_PI * cutoff / fs;
  double cosw0 = cos(w0);
  double sinw0 = sin(w0);
  double Q = 1.0;

  double alpha = sinw0 / (2 * Q);

  double b0 = (1 + cosw0) / 2;
  double b1 = -(1 + cosw0);
  double b2 = (1 + cosw0) / 2;
  double a0 = 1 + alpha;
  double a1 = -2 * cosw0;
  double a2 = 1 - alpha;

  f->b[0] = b0 / a0;
  f->b[1] = b1 / a0;
  f->b[2] = b2 / a0;
  f->a[0] = 1.0;
  f->a[1] = a1 / a0;
  f->a[2] = a2 / a0;

  f->x[0] = f->x[1] = 0.0;
  f->y[0] = f->y[1] = 0.0;
}

void init_lowpass(IIRFilter *f, double cutoff, double fs) {
  double w0 = 2 * M_PI * cutoff / fs;
  double cosw0 = cos(w0);
  double sinw0 = sin(w0);
  double Q = 1.0;

  double alpha = sinw0 / (2 * Q);

  double b0 = (1 - cosw0) / 2;
  double b1 = 1 - cosw0;
  double b2 = (1 - cosw0) / 2;
  double a0 = 1 + alpha;
  double a1 = -2 * cosw0;
  double a2 = 1 - alpha;

  f->b[0] = b0 / a0;
  f->b[1] = b1 / a0;
  f->b[2] = b2 / a0;
  f->a[0] = 1.0;
  f->a[1] = a1 / a0;
  f->a[2] = a2 / a0;

  f->x[0] = f->x[1] = 0.0;
  f->y[0] = f->y[1] = 0.0;
}

double filter_sample(IIRFilter *f, double xn) {
  double yn = f->b[0] * xn + f->b[1] * f->x[0] + f->b[2] * f->x[1] -
              f->a[1] * f->y[0] - f->a[2] * f->y[1];
  f->x[1] = f->x[0];
  f->x[0] = xn;
  f->y[1] = f->y[0];
  f->y[0] = yn;
  return yn;
}

double filter_bandpass(IIRFilter *hp, IIRFilter *lp, double xn) {
  double y = filter_sample(hp, xn);
  y = filter_sample(lp, y);
  return y;
}
