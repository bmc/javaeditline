#include <jni.h>
#include <stdio.h>
#include <histedit.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <signal.h>
#include <assert.h>

#define min(a, b)  ((a) < (b) ? (a) : (b))

#define PROMPT_MAX 128

typedef struct _SimpleStringList
{
    const char *string;
    struct _SimpleStringList *next;
}
SimpleStringList;

typedef struct _jEditLineData
{
    char prompt[PROMPT_MAX];
    JNIEnv *env;
    jclass javaClass;
    jobject javaEditLine;
    jmethodID handleCompletionMethodID;
}
jEditLineData;

static sig_t prev_sigint = SIG_DFL;
static sig_t prev_sigquit = SIG_DFL;
static sig_t prev_sighup = SIG_DFL;
static sig_t prev_sigterm = SIG_DFL;

/* Can only have one per process. */

static EditLine *editLineDesc = NULL;
static History *historyDesc = NULL;

volatile sig_atomic_t gotsig = 0;
static void signal_handler(int i)
{
    gotsig = 1;
}

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
    int tokenLength = 0;
    
    if (lineInfo != NULL)
    {
        jLine = (*env)->NewStringUTF(env, lineInfo->buffer);

        /* Find the beginning of the current token */

        const char *ptr;
	for (ptr = lineInfo->cursor - 1;
             (!isspace((unsigned char)*ptr)) && (ptr > lineInfo->buffer); ptr--)
		continue;
        if (ptr != lineInfo->buffer)
        {
            /* Stopped on white space. Increment to get to start of token. */
            ptr++;
        }

	tokenLength = lineInfo->cursor - ptr + 1;

        /* Save it as a Java string. */

        if (tokenLength == 0)
            jToken = (*env)->NewStringUTF(env, "");

        else
        {
            char *token = (char *) malloc(tokenLength + 1);
            strncpy(token, ptr, tokenLength);
            jToken = (*env)->NewStringUTF(env, token);
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
            el_deletestr(el, tokenLength - 1);
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
 * Method: static void n_el_end();
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
 * Method: static void n_el_set_prompt(, String prompt);
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
 * Method: static String n_el_gets();
 */
JNIEXPORT jstring JNICALL Java_org_clapper_editline_EditLine_n_1el_1gets
  (JNIEnv *env, jclass cls)
{
    jstring result = NULL;
    int count = 0;
    const char *line;

    for (;;)
    {
        line = el_gets(editLineDesc, &count);

        if (gotsig)
        {
            gotsig = 0;
            el_reset(editLineDesc);
            putchar('\n');
        }

        else
        {
            break;
        }
    }

    if (line != NULL)
        result = (*env)->NewStringUTF(env, line);

    return result;
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static int n_history_get_size()
 */
JNIEXPORT jint JNICALL Java_org_clapper_editline_EditLine_n_1history_1get_1size
  (JNIEnv *env, jclass cls)
{
    HistEvent ev;
    /**
       H_GETSIZE doesn't seem to work on the Mac.

    int result = (jint) history(historyDesc, &ev, H_GETSIZE);
    */
    int total = 0;
    int rc;
    for (rc = history(historyDesc, &ev, H_LAST);
         rc != -1;
         rc = history(historyDesc, &ev, H_PREV))
        total++;

    return total;
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_history_set_size(, int size)
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1history_1set_1size
  (JNIEnv *env, jclass cls, jint newSize)
{
    HistEvent ev;
    history(historyDesc, &ev, H_SETSIZE, (int) newSize);
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_history_clear()
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1history_1clear
  (JNIEnv *env, jclass cls)
{
    HistEvent ev;
    history(historyDesc, &ev, H_CLEAR);
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static void n_history_append(, String line)
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
        history(historyDesc, &ev, H_ENTER, str);
        (*env)->ReleaseStringUTFChars(env, line, str);
    }
}

/*
 * Class:  org_clapper_editline_EditLine
 * Method: static String[] n_history_get_all()
 */
JNIEXPORT
jobjectArray JNICALL Java_org_clapper_editline_EditLine_n_1history_1get_1all
  (JNIEnv *env, jclass cls)
{
    SimpleStringList *list = NULL;

    int total = 0;
    int rc;
    HistEvent ev;

    /*
      Capture the history elements in a linked-list, so we only have to
      traverse the list once. For simplicity, insert the entries at the top
      (like a stack).
    */
    for (rc = history(historyDesc, &ev, H_LAST);
         rc != -1;
         rc = history(historyDesc, &ev, H_PREV))
    {
        SimpleStringList *entry =
            (SimpleStringList *) malloc(sizeof(SimpleStringList));

        if (list == NULL)
        {
            list = entry;
            entry->next = NULL;
        }
        else
        {
            entry->next = list;
            list = entry;
        }

        entry->string = strdup(ev.str);
        total++;
    }

    /* Allocate an appropriate size array. */

    jobjectArray result = (jobjectArray)
        (*env)->NewObjectArray(env, total,
                               (*env)->FindClass(env, "java/lang/String"),
                               (*env)->NewStringUTF(env, ""));

    /*
      Traverse the list and fill the array backwards, since the list
      is in reverse order.
    */
    int i = total;
    SimpleStringList *entry = list;
    SimpleStringList *prev = NULL;
    while (entry != NULL)
    {
        i--;
        assert (i >= 0);
        assert(entry->string != NULL);

        /* Create a Java version of the string. */
        jstring js = (*env)->NewStringUTF(env, entry->string);

        /* Store it in the Java array. */
        (*env)->SetObjectArrayElement(env, result, i, js);

        /* Move along. */
        prev = entry;
        entry = entry->next;

        /* Free the linked list entry and its string. */
        free((void *) prev->string);
        free(prev);
    }

    return result;
}

/*
 * Class:     org_clapper_editline_EditLine
 * Method:    n_el_trap_signals
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1el_1trap_1signals
    (JNIEnv *env, jclass cls, jboolean on)
{
    gotsig = 0;

    if (on)
    {
        prev_sigint = signal(SIGINT, signal_handler);
        prev_sighup = signal(SIGHUP, signal_handler);
        prev_sigquit = signal(SIGQUIT, signal_handler);
        prev_sigterm = signal(SIGTERM, signal_handler);
    }

    else
    {
        (void) signal(SIGINT, prev_sigint);
        (void) signal(SIGHUP, prev_sighup);
        (void) signal(SIGQUIT, prev_sigquit);
        (void) signal(SIGTERM, prev_sigterm);
    }
}

/*
 * Class:     org_clapper_editline_EditLine
 * Method:    static void n_history_set_unique(boolean on)
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_org_clapper_editline_EditLine_n_1history_1set_1unique
  (JNIEnv *env, jclass cls, jboolean on)
{
    HistEvent ev;
    history(historyDesc, &ev, H_SETUNIQUE, on ? 1 : 0);
}
