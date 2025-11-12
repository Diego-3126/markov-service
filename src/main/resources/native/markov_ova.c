#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
#include "markov_ova.h"

// Implementación de strdup para portabilidad
static char* my_strdup(const char* s) {
    if (s == NULL) return NULL;
    size_t len = strlen(s) + 1;
    char* copy = malloc(len);
    if (copy != NULL) {
        memcpy(copy, s, len);
    }
    return copy;
}

// Funciones internas (no exportadas)
static void to_lowercase(char* str) {
    for (int i = 0; str[i]; i++) {
        str[i] = tolower(str[i]);
    }
}

static char** tokenize_text(const char* text, int* word_count) {
    char* text_copy = my_strdup(text);
    if (text_copy == NULL) {
        *word_count = 0;
        return NULL;
    }
    
    char** tokens = malloc(MAX_WORDS * sizeof(char*));
    if (tokens == NULL) {
        free(text_copy);
        *word_count = 0;
        return NULL;
    }
    
    *word_count = 0;
    
    char* token = strtok(text_copy, " \t\n\r.,;:!?\"'()[]{}");
    while (token != NULL && *word_count < MAX_WORDS) {
        to_lowercase(token);
        tokens[*word_count] = my_strdup(token);
        if (tokens[*word_count] == NULL) {
            break;
        }
        (*word_count)++;
        token = strtok(NULL, " \t\n\r.,;:!?\"'()[]{}");
    }
    
    free(text_copy);
    return tokens;
}

