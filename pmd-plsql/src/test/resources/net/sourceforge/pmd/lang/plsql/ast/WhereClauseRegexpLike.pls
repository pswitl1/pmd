--
-- Where Clause With Regexp Like
-- https://docs.oracle.com/en/database/oracle/oracle-database/18/sqlrf/Pattern-matching-Conditions.html#GUID-D2124F3A-C6E4-4CCA-A40E-2FFCABFD8E19
--

BEGIN

SELECT first_name, last_name
INTO test
FROM employees
WHERE REGEXP_LIKE (first_name, '^Ste(v|ph)en$')
ORDER BY first_name, last_name;

SELECT last_name
INTO test
FROM employees
WHERE REGEXP_LIKE (last_name, '([aeiou])\1', 'i')
ORDER BY last_name;
END;
/
