(ns linkpage.auth.current-user.backend
  (:require
   [linkpage.rpc.backend :as rpc]
   [clojure.core.async :refer [go timeout <!]]))

(defmethod rpc/rpc! :current-user/get [_req]
  (go
    (<! (timeout 1500))
    [:result/ok {:user/user-id 1
                 :user/username "test-user"
                 :user/email "my-email"}]))