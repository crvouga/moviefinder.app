(ns moviefinder-app.auth.current-user.backend
  (:require
   [moviefinder-app.rpc.backend :as rpc]
   [clojure.core.async :refer [go timeout <!]]))

#_(def fake-user {:user/id "123"
                  :user/phone-number "1234567890"
                  :user/name "John Doe"})

(defmethod rpc/rpc! :rpc/get-current-user [_req]
  (go
    (<! (timeout 100))
    (merge #_fake-user {:result/type :result/ok})))
