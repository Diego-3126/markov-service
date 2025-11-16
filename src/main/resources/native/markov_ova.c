#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <time.h>

#define MAX_WORD_LENGTH 100
#define MAX_WORDS 10000
#define MAX_STATES 5000
#define MAX_NEXT_WORDS 100

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
    char* last_training_text;
} MarkovModel;

char* my_strdup(const char* s) {
    if (s == NULL) return NULL;
    size_t len = strlen(s) + 1;
    char* copy = malloc(len);
    if (copy != NULL) {
        memcpy(copy, s, len);
    }
    return copy;
}

void to_lowercase(char* str) {
    for (int i = 0; str[i]; i++) {
        str[i] = tolower(str[i]);
    }
}

char** tokenize_text(const char* text, int* word_count) {
    char* text_copy = my_strdup(text);
    char** tokens = malloc(MAX_WORDS * sizeof(char*));
    *word_count = 0;
    
    char* token = strtok(text_copy, " \t\n\r.,;:!?\"'()[]{}");
    while (token != NULL && *word_count < MAX_WORDS) {
        to_lowercase(token);
        tokens[*word_count] = my_strdup(token);
        (*word_count)++;
        token = strtok(NULL, " \t\n\r.,;:!?\"'()[]{}");
    }
    
    free(text_copy);
    return tokens;
}

int find_state_index(MarkovModel* model, char** words, int count) {
    for (int i = 0; i < model->state_count; i++) {
        int match = 1;
        for (int j = 0; j < count; j++) {
            if (strcmp(model->states[i].words[j], words[j]) != 0) {
                match = 0;
                break;
            }
        }
        if (match) return i;
    }
    return -1;
}

void add_to_vocabulary(MarkovModel* model, const char* word) {
    for (int i = 0; i < model->vocab_size; i++) {
        if (strcmp(model->vocabulary[i], word) == 0) return;
    }
    model->vocabulary[model->vocab_size] = my_strdup(word);
    if (model->vocabulary[model->vocab_size] != NULL) {
        model->vocab_size++;
    }
}

// ✅ CAMBIO PARA LINUX: Usar __attribute__((visibility("default"))) en lugar de __declspec(dllexport)
__attribute__((visibility("default"))) void* markov_create_model(int order) {
    MarkovModel* model = malloc(sizeof(MarkovModel));
    model->order = order;
    model->state_count = 0;
    model->vocab_size = 0;
    model->last_training_text = NULL;
    model->states = malloc(MAX_STATES * sizeof(MarkovState));
    model->vocabulary = malloc(MAX_WORDS * sizeof(char*));
    return model;
}

__attribute__((visibility("default"))) void markov_train_model(void* model_ptr, const char* text) {
    MarkovModel* model = (MarkovModel*)model_ptr;
    
    if (model->last_training_text != NULL) {
        free(model->last_training_text);
    }
    model->last_training_text = my_strdup(text);
    
    int word_count;
    char** tokens = tokenize_text(text, &word_count);
    
    if (word_count <= model->order) {
        for (int i = 0; i < word_count; i++) free(tokens[i]);
        free(tokens);
        return;
    }
    
    for (int i = 0; i < word_count - model->order; i++) {
        char** current_words = malloc(model->order * sizeof(char*));
        for (int j = 0; j < model->order; j++) {
            current_words[j] = tokens[i + j];
            add_to_vocabulary(model, tokens[i + j]);
        }
        
        char* next_word = tokens[i + model->order];
        add_to_vocabulary(model, next_word);
        
        int state_index = find_state_index(model, current_words, model->order);
        
        if (state_index == -1) {
            state_index = model->state_count;
            model->states[state_index].words = malloc(model->order * sizeof(char*));
            for (int j = 0; j < model->order; j++) {
                model->states[state_index].words[j] = my_strdup(current_words[j]);
            }
            model->states[state_index].word_count = model->order;
            model->states[state_index].next_words = malloc(MAX_NEXT_WORDS * sizeof(char*));
            model->states[state_index].frequencies = malloc(MAX_NEXT_WORDS * sizeof(int));
            model->states[state_index].next_count = 0;
            model->state_count++;
        }
        
        MarkovState* state = &model->states[state_index];
        int found = 0;
        for (int j = 0; j < state->next_count; j++) {
            if (strcmp(state->next_words[j], next_word) == 0) {
                state->frequencies[j]++;
                found = 1;
                break;
            }
        }
        
        if (!found && state->next_count < MAX_NEXT_WORDS) {
            state->next_words[state->next_count] = my_strdup(next_word);
            state->frequencies[state->next_count] = 1;
            state->next_count++;
        }
        
        free(current_words);
    }
    
    for (int i = 0; i < word_count; i++) {
        free(tokens[i]);
    }
    free(tokens);
}

