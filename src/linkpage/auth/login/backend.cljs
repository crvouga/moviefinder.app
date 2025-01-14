(ns linkpage.auth.login.backend
  (:require
   [linkpage.rpc.backend :as rpc]
   [linkpage.auth.login.verify-sms.impl]
   [linkpage.auth.login.verify-sms.interface :as verify-sms]
   [clojure.core.async :refer [go <!]]))

(defmethod rpc/rpc! :login/send-code [ctx req]
  (go
    (let [payload (second req)
          input (merge ctx payload)
          res (<! (verify-sms/send-code! input))]
      res)))