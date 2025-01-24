(ns moviefinder-app.rpc.frontend
  (:require
   [moviefinder-app.frontend.store :as store]
   [moviefinder-app.core.http-client :as http-client]
   [clojure.edn :as edn]
   [clojure.core.async :refer [<! go]]
   [moviefinder-app.core.result :as result]))

(def backend-url-dev "http://localhost:5002")
(def backend-url-prod "")
(defn dev? [] (= (.-hostname js/window.location) "localhost"))
(def backend-url (if (dev?) backend-url-dev backend-url-prod))

(println "backend-url: " backend-url)

(defn- rpc-fetch! [msg]
  (http-client/fetch!
   {:http-request/url (str backend-url "/rpc")
    :http-request/method :http-method/post
    :http-request/headers {"Content-Type" "text/plain"}
    :http-request/body (pr-str msg)}))

(defn rpc! [msg]
  (go
    (let [res (<! (rpc-fetch! msg))
          body (-> res :http-response/body)
          body-edn (-> body edn/read-string)
          ok? (-> res :http-response/ok?)]
      (if ok?
        (result/conform body-edn)
        [:result/err "Errored while requesting from backend"]))))

(defmethod store/eff! :rpc/send! [i]
  (go
    (let [eff-payload (store/eff-payload i)
          msg (-> eff-payload :rpc/req)
          map-res (-> eff-payload :rpc/res)
          res (<! (rpc! msg))
          mapped-res (map-res res)]
      (store/put! i mapped-res))))