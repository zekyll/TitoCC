This directory contains tests source files for manually testing the compiler. The tests are meant to be compiled with TitoCC and then run on Titokone.

All tests outputs "1" on success for each test and something else if the test fails.

--- test_basic_features.c ---
Tests basic language features.

--- test_pointers_and_arrays.c ---
Tests features related to pointers, arrays and lvalues.

--- test_loops.c ---
Tests for, while and do-while loops, and break/continue statements.

--- test_function_pointers.c ---
Tests function declarators and function pointers.

--- test_integer_types.c ---
Tests operations on different integer types.

--- test_character_and_string_literals.c ---
Tets character and string literals and escape sequences. (More thoroughly checked in tokenizer JUnit tests.)

--- test_declarations_and_storage_classes.c ---
Tests declarations, definitions and storage classes.

--- example_random_number_generator.c ---
Random number generator using WELL512 algorithm. First 5 generated numbers should be:
1174520813
2105604108
-526364649
-1098122639
112202824

--- example_fibonacci.c ---
Outputs fibonacci numbers.

--- example_recursive_factorial.c ---
Calculates factorial of given numbers.

--- example_quicksort.c ---
Sorts an array of values using quicksort.

--- example_sieve_of_erasthothenes.c ---
Reads an integer n and generates all primes between 2 and n.
