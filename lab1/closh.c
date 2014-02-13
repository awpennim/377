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

void execCmdSequentially(char* cmdTokens[], int count, int timeout){
	int childProcessID; // id of the child process we will fork and wait for
	int i;
	for(i = 0; i < count; i++){
		childProcessID = fork();

		if(childProcessID == 0){ // if this is the child process
 			execvp(cmdTokens[0], cmdTokens);
 			
 			// code should only be reached if execvp fails
			printf("Can't execute %s\n", cmdTokens[0]);
    		exit(1);
		}
	
		
		
		if(timeout == 0){ // if there is no timeout, block on the child process to terminate
			int status;
			
			do
			 waitpid(childProcessID, &status, 0);
			while(!WIFEXITED(status) && !WIFSIGNALED(status)); // until WIFEXITED(status) or WIFSIGNALED(status) become true
		}
		else{ // timeout should be in the range 1-9, so we will give the child process "timeout" many seconds to terminate
			if(waitOnProcessForSeconds(childProcessID, timeout)){ // if the child process terminated on its own
				// good!
			}
			else{// if the child process exceeded timeout
				kill(childProcessID, SIGKILL);
				printf("%s timed out\n", cmdTokens[0]);
			}
		} 
	}
}

// used by execCmdSequentially
// returns true if process terminted. returns false if process exceeded timeRemaining.
int waitOnProcessForSeconds(int childProcessID, int timeRemaining){
	bool processBeenReaped = FALSE;
			
	while(!processBeenReaped && timeRemaining != 0) { // do this until we've reaped the child process, or until the process has timed out
		sleep(1); // give the process a second to run
		timeRemaining = timeRemaining - 1; // remove a second from our timer
		
		int status;
		if(waitpid(childProcessID, &status, WNOHANG) == 0) // if waitpid returns without reaping any child processes
			processBeenReaped = FALSE;
		else
			processBeenReaped = WIFEXITED(status);

	}
	
	return processBeenReaped; // return true if we have reaped the child process. return false if the child process exceeded its time.	
}

void execCmdConcurrently(char* cmdTokens[], int count, int timeouts){
	
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
		execCmdConcurrently(cmdTokens, count, timeout);
	else
		execCmdSequentially(cmdTokens, count, timeout);
  }
}
