/*
    mod_Mumble - Positional Audio Communication for Minecraft with Mumble
    Copyright (C) 2011 zsawyer (http://sourceforge.net/users/zsawyer)

    This file is part of mod_MumbleLink
        (http://sourceforge.net/projects/modmumblelink/).

    mod_MumbleLink is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mod_MumbleLink is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with mod_MumbleLink.  If not, see <http://www.gnu.org/licenses/>.

 */

/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class mod_MumbleLink */

#ifndef _Included_mod_MumbleLink
#define _Included_mod_MumbleLink
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     mod_MumbleLink
 * Method:    updateLinkedMumble
 * Signature: ([F[F[FLjava/lang/String;Ljava/lang/String;[F[F[FLjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_lunakittyyy_stapimumble_MumbleMod_updateLinkedMumble
  (JNIEnv *, jobject, jfloatArray, jfloatArray, jfloatArray, jstring, jstring, jfloatArray, jfloatArray, jfloatArray, jstring, jstring);

/*
 * Class:     mod_MumbleLink
 * Method:    initMumble
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_lunakittyyy_stapimumble_MumbleMod_initMumble
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