__attribute__((visibility("default"))) const char* markov_generate_text(void* model_ptr, int length, const char* start) {
    static char result[10000];
    result[0] = '\0';
    
    MarkovModel* model = (MarkovModel*)model_ptr;
    
    if (model->state_count == 0) {
        return "Modelo no entrenado. Primero entrene con algun texto.";
    }
    
    srand(time(NULL));
    
    int word_count;
    char** start_words = NULL;
    
    if (start != NULL) {
        start_words = tokenize_text(start, &word_count);
    }
    
    char** current_state = malloc(model->order * sizeof(char*));
    
    if (start_words != NULL && word_count >= model->order) {
        for (int i = 0; i < model->order; i++) {
            current_state[i] = my_strdup(start_words[word_count - model->order + i]);
        }
    } else {
        int random_index = rand() % model->state_count;
        for (int i = 0; i < model->order; i++) {
            current_state[i] = my_strdup(model->states[random_index].words[i]);
        }
    }
    
    for (int i = 0; i < length; i++) {
        int state_index = find_state_index(model, current_state, model->order);
        
        if (state_index == -1) break;
        
        MarkovState* state = &model->states[state_index];
        if (state->next_count == 0) break;
        
        int total_freq = 0;
        for (int j = 0; j < state->next_count; j++) {
            total_freq += state->frequencies[j];
        }
        
        int random_val = rand() % total_freq;
        int cumulative_freq = 0;
        char* next_word = NULL;
        
        for (int j = 0; j < state->next_count; j++) {
            cumulative_freq += state->frequencies[j];
            if (random_val < cumulative_freq) {
                next_word = state->next_words[j];
                break;
            }
        }
        
        if (next_word) {
            strcat(result, next_word);
            strcat(result, " ");
            
            for (int j = 0; j < model->order - 1; j++) {
                free(current_state[j]);
                current_state[j] = my_strdup(current_state[j + 1]);
            }
            free(current_state[model->order - 1]);
            current_state[model->order - 1] = my_strdup(next_word);
        } else {
            break;
        }
    }
    
    for (int i = 0; i < model->order; i++) {
        if (current_state[i]) free(current_state[i]);
    }
    free(current_state);
    
    if (start_words) {
        for (int i = 0; i < word_count; i++) {
            free(start_words[i]);
        }
        free(start_words);
    }
    
    return result;
}

__attribute__((visibility("default"))) int markov_get_vocabulary_size(void* model_ptr) {
    MarkovModel* model = (MarkovModel*)model_ptr;
    return model->vocab_size;
}

__attribute__((visibility("default"))) int markov_get_state_count(void* model_ptr) {
    MarkovModel* model = (MarkovModel*)model_ptr;
    return model->state_count;
}

__attribute__((visibility("default"))) void markov_free_model(void* model_ptr) {
    MarkovModel* model = (MarkovModel*)model_ptr;
    if (model != NULL) {
        for (int i = 0; i < model->state_count; i++) {
            for (int j = 0; j < model->states[i].word_count; j++) {
                free(model->states[i].words[j]);
            }
            free(model->states[i].words);
            
            for (int j = 0; j < model->states[i].next_count; j++) {
                free(model->states[i].next_words[j]);
            }
            free(model->states[i].next_words);
            free(model->states[i].frequencies);
        }
        free(model->states);
        
        for (int i = 0; i < model->vocab_size; i++) {
            free(model->vocabulary[i]);
        }
        free(model->vocabulary);
        
        if (model->last_training_text != NULL) {
            free(model->last_training_text);
        }
        
        free(model);
    }
}
