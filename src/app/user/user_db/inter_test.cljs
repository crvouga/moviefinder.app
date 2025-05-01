(ns app.user.user-db.inter-test
  (:require
   [cljs.test :refer-macros [deftest is testing async]]
   [clojure.core.async :refer [<!]]
   [app.user.user-db.inter :as user-db]
   [app.user.user-db.impl-kv]
   [lib.kv.impl]
   [app.user.entity :as user])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))


(defn new-db []
  (user-db/new! {:user-db/impl :user-db/impl-kv
                 :kv/impl :kv/impl-atom}))



(deftest user-db-operations
  (testing "Basic user-db operations"
    (async
     done
     (go
       (let [db (new-db)
             test-user-id "test-user-id"
             test-user {:user/user-id test-user-id
                        :user/phone-number "+1234567890"
                        :user/name "Test User"}]

         ;; Test putting a user
         (let [put-result (<! (user-db/put! db test-user))]
           (is (= :result/ok (:result/type put-result))
               "Putting a user should return a success result"))

         ;; Test fin`ding by user-id
         (let [find-result (<! (user-db/find-by-user-id! db test-user-id))]
           (is (= :result/ok (:result/type find-result))
               "Finding a user by ID should return a success result")
           (is (= test-user (dissoc find-result :result/type))
               "Retrieved user should match what was put"))

         ;; Test finding by phone number
         (let [find-by-phone-result (<! (user-db/find-by-phone-number! db (:user/phone-number test-user)))]
           (is (= :result/ok (:result/type find-by-phone-result))
               "Finding a user by phone number should return a success result")
           (is (user/eq? test-user find-by-phone-result)
               "Retrieved user should match what was put"))

         ;; Test zapping a user
         (let [zap-result (<! (user-db/zap! db test-user-id))]
           (is (= :result/ok (:result/type zap-result))
               "Zapping a user should return a success result"))

         ;; Verify user is gone after zap
         (let [find-after-zap (<! (user-db/find-by-user-id! db test-user-id))]
           (is (= :result/ok (:result/type find-after-zap))
               "Finding after zap should still return a success result")
           (is (nil? (:user/name find-after-zap))
               "User should be nil after being zapped"))

         (done))))))

(deftest user-db-multiple-users
  (testing "Managing multiple users"
    (async
     done
     (go
       (let [db (new-db)
             user-1 {:user/user-id "user-1"
                     :user/phone-number "+1111111111"
                     :user/name "User One"}
             user-2 {:user/user-id "user-2"
                     :user/phone-number "+2222222222"
                     :user/name "User Two"}]


         (<! (user-db/put! db user-1))
         (<! (user-db/put! db user-2))

         ;; Verify both users can be retrieved by ID
         (let [find-1 (<! (user-db/find-by-user-id! db (:user/user-id user-1)))
               find-2 (<! (user-db/find-by-user-id! db (:user/user-id user-2)))]
           (is (user/eq? user-1 find-1)
               "First user should be retrievable")
           (is (user/eq? user-2 find-2)
               "Second user should be retrievable"))

         ;; Verify both users can be retrieved by phone number
         (let [find-by-phone-1 (<! (user-db/find-by-phone-number! db (:user/phone-number user-1)))
               find-by-phone-2 (<! (user-db/find-by-phone-number! db (:user/phone-number user-2)))]
           (is (user/eq? user-1 find-by-phone-1)
               "First user should be retrievable by phone")
           (is (user/eq? user-2 find-by-phone-2)
               "Second user should be retrievable by phone"))

         (done))))))
