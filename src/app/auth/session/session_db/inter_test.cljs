(ns app.auth.session.session-db.inter-test
  (:require
   [cljs.test :refer-macros [deftest is testing async]]
   [clojure.core.async :refer [<!]]
   [app.auth.session.session-db.inter :as session-db]
   [app.auth.session.session-db.impl-kv]
   [lib.kv.impl-atom])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))

(defn new-db []
  (session-db/new! {:session-db/impl :session-db/impl-kv
                    :kv/impl :kv/impl-atom}))

(deftest session-db-operations
  (testing "Basic session-db operations"
    (async
     done
     (go
       (let [db (new-db)
             test-session-id "test-session-id"
             test-session {:session/session-id test-session-id
                           :session/user-id "test-user-id"
                           :session/created-at (js/Date.)}]

         ;; Test putting a session
         (let [put-result (<! (session-db/put! db test-session))]
           (is (= :result/ok (:result/type put-result))
               "Putting a session should return a success result"))

         ;; Test finding by session-id
         (let [find-result (<! (session-db/find-by-session-id! db test-session-id))]
           (is (= :result/ok (:result/type find-result))
               "Finding a session by ID should return a success result")
           (is (= test-session-id (:session/session-id find-result))
               "Retrieved session should have the correct session ID")
           (is (= (:session/user-id test-session) (:session/user-id find-result))
               "Retrieved session should have the correct user ID"))

         ;; Test zapping a session
         (let [zap-result (<! (session-db/zap! db test-session-id))]
           (is (= :result/ok (:result/type zap-result))
               "Zapping a session should return a success result"))

         ;; Verify session is gone after zap
         (let [find-after-zap (<! (session-db/find-by-session-id! db test-session-id))]
           (is (= :result/ok (:result/type find-after-zap))
               "Finding after zap should still return a success result")
           (is (nil? (:session/user-id find-after-zap))
               "Session should be nil after being zapped"))

         (done))))))

(deftest session-db-multiple-sessions
  (testing "Managing multiple sessions"
    (async
     done
     (go
       (let [db (new-db)
             session-1 {:session/session-id "session-1"
                        :session/user-id "user-1"
                        :session/created-at (js/Date.)}
             session-2 {:session/session-id "session-2"
                        :session/user-id "user-2"
                        :session/created-at (js/Date.)}]

         (<! (session-db/put! db session-1))
         (<! (session-db/put! db session-2))

         ;; Verify both sessions can be retrieved by ID
         (let [find-1 (<! (session-db/find-by-session-id! db (:session/session-id session-1)))
               find-2 (<! (session-db/find-by-session-id! db (:session/session-id session-2)))]
           (is (= (:session/user-id session-1) (:session/user-id find-1))
               "First session should be retrievable")
           (is (= (:session/user-id session-2) (:session/user-id find-2))
               "Second session should be retrievable"))

         (done))))))


(deftest find-by-session-id-test
  (testing "Finding sessions by session ID"
    (async
     done
     (go
       (let [db (new-db)
             test-user-id (str (random-uuid))
             test-session-id (str (random-uuid))
             test-session {:session/session-id test-session-id
                           :session/user-id test-user-id
                           :session/created-at (js/Date.)}]

         ;; Test finding a non-existent session
         (let [find-result (<! (session-db/find-by-session-id! db "non-existent-id"))]
           (is (= :result/ok (:result/type find-result))
               "Finding a non-existent session should return a success result with nil data")
           (is (nil? (:session/user-id find-result))
               "Non-existent session should have nil user ID"))

         ;; Add a session and then find it
         (<! (session-db/put! db test-session))
         (let [find-result (<! (session-db/find-by-session-id! db test-session-id))]
           (is (= :result/ok (:result/type find-result))
               "Finding an existing session should return a success result")
           (is (= test-session-id (:session/session-id find-result))
               "Retrieved session should have the correct session ID")
           (is (= test-user-id (:session/user-id find-result))
               "Retrieved session should have the correct user ID"))

         (done))))))
