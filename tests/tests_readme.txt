This directory contains tests source files for manually testing the compiler. The tests are meant to be compiled with TitoCC and then run on Titokone.

--- test_basic_features.c ---
Attempts test basic language features as thorougly as possible. Outputs "1" on success for each test and something else if the test fails.

--- test_pointers_and_arrays.c ---
Tests features related to pointers, arrays and lvalues. Outputs "1" on successful tests.

--- test_loops.c ---
Tets for, while and do-while loops, and break/continue statements. Outputs "1" on successful tests.

--- test_function_pointers.c ---
Tets function declarators and function pointers. Outputs "1" on successful tests.

--- test_integer_types.c ---
Tests operations on different integer types.

--- example_random_number_generator.c ---
Random number generator using WELL512 algorithm. First 5 generated numbers should be:
1174520813
1516304396
950030359
-1098122639
112202824

--- example_fibonacci.c ---
Outputs fibonacci numbers.

--- example_recursive_factorial.c ---
Calculates factorial of given numbers.