static int find_state_index(MarkovModel* model, char** words, int count) {
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

static void add_to_vocabulary(MarkovModel* model, const char* word) {
    for (int i = 0; i < model->vocab_size; i++) {
        if (strcmp(model->vocabulary[i], word) == 0) return;
    }
    model->vocabulary[model->vocab_size] = my_strdup(word);
    if (model->vocabulary[model->vocab_size] != NULL) {
        model->vocab_size++;
    }
}

// API pública implementada
MARKOV_API MarkovModel* markov_create_model(int order) {
    MarkovModel* model = malloc(sizeof(MarkovModel));
    if (model == NULL) return NULL;
    
    model->order = order;
    model->state_count = 0;
    model->vocab_size = 0;
    model->states = malloc(MAX_STATES * sizeof(MarkovState));
    model->vocabulary = malloc(MAX_WORDS * sizeof(char*));
    
    if (model->states == NULL || model->vocabulary == NULL) {
        free(model->states);
        free(model->vocabulary);
        free(model);
        return NULL;
    }
    
    return model;
}

MARKOV_API void markov_train_model(MarkovModel* model, const char* text) {
    if (model == NULL || text == NULL) return;
    
    int word_count;
    char** tokens = tokenize_text(text, &word_count);
    if (tokens == NULL) return;
    
    if (word_count <= model->order) {
        printf("Texto demasiado corto para el orden seleccionado.\n");
        for (int i = 0; i < word_count; i++) free(tokens[i]);
        free(tokens);
        return;
    }
    
    for (int i = 0; i < word_count - model->order; i++) {
        char** current_words = malloc(model->order * sizeof(char*));
        if (current_words == NULL) continue;
        
        for (int j = 0; j < model->order; j++) {
            current_words[j] = tokens[i + j];
            add_to_vocabulary(model, tokens[i + j]);
        }
        
        char* next_word = tokens[i + model->order];
        add_to_vocabulary(model, next_word);
        
        int state_index = find_state_index(model, current_words, model->order);
        
        if (state_index == -1) {
            if (model->state_count >= MAX_STATES) {
                free(current_words);
                continue;
            }
            
            state_index = model->state_count;
            model->states[state_index].words = malloc(model->order * sizeof(char*));
            if (model->states[state_index].words == NULL) {
                free(current_words);
                continue;
            }
            
            for (int j = 0; j < model->order; j++) {
                model->states[state_index].words[j] = my_strdup(current_words[j]);
                if (model->states[state_index].words[j] == NULL) {
                    // Liberar en caso de error
                    for (int k = 0; k < j; k++) free(model->states[state_index].words[k]);
                    free(model->states[state_index].words);
                    free(current_words);
                    continue;
                }
            }
            model->states[state_index].word_count = model->order;
            model->states[state_index].next_words = malloc(MAX_NEXT_WORDS * sizeof(char*));
            model->states[state_index].frequencies = malloc(MAX_NEXT_WORDS * sizeof(int));
            
            if (model->states[state_index].next_words == NULL || 
                model->states[state_index].frequencies == NULL) {
                free(model->states[state_index].words);
                free(model->states[state_index].next_words);
                free(model->states[state_index].frequencies);
                free(current_words);
                continue;
            }
            
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
            if (state->next_words[state->next_count] != NULL) {
                state->frequencies[state->next_count] = 1;
                state->next_count++;
            }
        }
        
        free(current_words);
    }
    
    for (int i = 0; i < word_count; i++) {
        free(tokens[i]);
    }
    free(tokens);
}

MARKOV_API char* markov_generate_text(MarkovModel* model, int length, const char* start) {
    if (model == NULL || model->state_count == 0) {
        return my_strdup("Modelo no entrenado. Primero entrene con algun texto.");
    }
    
    char* result = malloc(length * MAX_WORD_LENGTH * sizeof(char));
    if (result == NULL) return NULL;
    result[0] = '\0';
    
    int word_count = 0;
    char** start_words = NULL;
    
    if (start != NULL) {
        start_words = tokenize_text(start, &word_count);
    }
    
    // Usar estado inicial o aleatorio
    char** current_state = malloc(model->order * sizeof(char*));
    if (current_state == NULL) {
        if (start_words) {
            for (int i = 0; i < word_count; i++) free(start_words[i]);
            free(start_words);
        }
        free(result);
        return NULL;
    }
    
    if (start_words != NULL && word_count >= model->order) {
        for (int i = 0; i < model->order; i++) {
            current_state[i] = my_strdup(start_words[word_count - model->order + i]);
            if (current_state[i] == NULL) {
                // Liberar en caso de error
                for (int j = 0; j < i; j++) free(current_state[j]);
                free(current_state);
                for (int j = 0; j < word_count; j++) free(start_words[j]);
                free(start_words);
                free(result);
                return NULL;
            }
        }
    } else {
        // Estado aleatorio
        int random_index = rand() % model->state_count;
        for (int i = 0; i < model->order; i++) {
            current_state[i] = my_strdup(model->states[random_index].words[i]);
            if (current_state[i] == NULL) {
                for (int j = 0; j < i; j++) free(current_state[j]);
                free(current_state);
                free(result);
                return NULL;
            }
        }
    }
    
    // Construir el texto
    for (int i = 0; i < length; i++) {
        int state_index = find_state_index(model, current_state, model->order);
        
        if (state_index == -1) break;
        
        MarkovState* state = &model->states[state_index];
        if (state->next_count == 0) break;
        
        // Seleccionar palabra basada en frecuencia
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
            
            // Actualizar estado
            for (int j = 0; j < model->order - 1; j++) {
                free(current_state[j]);
                current_state[j] = my_strdup(current_state[j + 1]);
                if (current_state[j] == NULL) break;
            }
            if (current_state[model->order - 1]) free(current_state[model->order - 1]);
            current_state[model->order - 1] = my_strdup(next_word);
        } else {
            break;
        }
    }
    
    // Liberar memoria
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

MARKOV_API void markov_free_model(MarkovModel* model) {
    if (model == NULL) return;
    
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
    free(model);
}

// Funciones de utilidad
MARKOV_API int markov_get_state_count(MarkovModel* model) {
    return (model != NULL) ? model->state_count : 0;
}

MARKOV_API int markov_get_vocab_size(MarkovModel* model) {
    return (model != NULL) ? model->vocab_size : 0;
}

MARKOV_API int markov_get_model_order(MarkovModel* model) {
    return (model != NULL) ? model->order : 0;
}

MARKOV_API void markov_print_statistics(MarkovModel* model) {
    if (model == NULL || model->state_count == 0) {
        printf("Modelo no entrenado.\n");
        return;
    }
    
    printf("=== ESTADISTICAS DEL MODELO MARKOV ===\n");
    printf("Orden de la cadena: %d\n", model->order);
    printf("Estados unicos: %d\n", model->state_count);
    printf("Tamano del vocabulario: %d palabras\n", model->vocab_size);
    
    printf("\nEjemplos de transiciones:\n");
    int show_count = (model->state_count < 3) ? model->state_count : 3;
    
    for (int i = 0; i < show_count; i++) {
        printf("Estado %d: \"", i + 1);
        for (int j = 0; j < model->states[i].word_count; j++) {
            printf("%s ", model->states[i].words[j]);
        }
        printf("\" → ");
        
        for (int j = 0; j < model->states[i].next_count && j < 2; j++) {
            printf("%s(%d) ", model->states[i].next_words[j], 
                   model->states[i].frequencies[j]);
        }
        if (model->states[i].next_count > 2) printf("...");
        printf("\n");
    }
}

// Funciones educativas del OVA
MARKOV_API void markov_explain_algorithm() {
    printf("\n=== EXPLICACION: CADENAS DE MARKOV ===\n");
    printf("Las cadenas de Markov son modelos probabilisticos que predicen\n");
    printf("el siguiente estado basandose unicamente en el estado actual.\n\n");
    printf("En generacion de texto:\n");
    printf("- Cada 'estado' es una secuencia de palabras (n-grama)\n");
    printf("- Las 'transiciones' son probabilidades de palabras siguientes\n");
    printf("- El modelo aprende patrones del texto de entrenamiento\n");
    printf("- La generacion sigue estas probabilidades aprendidas\n\n");
    printf("Ejemplo: Para 'el gato' → [duerme(0.6), come(0.3), corre(0.1)]\n");
}

MARKOV_API void markov_demo_example() {
    printf("\n=== DEMOSTRACION DEL ALGORITMO ===\n");
    
    MarkovModel* model = markov_create_model(2);
    if (model == NULL) {
        printf("Error creando modelo.\n");
        return;
    }
    
    const char* demo_text = 
        "El sol brilla en el cielo azul. "
        "Los pajaros cantan en los arboles. "
        "El viento sopla suavemente entre las hojas. "
        "Los ninos juegan felices en el parque. "
        "La vida es bella cuando hay paz.";
    
    printf("Entrenando modelo con texto de ejemplo...\n");
    markov_train_model(model, demo_text);
    
    printf("Generando texto automatico...\n");
    char* generated = markov_generate_text(model, 15, "el sol");
    if (generated != NULL) {
        printf("Texto generado: %s\n", generated);
        free(generated);
    }
    
    markov_free_model(model);
    printf("Demostracion completada.\n");
}

MARKOV_API char* markov_get_transition_examples(MarkovModel* model, int examples_count) {
    if (model == NULL || model->state_count == 0) {
        return my_strdup("Modelo no entrenado.");
    }
    
    char* result = malloc(5000 * sizeof(char));
    if (result == NULL) return NULL;
    result[0] = '\0';
    
    strcat(result, "EJEMPLOS DE TRANSICIONES APRENDIDAS:\n\n");
    
    int count = (examples_count < model->state_count) ? examples_count : model->state_count;
    
    for (int i = 0; i < count; i++) {
        strcat(result, "Estado: \"");
        for (int j = 0; j < model->states[i].word_count; j++) {
            strcat(result, model->states[i].words[j]);
            strcat(result, " ");
        }
        strcat(result, "\" → Puede seguir: ");
        
        for (int j = 0; j < model->states[i].next_count && j < 3; j++) {
            char temp[100];
            sprintf(temp, "%s(%d) ", model->states[i].next_words[j], 
                    model->states[i].frequencies[j]);
            strcat(result, temp);
        }
        strcat(result, "\n");
    }
    
    return result;
}
