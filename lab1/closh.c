// closh.c - CS 377, Fall 2013
// Andrew W Penniman

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <sys/wait.h>

typedef int bool;
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

void sequential(char* cmdTokens[], int count, int timeout){
	int childProcessID;
	int i;
	for(i = 0; i < count; i++){
		childProcessID = fork();

		if(childProcessID == 0){ // if this is the child process
 			execvp(cmdTokens[0], cmdTokens);
 			
 			// code should only be reached if execvp fails
			printf("Can't execute %s\n", cmdTokens[0]);
    		exit(1);
		}
	
		int status;
		
		if(timeout == 0){ // if there is no timeout, block on the child process to terminate
			do
			 waitpid(childProcessID, &status, 0);
			while(!WIFEXITED(status) && !WIFSIGNALED(status));
		}
		else{
			int time_remaining = timeout; // we know timeout does not equal 0
			bool waiting = TRUE;
			
			while(waiting && time_remaining != 0) { 
				sleep(1);
				time_remaining = time_remaining - 1;
				
				if(waitpid(childProcessID, &status, WNOHANG) == 0) // if waitpid returns without reaping any child processes
					waiting = TRUE;
				else
					waiting = !WIFEXITED(status); // we are "waiting", if the child process has not terminated
				
				if(waiting && time_remaining == 0){
					kill(childProcessID, SIGKILL);
					printf("%s timed out\n", cmdTokens[0]);
					// will exit loop because (time_remaining != 0) == false
				}
			}
		} 
	}
}

void concurrent(char* cmdTokens[], int count, int timeouts){
	
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
