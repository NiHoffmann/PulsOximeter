#ifndef FILTER_H
#define FILTER_H

typedef struct {
  double a[3];
  double b[3];
  double x[2];
  double y[2];
} IIRFilter;

void init_highpass(IIRFilter *f, double cutoff, double fs);
void init_lowpass(IIRFilter *f, double cutoff, double fs);

double filter_sample(IIRFilter *f, double xn);
double filter_bandpass(IIRFilter *hp, IIRFilter *lp, double xn);

#endif
