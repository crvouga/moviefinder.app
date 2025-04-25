(ns core.backend.db-conn.interface.sql-injection-test
  (:require
   [cljs.core.async :refer [<!] :refer-macros [go]]
   [cljs.test :refer-macros [deftest testing is async]]
   [core.backend.db-conn.impl]
   [core.backend.db-conn.interface :as db-conn]
   [core.backend.db-conn.interface.fixture :refer [create-table-query insert-query select-query]]))

(deftest sql-injection-test
  (async done
         (testing "Prevents SQL injection attacks"
           (go
             (let [db-conn (db-conn/new! {:db-conn/impl :db-conn-impl/better-sqlite3})
                   malicious-input "test'; DROP TABLE test_table; --"
                   _ (<! (db-conn/query-chan! db-conn create-table-query))
                   _ (<! (db-conn/query-chan! db-conn insert-query))
                   result (<! (db-conn/query-chan! db-conn select-query))]

               (is (= 1 (count (:db-conn/rows result))) "Should find exactly one row")
               (is (= malicious-input (-> result :db-conn/rows first :name)) "Malicious input should be stored as-is")
               (let [table-check (<! (db-conn/query-chan! db-conn select-query))]
                 (is (seq (:db-conn/rows table-check)) "Table should still exist")))
             (done)))))


(deftest sql-injection-test-2
  (async done
         (testing "Prevents SQL injection in WHERE clause"
           (go
             (let [db-conn (db-conn/new! {:db-conn/impl :db-conn-impl/better-sqlite3})
                   malicious-where "1=1; DROP TABLE test_table; --"
                   _ (<! (db-conn/query-chan! db-conn create-table-query))
                   _ (<! (db-conn/query-chan! db-conn insert-query))
                   result (<! (db-conn/query-chan! db-conn [:select [:*] :from :test_table :where malicious-where]))]


               (is (empty? (:db-conn/rows result)) "Query with malicious WHERE should return no rows")


               (let [table-check (<! (db-conn/query-chan! db-conn select-query))]
                 (is (= 1 (count (:db-conn/rows table-check))) "Table should exist with one row")
                 (is (= "safe data" (-> table-check :db-conn/rows first :name)) "Original data should be intact")))
             (done)))))
