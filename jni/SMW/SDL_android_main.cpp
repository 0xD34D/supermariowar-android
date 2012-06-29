
/* Include the SDL main definition header */
#include "SDL_main.h"
#include "gfx.h"
#include "jni_smw.h"

/*******************************************************************************
                 Functions called by JNI
*******************************************************************************/
#include <jni.h>
#include <android/log.h>

static JNIEnv* mEnv = NULL;
jclass jNativeCls;
jmethodID jStartMusicMethod;
jmethodID jStopMusicMethod;
jmethodID jPauseMusicMethod;
jmethodID jSetVolumeMethod;

// Called before SDL_main() to initialize JNI bindings in SDL library
extern "C" void SDL_Android_Init(JNIEnv* env, jclass cls);

// Library init
extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved);

// Start up the SDL app
extern "C" void Java_com_scheffsblend_smw_SDLActivity_nativeInit(JNIEnv* env, jclass cls, jobject obj)
{
    /* This interface could expand with ABI negotiation, calbacks, etc. */
    SDL_Android_Init(env, cls);
    mEnv = env;
	jNativeCls = env->FindClass("com/scheffsblend/smw/SDLActivity");

	// Load OnStopMusic()
	jStopMusicMethod = env->GetStaticMethodID( jNativeCls,
			"OnStopMusic", "()V");

	// Load OnStartMusic(String name)
	jStartMusicMethod = env->GetStaticMethodID(jNativeCls,
			"OnStartMusic", "(ZLjava/lang/String;)V");

	// Load OnPauseMusic(boolean pause)
	jPauseMusicMethod = env->GetStaticMethodID( jNativeCls,
			"OnPauseMusic", "(Z)V" );

	// Load OnSetVolume(int level)
	jSetVolumeMethod = env->GetStaticMethodID( jNativeCls,
			"OnSetVolume", "(I)V" );

    if( !jStopMusicMethod || !jStartMusicMethod ||
    		!jPauseMusicMethod || !jSetVolumeMethod ) {
        __android_log_print(ANDROID_LOG_WARN, "SDL", "SDL: Couldn't locate Java callbacks, check that they're named and typed correctly");
    }
    /* Run the application code! */
    int status;
    char *argv[2];
    argv[0] = strdup("SDL_app");
    argv[1] = NULL;
    status = SDL_main(1, argv);

    /* We exit here for consistency with other platforms. */
    exit(status);
}

// Init the SDL app graphics
extern  "C" void Java_com_scheffsblend_smw_SDLActivity_gfxInit(JNIEnv* env, jclass cls,
		jint width, jint height)
{
	gfx_init(width, height, true);
}

/* vi: set ts=4 sw=4 expandtab: */

/**
 * Fires when a background song is requested
 */
void jni_start_music (bool playOnce, const char * name)
{
    mEnv->CallStaticVoidMethod( jNativeCls, jStartMusicMethod,
    		playOnce, mEnv->NewStringUTF(name) );
}

/**
 * Fires when a background song is stopped
 */
void jni_stop_music ( void )
{
    mEnv->CallStaticVoidMethod( jNativeCls, jStopMusicMethod );
}


void jni_pause_music( bool pause )
{
	mEnv->CallStaticVoidMethod( jNativeCls, jPauseMusicMethod, pause );
}


void jni_set_volume( int level )
{
	mEnv->CallStaticVoidMethod( jNativeCls, jSetVolumeMethod, level );
}
