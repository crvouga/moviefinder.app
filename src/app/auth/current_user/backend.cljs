(ns app.auth.current-user.backend
  (:require
   [app.rpc.backend :as rpc]
   [clojure.core.async :as a]))

(def fake-user {:user/id "123"
                :user/phone-number "1234567890"
                :user/name "John Doe"})

(defmethod rpc/rpc! :rpc/get-current-user [_req]
  (a/go
    (a/<! (a/timeout 1000))
    (merge fake-user {:result/type :result/ok})))
