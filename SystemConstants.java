//Nick Ribeiro
//Homework 1
//02-07-22

public interface SystemConstants {

    //Settings
    public int WORDSIZE = 10000;
    public int GPRSIZE = 8;
    public long VALID_PROGRAM_AREA = 2999;

    //Fetch mode constants
    public long FETCH_REGISTER = 1;
    public long FETCH_REGISTER_DEFERRED = 2;
    public long FETCH_AUTO_INCREMENT = 3;
    public long FETCH_AUTO_DECREMENT = 4;
    public long FETCH_DIRECT = 5;
    public long FETCH_IMMEDIATE = 6;

    //Execution time constants
    public long TIME_HALT = 12;
    public long TIME_ADD = 3;
    public long TIME_SUBTRACT = 3;
    public long TIME_MULTIPLY = 6;
    public long TIME_DIVIDE = 6;
    public long TIME_MOVE = 2;
    public long TIME_BRANCH = 2;
    public long TIME_BRANCH_PLUS = 4;
    public long TIME_BRANCH_MINUS = 4;
    public long TIME_BRANCH_ZERO = 4;
    public long TIME_PUSH = 2;
    public long TIME_POP = 2;
    public long TIME_SYSTEM_CALL = 12; 

    //Error constants
    public long OK = 0;
    public long ERROR = -1;
    public long ERROR_INVALID_ADDRESS = -2;
    public long ERROR_INVALID_OPERANDS = -3;
    public long ERROR_INVALID_FETCH_MODE = -4;
    public long ERROR_IMMEDIATE_DESTINATION = -5;
    public long ERROR_FATAL_RUNTIME = -6;
    public long ERROR_INVALID_OPCODE = -7;

}