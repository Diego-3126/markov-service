#ifndef MARKOV_OVA_H
#define MARKOV_OVA_H

#define MAX_WORD_LENGTH 100
#define MAX_WORDS 10000
#define MAX_STATES 5000
#define MAX_NEXT_WORDS 100
#define MAX_TEXT_LENGTH 100000

#ifdef _WIN32
    #ifdef MARKOV_OVA_EXPORTS
        #define MARKOV_API __declspec(dllexport)
    #else
        #define MARKOV_API __declspec(dllimport)
    #endif
#else
    #define MARKOV_API __attribute__((visibility("default")))
#endif

// Estructuras de datos
typedef struct {
    char** words;
    int word_count;
    char** next_words;
    int next_count;
    int* frequencies;
} MarkovState;

typedef struct {
    MarkovState* states;
    int state_count;
    int order;
    char** vocabulary;
    int vocab_size;
} MarkovModel;

// API principal de la librería
MARKOV_API MarkovModel* markov_create_model(int order);
MARKOV_API void markov_train_model(MarkovModel* model, const char* text);
MARKOV_API char* markov_generate_text(MarkovModel* model, int length, const char* start);
MARKOV_API void markov_free_model(MarkovModel* model);

// Funciones de utilidad
MARKOV_API int markov_get_state_count(MarkovModel* model);
MARKOV_API int markov_get_vocab_size(MarkovModel* model);
MARKOV_API int markov_get_model_order(MarkovModel* model);
MARKOV_API void markov_print_statistics(MarkovModel* model);

// Funciones educativas (parte OVA)
MARKOV_API void markov_explain_algorithm();
MARKOV_API void markov_demo_example();
MARKOV_API char* markov_get_transition_examples(MarkovModel* model, int examples_count);

#endif
