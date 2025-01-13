(ns linkpage.auth.login.backend
  (:require
   [linkpage.rpc.backend :as rpc]
   [clojure.core.async :refer [go timeout <!]]))

(defmethod rpc/rpc! :login/send-code [req]
  (let [payload (second req)]
    (go
      (<! (timeout 1500))
      [:result/ok payload])))