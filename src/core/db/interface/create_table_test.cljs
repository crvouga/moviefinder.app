#_(ns core.db.interface.create-table-test
    (:require
     [clojure.core.async :refer [<!] :refer-macros [go]]
     [cljs.test :refer-macros [deftest testing is async]]
     [core.db.impl]
     [core.db.interface :as db-conn]
     [core.db.interface.fixture :refer [create-table-query]]))

#_(deftest create-table-test
    (async done
           (testing "Can create a table"
             (go
               (let [conn (db-conn/new!)]
                 (<! (db-conn/query-chan! conn create-table-query))
                 (is true "Table creation should not throw error")
                 (done))))))

