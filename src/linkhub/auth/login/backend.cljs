(ns linkhub.auth.login.backend 
  (:require
    [linkhub.rpc.backend :as rpc]
    [clojure.core.async :refer [go timeout <!]]))


(defmethod rpc/rpc! :login/send-code [req]
  (go
    (<! (timeout 3000))
    [:result/ok req]))