/*
 * Copyright (C) 2024, 2025, Yasumasa Suenaga
 *
 * This file is part of nativebinder.
 *
 * nativebinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * nativebinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with nativebinder. If not, see <http://www.gnu.org/licenses/>.
 */
#include <stdio.h>

#ifdef __linux__
#include <errno.h>
#endif

void intManyArgs(char a1,
                 char a2,
                 short a3,
                 short a4,
                 int a5,
                 long a6,
                 char a7,
                 char a8,
                 short a9,
                 short a10,
                 int a11,
                 long a12){
  printf("intManyArgs:\n");
  printf("   a1 = %hhd\n", a1);
  printf("   a2 = %hhd\n", a2);
  printf("   a3 = %hd\n", a3);
  printf("   a4 = %hd\n", a4);
  printf("   a5 = %d\n", a5);
  printf("   a6 = %ld\n", a6);
  printf("   a7 = %hhd\n", a7);
  printf("   a8 = %hhd\n", a8);
  printf("   a9 = %hd\n", a9);
  printf("  a10 = %hd\n", a10);
  printf("  a11 = %d\n", a11);
  printf("  a12 = %ld\n", a12);
  printf("\n");

#ifdef __linux__
  errno = 100;
#endif
}

void fpManyArgs(float a1,
                double a2,
                float a3,
                double a4,
                float a5,
                double a6,
                float a7,
                double a8,
                float a9,
                double a10,
                float a11,
                double a12,
                float a13,
                double a14,
                float a15,
                double a16){
  printf("fpManyArgs:\n");
  printf("   a1 = %f\n", a1);
  printf("   a2 = %lf\n", a2);
  printf("   a3 = %f\n", a3);
  printf("   a4 = %lf\n", a4);
  printf("   a5 = %f\n", a5);
  printf("   a6 = %lf\n", a6);
  printf("   a7 = %f\n", a7);
  printf("   a8 = %lf\n", a8);
  printf("   a9 = %f\n", a9);
  printf("  a10 = %lf\n", a10);
  printf("  a11 = %f\n", a11);
  printf("  a12 = %lf\n", a12);
  printf("  a13 = %f\n", a13);
  printf("  a14 = %lf\n", a14);
  printf("  a15 = %f\n", a15);
  printf("  a16 = %lf\n", a16);
  printf("\n");

#ifdef __linux__
  errno = 200;
#endif
}

void mixManyArgs(char a1,
                 float a2,
                 short a3,
                 double a4,
                 int a5,
                 float a6,
                 long a7,
                 double a8,
                 char a9,
                 float a10,
                 short a11,
                 double a12,
                 char a13,
                 float a14,
                 short a15,
                 double a16,
                 int a17,
                 float a18){
  printf("mixManyArgs:\n");
  printf("   a1 = %hhd\n", a1);
  printf("   a2 = %f\n", a2);
  printf("   a3 = %hd\n", a3);
  printf("   a4 = %lf\n", a4);
  printf("   a5 = %d\n", a5);
  printf("   a6 = %f\n", a6);
  printf("   a7 = %ld\n", a7);
  printf("   a8 = %lf\n", a8);
  printf("   a9 = %hhd\n", a9);
  printf("  a10 = %f\n", a10);
  printf("  a11 = %hd\n", a11);
  printf("  a12 = %lf\n", a12);
  printf("  a13 = %hhd\n", a13);
  printf("  a14 = %f\n", a14);
  printf("  a15 = %hd\n", a15);
  printf("  a16 = %lf\n", a16);
  printf("  a17 = %d\n", a17);
  printf("  a18 = %f\n", a18);
  printf("\n");

#ifdef __linux__
  errno = 300;
#endif
}
