(ns lib.session-id-cookie
  (:require
   [lib.http-server.http-req :as http-req]
   [lib.http-server.http-res :as http-res]
   [lib.session-id :as session-id]))


(defn read [^js req]
  (http-req/get-cookie req "session-id"))

(defn create [session-id]
  {:cookie/name "session-id"
   :cookie/value session-id
   :cookie/max-age 31536000
   :cookie/secure false
   :cookie/path "/"
   ;;    :cookie/domain "localhost"
   :cookie/same-site "Lax"
   :cookie/http-only true})

(defn write [res session-id]
  (http-res/set-cookie! res (create session-id)))

(defn with-cookie [respond]
  (fn [req res]
    (let [session-id (read req)]
      (when-not session-id
        (write res (session-id/gen)))
      (respond req res))))