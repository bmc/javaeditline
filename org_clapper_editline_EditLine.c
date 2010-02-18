#include <jni.h>
#include <stdio.h>
#include <histedit.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define min(a, b)  ((a) < (b) ? (a) : (b))

#define PROMPT_MAX 128

typedef struct jEditLineData
{
    char prompt[PROMPT_MAX];
    JNIEnv *env;
    jclass javaClass;
    jobject javaEditLine;
    jmethodID handleCompletionMethodID;
}
jEditLineData;

/* Can only have one per process. */

static EditLine *editLineDesc = NULL;
static History *historyDesc = NULL;

static jEditLineData *get_data()
{
    void *d;
    el_get(editLineDesc, EL_CLIENTDATA, &d);
    return (jEditLineData *) d;
}

static void set_prompt(const char *new_prompt)
{
    jEditLineData *data = get_data();
    strncpy(data->prompt, new_prompt, PROMPT_MAX - 1);
}

static const char *get_prompt()
{
    jEditLineData *data = get_data();
    return data->prompt;
}

static unsigned char complete(EditLine *el, int ch)
{
    jEditLineData *data = get_data();
    JNIEnv *env = data->env;

    const LineInfo *lineInfo = el_line(el);

    jstring jLine;
    jint jCursor;
    jstring jToken;
    
    if (lineInfo != NULL)
    {
        jLine = (*env)->NewStringUTF(env, lineInfo->buffer);

        /* Find the beginning of the current token */

        const char *ptr;
	for (ptr = lineInfo->cursor - 1;
             (!isspace((unsigned char)*ptr)) && (ptr > lineInfo->buffer); ptr--)
		continue;
	int len = lineInfo->cursor - ptr;

        /* Save it as a Java string. */

        if (len == 0)
            jToken = (*env)->NewStringUTF(env, "");

        else
        {
            char *token = (char *) malloc(len + 1);
            strncpy(token, ptr, len);
            jToken = (*env)->NewStringUTF(env, "");
            free(token);
        }

        jCursor = (long) (lineInfo->cursor - lineInfo->buffer);
    }

    else
    {
        jToken = (*env)->NewStringUTF(env, "");
        jLine = (*env)->NewStringUTF(env, "");
        jCursor = 0;
    }

    jmethodID method = data->handleCompletionMethodID;

    unsigned char result = CC_ERROR;
    jstring completion = (*env)->CallObjectMethod(env,
                                                  data->javaEditLine,
                                                  method,
                                                  jToken,
                                                  jLine,
                                                  jCursor);

    if (completion != NULL)
    {
        const char *str = (*env)->GetStringUTFChars(env, completion, NULL);
        if (str == NULL)
        {
            puts("Out of memory (Java) during completion.");
        }

        else
        {
            result = CC_REFRESH;
            el_insertstr(el, str);
            (*env)->ReleaseStringUTFChars(env, completion, str);
        }
    }

    return result;
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static long n_el_init(String program, EditLine javaEditLine)
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1el_1init
    (JNIEnv *env, jclass cls, jstring program, jobject javaEditLine)
{
    const char *cProgram = (*env)->GetStringUTFChars(env, program, NULL);
    if (cProgram == NULL)
    {
        /* OutOfMemoryError already thrown */
        return;
    }

    jEditLineData *data = (jEditLineData*) malloc(sizeof(jEditLineData));

    if (data == NULL)
    {
        jclass exc = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
        (*env)->ThrowNew(env, exc, "unable to allocation jEditLineData");
    }

    else
    {
        editLineDesc = el_init(cProgram, stdin, stdout, stderr);
        historyDesc = history_init();

        data->env = env;
        data->javaClass = (*env)->NewGlobalRef(env, cls);
        data->javaEditLine = (*env)->NewGlobalRef(env, javaEditLine);
        data->handleCompletionMethodID = (*env)->GetMethodID(
            env, cls,
            "handleCompletion",
            "(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;");

        if (data->handleCompletionMethodID != NULL)
            el_set(editLineDesc, EL_ADDFN, "ed-complete", "Complete", complete);

        el_set(editLineDesc, EL_CLIENTDATA, (void *) data);
        el_set(editLineDesc, EL_PROMPT, get_prompt);
        el_set(editLineDesc, EL_HIST, history, historyDesc);
        el_set(editLineDesc, EL_SIGNAL, 1);

    }

    (*env)->ReleaseStringUTFChars(env, program, cProgram);
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_el_source(String path);
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1el_1source
  (JNIEnv *env, jclass cls, jstring javaPath)
{
    if (javaPath == NULL)
        el_source(editLineDesc, NULL);

    else
    {
        const char *path = (*env)->GetStringUTFChars(env, javaPath, NULL);
        if (path == NULL)
        {
            /* OutOfMemoryError already thrown */
        }

        else
        {
            el_source(editLineDesc, path);
            (*env)->ReleaseStringUTFChars(env, javaPath, path);
        }
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
    int result = (jint) history(historyDesc, &ev, H_GETSIZE);
    printf("%d ret=%d\n", ev.num, result);
    return result;
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
        ev.str = str;
        printf("Adding \"%s\" to history.\n", str);
        history(historyDesc, &ev, H_ENTER, str);
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
