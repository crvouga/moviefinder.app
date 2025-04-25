(ns core.backend.db-conn.interface.create-table-test
  (:require
   [cljs.core.async :refer [<!] :refer-macros [go]]
   [cljs.test :refer-macros [deftest testing is async]]
   [core.backend.db-conn.impl]
   [core.backend.db-conn.interface :as db-conn]
   [core.backend.db-conn.interface.fixture :refer [create-table-query]]))

(deftest create-table-test
  (async done
         (testing "Can create a table"
           (go
             (let [conn (db-conn/new!)]
               (<! (db-conn/query-chan! conn create-table-query))
               (is true "Table creation should not throw error")
               (done))))))

