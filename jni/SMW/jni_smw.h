#ifndef _JNI_SMW_H_
#define _JNI_SMW_H_


/* JNI */
#include <jni.h>

void jni_start_music( bool playOnce, const char * name );
void jni_stop_music();
void jni_set_volume( int level );
void jni_pause_music( bool pause );

#endif
