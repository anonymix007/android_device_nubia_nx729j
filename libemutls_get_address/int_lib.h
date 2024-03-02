/* ===-- int_lib.h - configuration header for compiler-rt  -----------------===
 *
 *                     The LLVM Compiler Infrastructure
 *
 * This file is dual licensed under the MIT and the University of Illinois Open
 * Source Licenses. See LICENSE.TXT for details.
 *
 * ===----------------------------------------------------------------------===
 *
 * This file is a configuration header for compiler-rt.
 * This file is not part of the interface of this library.
 *
 * ===----------------------------------------------------------------------===
 */

#ifndef INT_LIB_H
#define INT_LIB_H

#define ALWAYS_INLINE __attribute__((always_inline))
#define NOINLINE __attribute__((noinline))
#define NORETURN __attribute__((noreturn))
#define UNUSED __attribute__((unused))

#endif /* INT_LIB_H */
