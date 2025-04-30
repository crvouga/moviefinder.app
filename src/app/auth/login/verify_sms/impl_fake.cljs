(ns app.auth.login.verify-sms.impl-fake
  (:require [clojure.core.async :as a]
            [app.auth.login.verify-sms.inter :as verify-sms]))

(def fake-code "123")

(defmethod verify-sms/send-code! :verify-sms-impl/fake [{:keys [user/phone-number] :as i}]
  (a/go
    (a/<! (a/timeout 500))
    (println "Sending code " fake-code " to " phone-number)
    (assoc i :result/type :result/ok)))


(defmethod verify-sms/verify-code! :verify-sms-impl/fake [{:keys [user/phone-number verify-sms/code] :as i}]
  (a/go
    (a/<! (a/timeout 500))
    (println "Verifying code " code " for " phone-number)
    (if (= code fake-code)
      (assoc i :result/type :result/ok)
      (assoc i
             :result/type :result/err
             :err/err :verify-sms-err/wrong-code))))             

