// closh.c - CS 377, Fall 2013
// Andrew W Penniman

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>

#define TRUE 1
#define FALSE 0

// tokenize the command string into arguments - do not modify
void readCmdTokens(char* cmd, char** cmdTokens) {
  cmd[strlen(cmd) - 1] = '\0'; // drop trailing newline
  int i = 0;
  cmdTokens[i] = strtok(cmd, " "); // tokenize on spaces
  while (cmdTokens[i++] && i < sizeof(cmdTokens)) {
    cmdTokens[i] = strtok(NULL, " "); //subsequent calls to strtok should replace cmd with NULL. That is just how strtok is implemented.
  }
}

// read one character of input, then discard up to the newline - do not modify
char readChar() {
  char c = getchar();
  while (getchar() != '\n');
  return c;
}

// main method - program entry point
int main() {
  char cmd[81]; // array of chars (a string)
  char* cmdTokens[20]; // array of strings
  int count; // number of times to execute command
  int parallel; // whether to run in parallel or sequentially
  int timeout; // max seconds to run set of commands (parallel) or each command (sequentially)

  while (TRUE) { // main shell input loop

    // begin parsing code - do not modify
    printf("closh> ");
    fgets(cmd, sizeof(cmd), stdin);
    if (cmd[0] == '\n') continue;
    readCmdTokens(cmd, cmdTokens);
    do {
      printf("  count> ");
      count = readChar() - '0';
    } while (count <= 0 || count > 9);
    printf("  [p]arallel or [s]equential> ");
    parallel = (readChar() == 'p') ? TRUE : FALSE;
    do {
      printf("  timeout> ");
      timeout = readChar() - '0';
    } while (timeout < 0 || timeout > 9);
    // end parsing code

	if(parallel)
		concurrent(cmdTokens, count, timeout);
	else
		sequential(cmdTokens, count, timeout);
  }
}

void sequential(char* cmdTokens[], int count, int timeout){
	int childProcessIDs[9]; // char value ranges from 0-9

	int i;
	for(i = 0; i < count; i++){
		childProcessIDs[i] = fork();

		if(childProcessIDs[i] == 0){ // if this is the child process
 			execvp(cmdTokens[0], cmdTokens);
 			
 			// code should only be reached if execvp fails
			printf("Can't execute %s\n", cmdTokens[0]);
    		exit(1);
		}
		else{
			// if sequential, block	
		}
	  
		count--;
	}
}

void concurrent(char* cmdTokens[], int count, int timeout){
	
}
