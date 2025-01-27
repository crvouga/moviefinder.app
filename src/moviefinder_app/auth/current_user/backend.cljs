(ns moviefinder-app.auth.current-user.backend
  (:require
   [moviefinder-app.rpc.backend :as rpc]
   [clojure.core.async :refer [go timeout <!]]))

#_(def fake-user {:user/id "123"
                  :user/phone-number "1234567890"
                  :user/name "John Doe"})

(defmethod rpc/rpc! :current-user/get [_req]
  (go
    (<! (timeout 1500))
    (merge #_fake-user {:result/type :result/ok})))
