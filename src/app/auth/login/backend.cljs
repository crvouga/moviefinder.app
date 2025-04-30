(ns app.auth.login.backend
  (:require
   [app.rpc.backend :as rpc]
   [app.auth.login.verify-sms.impl]
   [app.auth.login.verify-sms.inter :as verify-sms]
   [clojure.core.async :as a]
   [lib.result :as result]))

(def fake-user {:user/id "123"
                :user/phone-number "1234567890"
                :user/name "John Doe"})

(rpc/reg
 :rpc/send-code
 (fn [req]
   (a/go
     (let [res (a/<! (verify-sms/send-code! req))]
       res))))

(rpc/reg
 :rpc/verify-code
 (fn [req]
   (a/go
     (let [res (a/<! (verify-sms/verify-code! req))]
       (if (result/ok? res)
         (merge res fake-user)
         res)))))