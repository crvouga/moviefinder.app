(ns lib.sql-test
  (:require
   [cljs.test :refer-macros [deftest testing is]]
   [lib.sql :as sql]))

(deftest escape-param-val-test
  (testing "Escaping string parameters"
    (let [escape-param-val #'lib.sql/escape-param-val]
      (is (= "'hello'" (escape-param-val "hello")) "Basic string escaping")
      (is (= "'it''s a test'" (escape-param-val "it's a test")) "String with single quote")
      (is (= "'multiple''quotes''here'" (escape-param-val "multiple'quotes'here")) "String with multiple quotes")))

  (testing "Escaping non-string parameters"
    (let [escape-param-val #'lib.sql/escape-param-val]
      (is (= "42" (escape-param-val 42)) "Number parameter")
      (is (= "true" (escape-param-val true)) "Boolean parameter")
      (is (= "null" (escape-param-val nil)) "Nil parameter"))))

(deftest replace-param-test
  (testing "Replacing parameters in SQL strings"
    (let [replace-param #'lib.sql/replace-param]
      (is (= "SELECT * FROM users WHERE id = 1"
             (replace-param "SELECT * FROM users WHERE id = ?" 1))
          "Replace with number")

      (is (= "SELECT * FROM users WHERE name = 'John'"
             (replace-param "SELECT * FROM users WHERE name = ?" "John"))
          "Replace with string")

      (is (= "SELECT * FROM users WHERE active = true"
             (replace-param "SELECT * FROM users WHERE active = ?" true))
          "Replace with boolean"))))

(deftest params->sql-test
  (testing "Replacing multiple parameters in SQL strings"
    (let [params->sql #'lib.sql/params->sql]
      (is (= "SELECT * FROM users"
             (params->sql "SELECT * FROM users"))
          "No parameters")

      (is (= "SELECT * FROM users WHERE id = 1"
             (params->sql "SELECT * FROM users WHERE id = ?" 1))
          "Single parameter")

      (is (= "SELECT * FROM users WHERE id = 1 AND name = 'John'"
             (params->sql "SELECT * FROM users WHERE id = ? AND name = ?" 1 "John"))
          "Multiple parameters")

      (is (= "INSERT INTO users (name, age) VALUES ('O''Connor', 30)"
             (params->sql "INSERT INTO users (name, age) VALUES (?, ?)" "O'Connor" 30))
          "Parameters with quotes"))))

(deftest sql-query->raw-sql-test
  (testing "Converting string queries"
    (is (= "SELECT * FROM users"
           (sql/sql-query->raw-sql "SELECT * FROM users"))
        "String query passes through unchanged"))

  (testing "Converting HoneySQL queries"
    (is (= "SELECT * FROM users WHERE id = 1"
           (sql/sql-query->raw-sql {:select [:*]
                                    :from [:users]
                                    :where [:= :id 1]}))
        "Basic HoneySQL query")

    (is (= "SELECT name, age FROM users WHERE (age > 18) AND (name = 'John')"
           (sql/sql-query->raw-sql {:select [:name :age]
                                    :from [:users]
                                    :where [:and
                                            [:> :age 18]
                                            [:= :name "John"]]}))
        "Complex HoneySQL query")))