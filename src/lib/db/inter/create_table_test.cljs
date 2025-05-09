(ns lib.db.inter.create-table-test
  (:require
   [clojure.core.async :refer [<!] :refer-macros [go]]
   [cljs.test :refer-macros [deftest testing is async]]
   [lib.db.impl]
   [lib.db.inter :as db]
   [lib.db.inter.fixture :refer [q-create-table]]))

(deftest create-table-test
  (async
   done
   (testing "Can create a table"
     (go
       (let [db (db/init! {:db/impl :db/impl-better-sqlite3})]
         (<! (db/query-chan! db q-create-table))
         (is true "Table creation should not throw error")
         (done))))))

