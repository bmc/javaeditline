#include <jni.h>
#include <stdio.h>
#include <histedit.h>
#include <stdlib.h>
#include <string.h>

#define min(a, b)  ((a) < (b) ? (a) : (b))

#define PROMPT_MAX 128

typedef struct jEditLineData
{
    char prompt[PROMPT_MAX];
}
jEditLineData;

/* Can only have one per process. */

static EditLine *editLineDesc = NULL;
static History *historyDesc = NULL;

static jEditLineData *getData()
{
    void *d;
    el_get(editLineDesc, EL_CLIENTDATA, &d);
    return (jEditLineData *) d;
}

static void set_prompt(const char *new_prompt)
{
    jEditLineData *data = getData();
    strncpy(data->prompt, new_prompt, PROMPT_MAX - 1);
}

static const char *get_prompt()
{
    jEditLineData *data = getData();
    return data->prompt;
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static long n_el_init(String program);
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1el_1init
  (JNIEnv *env, jclass cls, jstring program)
{
    char cProgramName[128];
    int totalChars = (*env)->GetStringLength(env, program);
    int maxBuf = sizeof(cProgramName) - 1;
    int total = min(totalChars, maxBuf);
    (*env)->GetStringUTFRegion(env, program, 0, total, cProgramName);
    jEditLineData *data = (jEditLineData*) malloc(sizeof(jEditLineData));

    if (data == NULL)
    {
        jclass exc = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
        if (exc == NULL)
        {
            /* Unable to find the exception class, give up. */
        }

        else
        {
            (*env)->ThrowNew(env, exc, "unable to allocation jEditLineData");
        }
    }

    else
    {
        editLineDesc = el_init(cProgramName, stdin, stdout, stderr);
        historyDesc = history_init();
        el_set(editLineDesc, EL_CLIENTDATA, (void *) data);
        set_prompt("? ");
    }
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_el_end(long handle);
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1el_1end
(JNIEnv *env, jclass cls)
{
    el_end(editLineDesc);
    history_end(historyDesc);
    editLineDesc = NULL;
    historyDesc = NULL;
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_el_set_prompt(long handle, String prompt);
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1el_1set_1prompt
(JNIEnv *env, jclass cls, jstring prompt)
{
    const char *str = (*env)->GetStringUTFChars(env, prompt, NULL);
    if (str == NULL)
    {
        /* OutOfMemoryError already thrown */
    }

    else
    {
        set_prompt(str);
        (*env)->ReleaseStringUTFChars(env, prompt, str);
    }
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_el_get_lineinfo(long handle, LineInfo info)
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1el_1get_1lineinfo
  (JNIEnv *env, jclass cls, jobject info)
{
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static String n_el_gets(long handle);
 */
JNIEXPORT jstring JNICALL Java_org_clapper_editline_EditLine_n_1el_1gets
  (JNIEnv *env, jclass cls)
{
    jstring result = NULL;
    int count = 0;
    const char *line = el_gets(editLineDesc, &count);

    if (line != NULL)
        result = (*env)->NewStringUTF(env, line);

    return result;
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static int n_history_get_size(long handle)
 */
JNIEXPORT jint JNICALL Java_org_clapper_editline_EditLine_n_1history_1get_1size
  (JNIEnv *env, jclass cls)
{
    HistEvent ev;
    return (jint) history(historyDesc, &ev, H_GETSIZE);
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_history_set_size(long handle, int size)
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1history_1set_1size
  (JNIEnv *env, jclass cls, jint newSize)
{
    HistEvent ev;
    history(historyDesc, &ev, H_SETSIZE, (int) newSize);
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_history_clear(long handle)
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1history_1clear
  (JNIEnv *env, jclass cls)
{
    HistEvent ev;
    history(historyDesc, &ev, H_CLEAR);
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_history_append(long handle, String line)
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1history_1append
  (JNIEnv *env, jclass cls, jstring line)
{
    const char *str = (*env)->GetStringUTFChars(env, line, NULL);
    if (str == NULL)
    {
        /* OutOfMemoryError already thrown */
    }

    else
    {
        HistEvent ev;
        history(historyDesc, &ev, H_APPEND, str);

        (*env)->ReleaseStringUTFChars(env, line, str);
    }
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static String[] n_history_get_all(long handle)
 */
JNIEXPORT
jobjectArray JNICALL Java_org_clapper_editline_EditLine_n_1history_1get_1all
  (JNIEnv *env, jclass cls)
{
    return NULL;
}
