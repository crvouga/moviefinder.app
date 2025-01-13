(ns linkpage.auth.login.backend
  (:require
   [linkpage.rpc.backend :as rpc]
   [clojure.core.async :refer [go timeout <!]]))

(def called! (atom 0))

(defmethod rpc/rpc! :login/send-code [req]
  (swap! called! inc)
  (let [payload (second req)]
    (go
      (<! (timeout 1500))
      (if (even? @called!)
        [:result/ok payload]
        [:result/err {:error/message "Failed for some reason"}]))))