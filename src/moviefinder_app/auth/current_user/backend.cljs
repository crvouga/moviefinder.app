(ns moviefinder-app.auth.current-user.backend
  (:require
   [moviefinder-app.rpc.backend :as rpc]
   [clojure.core.async :refer [go timeout <!]]))

(defmethod rpc/rpc! :current-user/get [_ctx _req]
  (go
    (<! (timeout 1500))
    [:result/ok]
    #_[:result/ok {:user/user-id 1
                   :user/username "test-user"
                   :user/email "my-email"}]))