(ns linkpage.auth.current-user.backend
  (:require
   [linkpage.rpc.backend :as rpc]
   [clojure.core.async :refer [go timeout <!]]))

(def called! (atom 0))

(defmethod rpc/rpc! :current-user/get [_req]
  (go
    (swap! called! inc)
    (<! (timeout 1500))
    (if (even? @called!)
      [:result/ok {:user/user-id 1
                   :user/username "test-user"
                   :user/email "my-email"}]
      [:result/err {:error/message "Failed for some reason"}])))