#_(ns lib.db.inter.sql-injection-test
    (:require
     [clojure.core.async :refer [<!] :refer-macros [go]]
     [cljs.test :refer-macros [deftest testing is async]]
     [lib.db.impl]
     [lib.db.inter :as db]
     [lib.db.inter.fixture :refer [create-table-query insert-query select-query]]))

#_(deftest sql-injection-test
    (async done
           (testing "Prevents SQL injection attacks"
             (go
               (let [db (db/new! {:db/impl :db/impl-better-sqlite3})
                     malicious-input "test'; DROP TABLE test_table; --"
                     _ (<! (db/query-chan! db create-table-query))
                     _ (<! (db/query-chan! db insert-query))
                     result (<! (db/query-chan! db select-query))]

                 (is (= 1 (count (:db/rows result))) "Should find exactly one row")
                 (is (= malicious-input (-> result :db/rows first :name)) "Malicious input should be stored as-is")
                 (let [table-check (<! (db/query-chan! db select-query))]
                   (is (seq (:db/rows table-check)) "Table should still exist")))
               (done)))))


#_(deftest sql-injection-test-2
    (async done
           (testing "Prevents SQL injection in WHERE clause"
             (go
               (let [db (db/new! {:db/impl :db/impl-better-sqlite3})
                     malicious-where "1=1; DROP TABLE test_table; --"
                     _ (<! (db/query-chan! db create-table-query))
                     _ (<! (db/query-chan! db insert-query))
                     result (<! (db/query-chan! db [:select [:*] :from :test_table :where malicious-where]))]


                 (is (empty? (:db/rows result)) "Query with malicious WHERE should return no rows")


                 (let [table-check (<! (db/query-chan! db select-query))]
                   (is (= 1 (count (:db/rows table-check))) "Table should exist with one row")
                   (is (= "safe data" (-> table-check :db/rows first :name)) "Original data should be intact")))
               (done)))))
