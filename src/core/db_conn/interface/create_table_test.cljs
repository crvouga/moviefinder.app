(ns core.db-conn.interface.create-table-test
  (:require [core.db-conn.interface :as db-conn]
            [core.db-conn.impl]
            [cljs.test :refer-macros [deftest testing is async]]
            [cljs.core.async :refer [<!] :refer-macros [go]]))

(deftest create-table-test
  (async done
         (testing "Can create a table"
           (go
             (let [db-conn (db-conn/new! {:db-conn/impl :db-conn-impl/pglite})]

          ; Create table
               (<! (-> db-conn
                       (assoc :db-conn/query "CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT)")
                       db-conn/query-chan!))

               (is true "Table creation should not throw error")
               (done))))))

