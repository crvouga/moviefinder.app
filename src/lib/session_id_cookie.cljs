(ns lib.session-id-cookie
  (:require
   [lib.http-server.http-req :as http-req]
   [lib.http-server.http-res :as http-res]))


(defn get-cookie [^js req]
  (http-req/get-cookie req "session-id"))

(defn to-cookie [session-id]
  {:cookie/name "session-id"
   :cookie/value session-id
   :cookie/max-age 31536000
   :cookie/secure false
   :cookie/path "/"
   ;;    :cookie/domain "localhost"
   :cookie/same-site "Lax"
   :cookie/http-only true})

(defn set-cookie [res session-id]
  (http-res/set-cookie! res (to-cookie session-id)))

(defn with-cookie [respond]
  (fn [req res]
    (let [session-id (get-cookie req)]
      (when-not session-id
        (set-cookie res (random-uuid)))
      (respond req res))))