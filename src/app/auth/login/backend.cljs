(ns app.auth.login.backend
  (:require
   [app.rpc.backend :as rpc]
   [app.auth.login.verify-sms.impl]
   [app.auth.login.verify-sms.interface :as verify-sms]
   [clojure.core.async :refer [go <!]]))

(def fake-user {:user/id "123"
                :user/phone-number "1234567890"
                :user/name "John Doe"})

(defmethod rpc/rpc! :rpc/send-code [req]
  (go
    (let [res (<! (verify-sms/send-code! (second req)))]
      res)))

(defmethod rpc/rpc! :rpc/verify-code [req]
  (go
    (let [res (<! (verify-sms/verify-code! (second req)))
          ok? (-> res :result/type (= :result/ok))
          res (if ok? (merge res fake-user) res)]
      res)))